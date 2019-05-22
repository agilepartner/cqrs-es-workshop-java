# Step 06 : Materialized view, a.k.a. read models, a.k.a. projections

So fare we have mainly focused on the *write side* of CQRS, dealing with *commands*, *aggregates* and *event streams*. But what about the *read side* ?

The main idea behind CQRS is to make the *query/read side* as fast and efficient as possible by denormalizing data as much as needed, to create what we call *materialized views*. We also call these read models or projections. Because we know that, in average, 80% of requests on an application are reads and only 20% of writes, it makes sense to optimize for read.

To be able to react to events and denormalize the data they contain to create materialized views, we need some more infrastructure, starting with something like `EventHandler`.

## Infrastructure

### Event handler

An *event handler* is quite symmetrical to a *command handler*. Its role is to react to a specific type of event.

```Java
public interface EventHandler<T extends Event>  {
    public void handle(T event);
}
```

### Event resolver

Opposite to *command handlers*, where you can have only one handler for a given *command*, we actually want to give the possibility to have several *event handlers* for a given *event*.

```Java
public interface EventResolver {
    public <T extends Event> Iterable<EventHandler<T>> findHandlersFor(Class<?> evtClass);
    public <T extends Event> void register(EventHandler<T> handler, Class<?> evtClass);
}
```

Note that the return type of `findHandlersFor` returns an `Iterable`.

### In-memory event resolver implementation

The concrete implementation is straight forward.

```Java
public class InMemoryEventResolver implements EventResolver {

    private final Map<String, List<EventHandler<? extends Event>>> eventHandlers = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> Iterable<EventHandler<T>> findHandlersFor(Class<?> evtClass) {
        List<EventHandler<?>> handlers = eventHandlers.get(evtClass.getSimpleName());
        if (handlers == null)
            throw new UnsupportedOperationException(String.format("No handlers defined for event %s", evtClass.getSimpleName()));

        List<EventHandler<T>> concreteHandlers = new ArrayList<>();
        for (EventHandler<?> handler : handlers) {
            concreteHandlers.add((EventHandler<T>) handler);
        }

        return concreteHandlers;
    }

    @Override
    public <T extends Event> void register(EventHandler<T> handler, Class<?> evtClass) {
        List<EventHandler<?>> handlers;
        if (eventHandlers.containsKey(evtClass.getSimpleName())) {
            handlers = eventHandlers.get(evtClass.getSimpleName());
        } else {
            handlers = new ArrayList<>();
            eventHandlers.put(evtClass.getSimpleName(), handlers);
        }
        handlers.add(handler);
    }
}
```

### In-memory event publisher implementation

We also need an implementation for the `EventPublisher` that will allow to publish events from the `EventStore` and be use the resolver to handle events.

```Java
public class InMemoryEventPublisher implements EventPublisher {
    private final EventResolver resolver;

    public InMemoryEventPublisher(EventResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T extends Event> void publish(UUID aggregateId, T event) {
        Iterable<EventHandler<T>> eventHandlers = resolver.findHandlersFor(event.getClass());
        for (EventHandler<T> eventHandler : eventHandlers) {
            eventHandler.handle(event);
        }
    }
}
```

### Tests for event resolver and publisher

We can test that resolver and publisher collaborate together pretty well.

