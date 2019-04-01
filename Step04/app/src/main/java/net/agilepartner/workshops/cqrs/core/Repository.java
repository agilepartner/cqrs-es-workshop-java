package net.agilepartner.workshops.cqrs.core;

import java.util.UUID;

public interface Repository<T extends AggregateRoot> {
    public T getById(UUID id);
    public void save(T aggregate);
}