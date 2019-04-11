package net.agilepartner.workshops.cqrs.core.infrastructure;

import java.util.*;
import java.util.function.Function;

import net.agilepartner.workshops.cqrs.core.*;

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