```Java
public class InMemoryEventResolverAndPublisherTests {

    @Test
    public void registerSingleHandler() {
        FirstHandler first = new FirstHandler();
        SecondHandler second = new SecondHandler();

        EventResolver resolver = new InMemoryEventResolver();
        resolver.register(first, NameChanged.class);

        UUID aggregateId = UUID.randomUUID();
        NameChanged nameChanged = new NameChanged(aggregateId, "Super name");

        EventPublisher publisher = new InMemoryEventPublisher(resolver);
        publisher.publish(aggregateId, nameChanged);

        assertTrue(first.wasCalled());
        assertFalse(second.wasCalled());
    }

    @Test
    public void registerSeveralHandlers() {
        FirstHandler first = new FirstHandler();
        SecondHandler second = new SecondHandler();

        EventResolver resolver = new InMemoryEventResolver();
        resolver.register(first, NameChanged.class);
        resolver.register(second, NameChanged.class);

        UUID aggregateId = UUID.randomUUID();
        NameChanged nameChanged = new NameChanged(aggregateId, "Super name");

        EventPublisher publisher = new InMemoryEventPublisher(resolver);
        publisher.publish(aggregateId, nameChanged);

        assertTrue(first.wasCalled());
        assertTrue(second.wasCalled());
    }

    @Test
    public void registerDifferentHandlers() {
        FirstHandler first = new FirstHandler();
        SecondHandler second = new SecondHandler();
        NewEventHandler newHandler = new NewEventHandler();

        EventResolver resolver = new InMemoryEventResolver();
        resolver.register(first, NameChanged.class);
        resolver.register(second, NameChanged.class);
        resolver.register(newHandler, NewEvent.class);

        EventPublisher publisher = new InMemoryEventPublisher(resolver);

        UUID aggregateId = UUID.randomUUID();
        NameChanged nameChanged = new NameChanged(aggregateId, "Super name");
        publisher.publish(aggregateId, nameChanged);

        assertTrue(first.wasCalled());
        assertTrue(second.wasCalled());
        assertFalse(newHandler.wasCalled());

        NewEvent newEvent = new NewEvent();
        publisher.publish(aggregateId, newEvent);

        assertTrue(first.wasCalled());
        assertTrue(second.wasCalled());
        assertTrue(newHandler.wasCalled());
    }

    private class FirstHandler extends TestHandler<NameChanged> {
    }

    private class SecondHandler extends TestHandler<NameChanged> {
    }

    private class NewEventHandler extends TestHandler<NewEvent> {
    }

    private class NewEvent extends Event {
        private static final long serialVersionUID = 1L;
    }

    private abstract class TestHandler<T extends Event> implements EventHandler<T>{

        private Boolean called;

        public TestHandler() {
            called = false;
        }

        @Override
        public void handle(T event) {
            called = true;
        }

        public Boolean wasCalled() {
            return called;
        }
    }
}
```

Notice all the private classes that are there only as helper for the tests.

## Denormalizing InventoryItem events into materialized views

Now that we have completed our infrastructure, we can actually focus on the denormalization of events into materialized views.

### Inventory Item read model

Let's start by defining a *value object* that represents an *inventory item read model*. Remember that a value object is always immutable. Use of *value object* is a great way to avoid side effects.

```Java
public class InventoryItemReadModel {
    public final String name;
    public final int quantity;

    public InventoryItemReadModel(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}
```

### Inventory view

Now we can define a view that will handle all the events related to `InventoryItem` and maintain a list of read models.

```Java
public class InventoryView {
    private final Map<UUID, InventoryItemReadModel> inventory = new ConcurrentHashMap<>();

    public final InventoryItemCreatedHandler createdHandler = new InventoryItemCreatedHandler();
    public final InventoryItemRenamedHandler renamedHandler = new InventoryItemRenamedHandler();
    public final InventoryItemCheckedInHandler checkedInHandler = new InventoryItemCheckedInHandler();
    public final InventoryItemCheckedOutHandler checkedOutHandler = new InventoryItemCheckedOutHandler();
    public final InventoryItemDeactivatedHandler deactivatedHandler = new InventoryItemDeactivatedHandler();

    public InventoryItemReadModel get(UUID aggregateId) {
        return inventory.get(aggregateId);
    }

    class InventoryItemCreatedHandler implements EventHandler<InventoryItemCreated> {
        @Override
        public void handle(InventoryItemCreated event) {
            InventoryItemReadModel item = new InventoryItemReadModel(event.name, event.quantity);
            inventory.put(event.aggregateId, item);
        }
    }

    class InventoryItemRenamedHandler implements EventHandler<InventoryItemRenamed> {
        @Override
        public void handle(InventoryItemRenamed event) {
            InventoryItemReadModel existingItem = inventory.get(event.aggregateId);
            inventory.put(event.aggregateId, new InventoryItemReadModel(event.name, existingItem.quantity));
        }
    }

    class InventoryItemCheckedInHandler implements EventHandler<InventoryItemCheckedIn> {
        @Override
        public void handle(InventoryItemCheckedIn event) {
            InventoryItemReadModel existingItem = inventory.get(event.aggregateId);
            inventory.put(event.aggregateId, new InventoryItemReadModel(existingItem.name, existingItem.quantity + event.quantity));
        }
    }

    class InventoryItemCheckedOutHandler implements EventHandler<InventoryItemCheckedOut> {
        @Override
        public void handle(InventoryItemCheckedOut event) {
            InventoryItemReadModel existingItem = inventory.get(event.aggregateId);
            inventory.put(event.aggregateId, new InventoryItemReadModel(existingItem.name, existingItem.quantity - event.quantity));
        }
    }

    class InventoryItemDeactivatedHandler implements EventHandler<InventoryItemDeactivated> {
        @Override
        public void handle(InventoryItemDeactivated event) {
            inventory.remove(event.aggregateId);
        }
    }
}
```

