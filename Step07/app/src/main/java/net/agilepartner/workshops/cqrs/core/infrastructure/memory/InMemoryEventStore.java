package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import java.util.*;
import java.util.concurrent.*;

import net.agilepartner.workshops.cqrs.core.Event;
import net.agilepartner.workshops.cqrs.core.EventPublisher;
import net.agilepartner.workshops.cqrs.core.infrastructure.EventStore;
import net.agilepartner.workshops.cqrs.core.infrastructure.OptimisticLockingException;

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
