# Step 05 : Persisting and publishing events

In the previous step, we implemented a basic `Repository` that holds aggregates in memory. This is obviously not enough. We need to persist all events in some kind of event log/stream. Enters the `EventStore` interface.

## EventStore interface

An *event store* is something pretty simple. It allows you to either load all events or to save news events for a given aggregate.

```Java
public interface EventStore {
    List<? extends Event> load(UUID aggregateId);
    void save(UUID aggregateId, Iterable<? extends Event> newEvents, int expectedVersion) throws OptimisticLockingException;
}
```

When saving new events, we need to handle a possible version problem. This could happen if some other process has changed the aggregate between the moment we loaded it and the moment we want to save it. If the current version number of the aggregate is different from the latest known event version number, an `OptimisticLockingException` will be raised.

```Java
public class OptimisticLockingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OptimisticLockingException(String message) {
        super(message);
    }
}
```

## EventPublisher interface

In an event-sourced system, it's not enough to just save events. Some other components may be interested in the fact that new events have been saved for the aggregate. Therefore, we need a way to publish the events to the outside world so something else can react to them. This is where an `EventPublisher` comes handy.

```Java
public interface EventPublisher {
    <T extends Event> void publish(UUID aggregateId, T event);
}
```

## In memory event store

For the moment, we will implement a simple in memory event store.

```Java
public class InMemoryEventStore implements EventStore {

    private final Map<UUID, List<Event>> events = new ConcurrentHashMap<>();
    private final EventPublisher publisher;

    public InMemoryEventStore(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void save(UUID aggregateId, Iterable<? extends Event> newEvents, int expectedVersion) throws OptimisticLockingException {
        List<Event> existingEvents;
        int currentVersion = 0;
        if (events.containsKey(aggregateId)) {
            existingEvents = events.get(aggregateId);
            currentVersion = existingEvents.get(existingEvents.size() - 1).version;
        } else {
            existingEvents = new ArrayList<>();
            events.put(aggregateId, existingEvents);
        }
        if (expectedVersion != currentVersion)
            throw new OptimisticLockingException(String.format("Expected version %d does not match current stored version %d", expectedVersion, currentVersion));

        for (Event e : newEvents) {
            existingEvents.add(e);
            publisher.publish(aggregateId, e);
        }
    }

    @Override
    public List<? extends Event> load(UUID aggregateId) {
        List<? extends Event> aggregateEvents = events.getOrDefault(aggregateId, new ArrayList<>());
        return new ArrayList<>(aggregateEvents);
    }
}
```

And we can implement some tests.