Here we use a trick to work around the fact that Java does not allow multiple implementation of the same generic interface `EventHandler<>`. That is why we define inner classes for each handler. Because they are inner classes, they are allowed to access to `InventoryView` inner state. We use them to maintain the `Map` of read models.

Note that we need to instantiate each event handler as a public field, because we will have to register the handler in the `EventResolver` later.

### Implementing the end to end tests

Because we started to have a lot of duplicated code, we refactored our end to end tests by extracting meaningful methods.

The test that is the most interesting to us right now is `wireUpWithMaterializedView`. In this test, we instantiate an `InventoryView`, create an `EventPublisher` in which we register the handlers of the view. We can then build the `Repository` that uses the `EventStore`, as well as the `CommandDispatcher`. Once we have wired all these component up, we can run our tests.

```Java
public class End2EndTests {

    private UUID appleId;
    private UUID bananaId;
    private UUID orangeId;

    class Fruits {
        public static final String Apple = "Apple";
        public static final String Banana = "Banana";
        public static final String Orange = "Orange";
        public static final String Pear = "Pear";
    }

    @Test
    public void wireUpWithInMemoryRepository() {
        Repository<InventoryItem> repository = new InMemoryRepository<InventoryItem>();
        CommandDispatcher dispatcher = buildCommandDispatcher(repository);
        runEnd2EndTests(dispatcher);
    }

    @Test
    public void wireUpWithEventStoreAwareRepository() {
        EventPublisher eventPublisher = new NoopPublisher();
        Repository<InventoryItem> repository = buildRepository(eventPublisher);
        CommandDispatcher dispatcher = buildCommandDispatcher(repository);

        runEnd2EndTests(dispatcher);
    }

    @Test
    public void wireUpWithMaterializedView() {
        InventoryView view = new InventoryView();
        EventPublisher eventPublisher = buildEventPublisher(view);
        Repository<InventoryItem> repository = buildRepository(eventPublisher);
        CommandDispatcher dispatcher = buildCommandDispatcher(repository);

        runEnd2EndTests(dispatcher);

        InventoryItemReadModel apples = view.get(appleId);
        assertNull(apples);
        InventoryItemReadModel bananas = view.get(bananaId);
        assertEquals(Fruits.Banana, bananas.name);
        assertEquals(5, bananas.quantity);
        InventoryItemReadModel oranges = view.get(orangeId);
        assertEquals(Fruits.Pear, oranges.name);
        assertEquals(0, oranges.quantity);
    }

    private EventPublisher buildEventPublisher(InventoryView view) {
        EventResolver eventResolver = new InMemoryEventResolver();
        eventResolver.register(view.createdHandler, InventoryItemCreated.class);
        eventResolver.register(view.renamedHandler, InventoryItemRenamed.class);
        eventResolver.register(view.checkedInHandler, InventoryItemCheckedIn.class);
        eventResolver.register(view.checkedOutHandler, InventoryItemCheckedOut.class);
        eventResolver.register(view.deactivatedHandler, InventoryItemDeactivated.class);

        return new InMemoryEventPublisher(eventResolver);
    }

    private Repository<InventoryItem> buildRepository(EventPublisher publisher) {
        EventStore eventStore = new InMemoryEventStore(publisher);
        Repository<InventoryItem> repository = new EventStoreAwareRepository<InventoryItem>(eventStore,
                aggregateId -> new InventoryItem(aggregateId));
        return repository;
    }

    private CommandDispatcher buildCommandDispatcher(Repository<InventoryItem> repository) {
        CommandResolver resolver = InMemoryCommandResolver.getInstance();
        resolver.register(new CreateInventoryItemHandler(repository), CreateInventoryItem.class);
        resolver.register(new RenameInventoryItemHandler(repository), RenameInventoryItem.class);
        resolver.register(new CheckInventoryItemInHandler(repository), CheckInventoryItemIn.class);
        resolver.register(new CheckInventoryItemOutHandler(repository), CheckInventoryItemOut.class);
        resolver.register(new DeactivateInventoryItemHandler(repository), DeactivateInventoryItem.class);

        return new SimpleCommandDispatcher(resolver);
    }

    private void runEnd2EndTests(CommandDispatcher dispatcher) {
        CreateInventoryItem createApple = CreateInventoryItem.create(Fruits.Apple, 10);
        appleId = createApple.aggregateId;
        CreateInventoryItem createBanana = CreateInventoryItem.create(Fruits.Banana, 7);
        bananaId = createBanana.aggregateId;
        CreateInventoryItem createOrange = CreateInventoryItem.create(Fruits.Orange, 5);
        orangeId = createOrange.aggregateId;

        try {
            // Create fruits
            dispatcher.dispatch(createApple);
            dispatcher.dispatch(createBanana);
            dispatcher.dispatch(createOrange);

            // Check out
            dispatcher.dispatch(CheckInventoryItemOut.create(createApple.aggregateId, 5)); // 5 apples left
            dispatcher.dispatch(CheckInventoryItemOut.create(createBanana.aggregateId, 5)); // 2 bananas left
            dispatcher.dispatch(CheckInventoryItemOut.create(createOrange.aggregateId, 5)); // 0 oranges left

            // Checking out too many oranges
            try {
                dispatcher.dispatch(CheckInventoryItemOut.create(createOrange.aggregateId, 5)); // Cannot check more
                                                                                                // oranges out
                Assert.fail("Should have raised NotEnoughStockException");
            } catch (NotEnoughStockException ex) {
            }

            // Renaming orange to pear
            dispatcher.dispatch(RenameInventoryItem.create(createOrange.aggregateId, "Pear")); // 0 pears left

            // Resupplying bananas (everybody loves bananas)
            dispatcher.dispatch(CheckInventoryItemIn.create(createBanana.aggregateId, 3)); // 5 bananas left

            // Nobody wants apples anymore
            dispatcher.dispatch(DeactivateInventoryItem.create(createApple.aggregateId)); // apple item deactivated

            // Can't check in an item that is deactivated
            try {
                dispatcher.dispatch(CheckInventoryItemIn.create(createApple.aggregateId, 5));
                Assert.fail("Should not be able to check apples in because the item is deactivated");
            } catch (InventoryItemDeactivatedException ex) {
            }

        } catch (DomainException e) {
            Assert.fail("Should not have raised any exception");
        }
    }
}
```

## Conclusion

We now have all the components that we need.

Our system has:

* a *command dispatcher* that dispatches commands to handlers
* a command handler per command that know how to process that command
* a repository that uses an event store to rehydrate the aggregate from the event stream
* an aggregate that implements the logic related to inventory item
* some event that is generated by the aggregate when a public behavior is called
* a repository that saves all the events generated by the aggregate and publish them
* event handlers that react to the different type of events to maintain a view up to date
* a view that contains read models of all inventory items known by the system

The main issue with our implementation is first that everything so far runs in memory, and second that all the calls are synchronous. Because we have a very trivial example, processing a command, saving the events to a stream and updating the view does not take that much time, and performances are quite acceptable. But what if we had a longer process to run. Will this synchronicity really scale ?

The quick answer is no. However, we have other priorities for now. Indeed, we have a great domain model and read models, but none of them are actually reachable from the outside world. How useful is this?

## What's next

In the next step, we will implement an API using GraphQL.

* Go to [Step 07](../Step07/Step07.md)
* Go back to [Home](../README.md)