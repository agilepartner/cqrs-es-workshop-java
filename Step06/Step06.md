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

```Java

```

```Java

```

```Java

```

```Java

```

## What's next

In the next step, we will...

* Go to [Step 07](../Step07/Step07.md)
* Go back to [Home](../README.md)