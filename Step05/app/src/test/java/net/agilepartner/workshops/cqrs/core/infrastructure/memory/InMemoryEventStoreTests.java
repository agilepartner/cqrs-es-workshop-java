package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import net.agilepartner.workshops.cqrs.core.Event;
import net.agilepartner.workshops.cqrs.core.EventPublisher;
import net.agilepartner.workshops.cqrs.core.NameChanged;
import net.agilepartner.workshops.cqrs.core.infrastructure.EventStore;
import net.agilepartner.workshops.cqrs.core.infrastructure.OptimisticLockingException;

@RunWith(SpringRunner.class)
public class InMemoryEventStoreTests {

    @Mock
    EventPublisher publisher;

    @Test
    public void saveEventsForNewAggregate() {
        EventStore eventStore = new InMemoryEventStore(publisher);
        UUID aggregateId = UUID.randomUUID();
        List<Event> newEvents = new ArrayList<>();

        NameChanged evt1 = new NameChanged(aggregateId, "first name");
        evt1.version = 1;

        NameChanged evt2 = new NameChanged(aggregateId, "second name");
        evt2.version = 2;

        newEvents.add(evt1);
        newEvents.add(evt2);

        eventStore.save(aggregateId, newEvents, 0);
        List<? extends Event> savedEvents = eventStore.load(aggregateId);

        assertEquals(2, savedEvents.size());
        assertEquals(1, savedEvents.get(0).version);
        assertEquals("first name", ((NameChanged)savedEvents.get(0)).name);
        assertEquals(2, savedEvents.get(1).version);
        assertEquals("second name", ((NameChanged)savedEvents.get(1)).name);
        verify(publisher).publish(aggregateId, evt1);
        verify(publisher).publish(aggregateId, evt2);
    }

    @Test
    public void saveEventsForExistingAggregate() {
        EventStore eventStore = new InMemoryEventStore(publisher);
        UUID aggregateId = UUID.randomUUID();
        List<Event> existingEvents = new ArrayList<>();

        NameChanged evt1 = new NameChanged(aggregateId, "first name");
        evt1.version = 1;

        NameChanged evt2 = new NameChanged(aggregateId, "second name");
        evt2.version = 2;

        existingEvents.add(evt1);
        existingEvents.add(evt2);

        eventStore.save(aggregateId, existingEvents, 0);

        NameChanged evt3 = new NameChanged(aggregateId, "third name");
        evt3.version = 3;
        NameChanged evt4 = new NameChanged(aggregateId, "fourth name");
        evt4.version = 4;

        List<Event> newEvents = new ArrayList<>();
        newEvents.add(evt3);
        newEvents.add(evt4);

        eventStore.save(aggregateId, newEvents, 2);

        List<? extends Event> savedEvents = eventStore.load(aggregateId);

        assertEquals(4, savedEvents.size());
        assertEquals(3, savedEvents.get(2).version);
        assertEquals("third name", ((NameChanged)savedEvents.get(2)).name);
        assertEquals(4, savedEvents.get(3).version);
        assertEquals("fourth name", ((NameChanged)savedEvents.get(3)).name);
        verify(publisher).publish(aggregateId, evt3);
        verify(publisher).publish(aggregateId, evt4);
    }

    @Test
    public void saveEventsRaisesConcurrencyProblem() {
        EventStore eventStore = new InMemoryEventStore(publisher);
        UUID aggregateId = UUID.randomUUID();
        List<Event> existingEvents = new ArrayList<>();

        NameChanged evt1 = new NameChanged(aggregateId, "first name");
        evt1.version = 1;

        NameChanged evt2 = new NameChanged(aggregateId, "second name");
        evt2.version = 2;

        existingEvents.add(evt1);
        existingEvents.add(evt2);

        eventStore.save(aggregateId, existingEvents, 0);

        NameChanged evt3 = new NameChanged(aggregateId, "third name");
        evt3.version = 3;
        List<Event> otherEvents = new ArrayList<>();
        otherEvents.add(evt3);
        eventStore.save(aggregateId, otherEvents, evt2.version);

        NameChanged evt4 = new NameChanged(aggregateId, "fourth name");
        evt4.version = 4;
        List<Event> newEvents = new ArrayList<>();
        newEvents.add(evt4);

        try {
            eventStore.save(aggregateId, newEvents, evt2.version);
            Assert.fail("Should have raised OptimisticLockingException");
        } catch (OptimisticLockingException e) { }
    }
}