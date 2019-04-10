package net.agilepartner.workshops.cqrs.core.infrastructure;

import java.util.*;

import net.agilepartner.workshops.cqrs.core.Event;

public interface EventStore {
    List<? extends Event> load(UUID aggregateId);
    void save(UUID aggregateId, Iterable<? extends Event> newEvents, int expectedVersion) throws OptimisticLockingException;
}