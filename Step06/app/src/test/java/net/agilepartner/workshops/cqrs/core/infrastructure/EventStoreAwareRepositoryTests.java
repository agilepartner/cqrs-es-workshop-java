package net.agilepartner.workshops.cqrs.core.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import net.agilepartner.workshops.cqrs.core.Event;
import net.agilepartner.workshops.cqrs.core.MyAggregate;
import net.agilepartner.workshops.cqrs.core.Repository;

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