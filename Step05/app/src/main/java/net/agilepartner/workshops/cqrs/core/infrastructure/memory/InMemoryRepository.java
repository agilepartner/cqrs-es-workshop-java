package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.agilepartner.workshops.cqrs.core.AggregateRoot;
import net.agilepartner.workshops.cqrs.core.Repository;

public class InMemoryRepository<T extends AggregateRoot> implements Repository<T> {
    private final ConcurrentHashMap<UUID, T> map = new ConcurrentHashMap<UUID, T>();

    @Override
    public T getById(UUID id) {
        return map.get((Object) id);
    }

    @Override
    public void save(T aggregate) {
        map.putIfAbsent(aggregate.getId(), aggregate);
    }
}
