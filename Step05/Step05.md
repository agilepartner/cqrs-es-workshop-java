# Step 05 : Persisting and publishing events

In the previous step, we implemented a basic `Repository` that holds aggregates in memory. This is obviously not enough. We need to persist all events in some kind of event log/stream. Enters the `EventStore` interface.

## EventStore interface

An *event store* is something pretty simple. It allows you to either load all events or to save news events for a given aggregate.

```Java
public interface EventStore {
    void save(UUID aggregateId, List<? extends Event> newEvents, int expectedVersion) throws OptimisticLockingException;
    List<? extends Event> load(UUID aggregateId);
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
    public void save(UUID aggregateId, List<? extends Event> newEvents, int expectedVersion) throws OptimisticLockingException {
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
        List<? extends Event> aggreagateEvents = events.getOrDefault(aggregateId, new ArrayList<>());
        return new ArrayList<>(aggreagateEvents);
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

Later on, we will see that we can use many different ways to save events as an event stream

* Using any relational database
* Using any noSQL database
* using [Greg Young's EventStore](https://eventstore.org/)
* Using Kafka streams

## Event handler

<!-- Step 06 ???? -->

```Java
public interface MessageHandler<T extends Message>  {
    public void handle(T action);
}
```

```Java
public interface EventHandler<T extends Event> extends MessageHandler<T>  {
    public void handle(T event);
}
```

```Java

```

```Java

```

```Java

```

```Java

```

## What's next

In the next step, we will ...

* Go to [Step 05](../Step06/Step06.md)
* Go back to [Home](../README.md)