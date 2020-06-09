package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Test;

import net.agilepartner.workshops.cqrs.core.MyAggregate;

public class InMemoryRepositoryTests {

    @Test
    public void getByIdDoesNotReturnValue() {
        InMemoryRepository<MyAggregate> repository = new InMemoryRepository<>();
        MyAggregate aggregate = repository.getById(UUID.randomUUID());
        assertNull(aggregate);
    }

    @Test
    public void saveAndGetByIdReturnsValue() {
        InMemoryRepository<MyAggregate> repository = new InMemoryRepository<>();
        UUID aggregateId = UUID.randomUUID();

        repository.save(new MyAggregate(aggregateId));
        MyAggregate aggregate = repository.getById(aggregateId);

        assertNotNull(aggregate);
        assertEquals(aggregateId, aggregate.getId());
    }
}