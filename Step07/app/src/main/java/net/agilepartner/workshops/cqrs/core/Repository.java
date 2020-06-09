package net.agilepartner.workshops.cqrs.core;

import java.util.UUID;

public interface Repository<T extends AggregateRoot> {
    T getById(UUID aggregateId);
    void save(T aggregate);
}