```Java
@RunWith(SpringRunner.class)
public class InMemoryEventStoreTests {

    @Mock
    EventPublisher publisher;

    @Test
    public void saveEventsForNewAggregate() {
        EventStore eventStore = new InMemoryEventStore(publisher);
        UUID aggregateId = UUID.randomUUID();
        List<Event> newEvents = new ArrayList<>();

        NameChanged evt1 = new NameChanged(aggregateId, "first name");
        evt1.version = 1;

        NameChanged evt2 = new NameChanged(aggregateId, "second name");
        evt2.version = 2;

        newEvents.add(evt1);
        newEvents.add(evt2);

        eventStore.save(aggregateId, newEvents, 0);
        List<? extends Event> savedEvents = eventStore.load(aggregateId);

        assertEquals(2, savedEvents.size());
        assertEquals(1, savedEvents.get(0).version);
        assertEquals("first name", ((NameChanged)savedEvents.get(0)).name);
        assertEquals(2, savedEvents.get(1).version);
        assertEquals("second name", ((NameChanged)savedEvents.get(1)).name);
        verify(publisher).publish(aggregateId, evt1);
        verify(publisher).publish(aggregateId, evt2);
    }

    @Test
    public void saveEventsForExistingAggregate() {
        EventStore eventStore = new InMemoryEventStore(publisher);
        UUID aggregateId = UUID.randomUUID();
        List<Event> existingEvents = new ArrayList<>();

        NameChanged evt1 = new NameChanged(aggregateId, "first name");
        evt1.version = 1;

        NameChanged evt2 = new NameChanged(aggregateId, "second name");
        evt2.version = 2;

        existingEvents.add(evt1);
        existingEvents.add(evt2);

        eventStore.save(aggregateId, existingEvents, 0);

        NameChanged evt3 = new NameChanged(aggregateId, "third name");
        evt3.version = 3;
        NameChanged evt4 = new NameChanged(aggregateId, "fourth name");
        evt4.version = 4;

        List<Event> newEvents = new ArrayList<>();
        newEvents.add(evt3);
        newEvents.add(evt4);

        eventStore.save(aggregateId, newEvents, 2);

        List<? extends Event> savedEvents = eventStore.load(aggregateId);

        assertEquals(4, savedEvents.size());
        assertEquals(3, savedEvents.get(2).version);
        assertEquals("third name", ((NameChanged)savedEvents.get(2)).name);
        assertEquals(4, savedEvents.get(3).version);
        assertEquals("fourth name", ((NameChanged)savedEvents.get(3)).name);
        verify(publisher).publish(aggregateId, evt3);
        verify(publisher).publish(aggregateId, evt4);
    }

    @Test
    public void saveEventsRaisesConcurrencyProblem() {
        EventStore eventStore = new InMemoryEventStore(publisher);
        UUID aggregateId = UUID.randomUUID();
        List<Event> existingEvents = new ArrayList<>();

        NameChanged evt1 = new NameChanged(aggregateId, "first name");
        evt1.version = 1;

        NameChanged evt2 = new NameChanged(aggregateId, "second name");
        evt2.version = 2;

        existingEvents.add(evt1);
        existingEvents.add(evt2);

        eventStore.save(aggregateId, existingEvents, 0);

        NameChanged evt3 = new NameChanged(aggregateId, "third name");
        evt3.version = 3;
        List<Event> otherEvents = new ArrayList<>();
        otherEvents.add(evt3);
        eventStore.save(aggregateId, otherEvents, evt2.version);

        NameChanged evt4 = new NameChanged(aggregateId, "fourth name");
        evt4.version = 4;
        List<Event> newEvents = new ArrayList<>();
        newEvents.add(evt4);

        try {
            eventStore.save(aggregateId, newEvents, evt2.version);
            Assert.fail("Should have raised OptimisticLockingException");
        } catch (OptimisticLockingException e) { }
    }
}
```

## Repository using event store to persist and retrieve events

Now that we have introduced the notion of *event store*, we can write a new kind of `Repository`, that will use the `EventStore` to persist the events of an `AggregateRoot`.

```Java
public class EventStoreAwareRepository<T extends AggregateRoot> implements Repository<T> {
    private final EventStore eventStore;
    private final Function<UUID, T> factory;

    public EventStoreAwareRepository(EventStore eventStore, Function<UUID, T> factory) {
        this.eventStore = eventStore;
        this.factory = factory;
    }

    @Override
    public T getById(UUID aggregateId) {
        T aggregate =  factory.apply(aggregateId);
        Collection<? extends Event> events  = eventStore.load(aggregate.getId());
        if (events == null || events.size() == 0) {
            throw new AggregateNotFoundException(aggregateId);
        }

        aggregate.loadFromHistory(events);
        return aggregate;
    }

    @Override
    public void save(AggregateRoot aggregate) throws OptimisticLockingException {
        Guards.checkNotNull(aggregate.getId());
        eventStore.save(aggregate.getId(), aggregate.getUncommittedChanges(), aggregate.getOriginalVersion());
        aggregate.markChangesAsCommitted();
    }
}
```

We inject two things into the `Repository`

* First the `EventStore` that is used to persist the events and get them back.
* Second, a factory function that is used to create a new instance of the *aggregate*.

We could have used some weird voodoo *reflection* magic to create a new instance of the *aggregate*, but instead, we prefer to be explicit and simple, and just pass the factory function as a dependency.

We also need a new exception `AggregateNotFoundException` in case no events for the given aggregate are found in the *event store*.

```Java
public class AggregateNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -3750447531677604222L;

    public AggregateNotFoundException(UUID id) {
        super("Aggregate not found. Id=" + id.toString());
    }
}
```

And of course, now we can implement the tests

