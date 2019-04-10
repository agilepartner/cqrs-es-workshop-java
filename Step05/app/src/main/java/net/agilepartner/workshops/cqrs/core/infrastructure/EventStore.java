package net.agilepartner.workshops.cqrs.core.infrastructure;

import java.util.List;
import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Event;

public interface EventStore {
    void save(UUID aggregateId, List<? extends Event> newEvents, int expectedVersion) throws OptimisticLockingException;
    List<? extends Event> load(UUID aggregateId);
}