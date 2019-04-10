package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import net.agilepartner.workshops.cqrs.core.MyAggregate;

@RunWith(SpringRunner.class)
public class InMemoryRepositoryTests {

    @Test
    public void getByIdDoesNotReturnValue() {
        InMemoryRepository<MyAggregate> repository = new InMemoryRepository<MyAggregate>();
        MyAggregate aggregate = repository.getById(UUID.randomUUID());
        assertNull(aggregate);
    }

    @Test
    public void saveAndGetByIdReturnsValue() {
        InMemoryRepository<MyAggregate> repository = new InMemoryRepository<MyAggregate>();
        UUID aggregateId = UUID.randomUUID();

        repository.save(new MyAggregate(aggregateId));
        MyAggregate aggregate = repository.getById(aggregateId);

        assertNotNull(aggregate);
        assertEquals(aggregateId, aggregate.getId());
    }
}