```Java
@RunWith(SpringRunner.class)
public class EventStoreAwareRepositoryTests {
    @Mock
    EventStore eventStore;

    @Test
    public void saveNewAggregate() {
        Repository<MyAggregate> repository = new EventStoreAwareRepository<>(
            eventStore,
            id -> new MyAggregate(id));

        UUID aggregateId = UUID.randomUUID();
        MyAggregate aggregate = new MyAggregate(aggregateId);
        aggregate.changeName("New name");

        Iterable<? extends Event> events = aggregate.getUncommittedChanges();

        assertEquals(1, aggregate.getVersion());
        assertEquals(0, aggregate.getOriginalVersion());

        repository.save(aggregate);

        assertFalse(aggregate.getUncommittedChanges().iterator().hasNext());
        assertEquals(1, aggregate.getVersion());
        assertEquals(1, aggregate.getOriginalVersion());
        verify(eventStore).save(aggregateId, events, 0);
    }

    @Test
    public void saveExistingAggregate() {
        Repository<MyAggregate> repository = new EventStoreAwareRepository<>(
            eventStore,
            id -> new MyAggregate(id));

        UUID aggregateId = UUID.randomUUID();
        MyAggregate aggregate = new MyAggregate(aggregateId);
        aggregate.changeName("New name 1");
        aggregate.changeName("New name 2");

        repository.save(aggregate);

        aggregate.changeName("New name 3");
        aggregate.changeName("New name 4");

        assertEquals(4, aggregate.getVersion());
        assertEquals(2, aggregate.getOriginalVersion());

        Iterable<? extends Event> events = aggregate.getUncommittedChanges();

        repository.save(aggregate);

        assertFalse(aggregate.getUncommittedChanges().iterator().hasNext());
        assertEquals(4, aggregate.getVersion());
        assertEquals(4, aggregate.getOriginalVersion());
        verify(eventStore).save(aggregateId, events, 2);
    }
}
```

## Putting it all together

```Java
public class End2EndTests {

    @Test
    public void wireUpWithInMemory() {
        Repository<InventoryItem> repository = new InMemoryRepository<InventoryItem>();

        runEnd2EndTests(repository);
    }

    @Test
    public void wireUpWithEventStore() {
        EventPublisher publisher = new NoopPublisher();
        EventStore eventStore = new InMemoryEventStore(publisher);
        Repository<InventoryItem> repository = new EventStoreAwareRepository<InventoryItem>(eventStore,
                aggregateId -> new InventoryItem(aggregateId));

        runEnd2EndTests(repository);
    }

    private void runEnd2EndTests(Repository<InventoryItem> repository) {
        CommandResolver resolver = InMemoryCommandResolver.getInstance();
        resolver.register(new CreateInventoryItemHandler(repository), CreateInventoryItem.class);
        resolver.register(new RenameInventoryItemHandler(repository), RenameInventoryItem.class);
        resolver.register(new CheckInventoryItemInHandler(repository), CheckInventoryItemIn.class);
        resolver.register(new CheckInventoryItemOutHandler(repository), CheckInventoryItemOut.class);
        resolver.register(new DeactivateInventoryItemHandler(repository), DeactivateInventoryItem.class);

        CommandDispatcher dispatcher = new SimpleCommandDispatcher(resolver);

        CreateInventoryItem createApple = CreateInventoryItem.create("Apple", 10);
        CreateInventoryItem createBanana = CreateInventoryItem.create("Banana", 7);
        CreateInventoryItem createOrange = CreateInventoryItem.create("Orange", 5);

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

Notice how we refactored the code to avoid duplication. The only thing that changes between the two tests is the implementation of the `Repository`.

We also implemented a `NoopPublisher` that does absolutely nothing.

```Java
public class NoopPublisher implements EventPublisher {
    @Override
    public <T extends Event> void publish(UUID aggregateId, T event) {
    }
}
```

We have now a simple in-memory implementation of an *event store*. Later on, we will see that we have many different ways to implement a more robust version of an *event store*, that will actually persist events.

* Using any relational database
* Using any noSQL database
* using [Greg Young's EventStore](https://eventstore.org/)
* Using Kafka streams

We also publish all events once the are saved.

## What's next

In the next step, we will see how we can react to events being published to generate materialized views, a.k.a. read models, a.k.a. projections.

* Go to [Step 06](../Step06/Step06.md)
* Go back to [Home](../README.md)