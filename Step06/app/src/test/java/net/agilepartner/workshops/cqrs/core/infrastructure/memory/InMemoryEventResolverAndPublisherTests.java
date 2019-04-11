package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.*;

import net.agilepartner.workshops.cqrs.core.*;
import net.agilepartner.workshops.cqrs.core.infrastructure.*;

public class InMemoryEventResolverAndPublisherTests {

    @Test
    public void registerSingleHandler() {
        FirstHandler first = new FirstHandler();
        SecondHandler second = new SecondHandler();

        EventResolver resolver = new InMemoryEventResolver();
        resolver.register(first, NameChanged.class);

        UUID aggregateId = UUID.randomUUID();
        NameChanged nameChanged = new NameChanged(aggregateId, "Super name");

        EventPublisher publisher = new InMemoryEventPublisher(resolver);
        publisher.publish(aggregateId, nameChanged);

        assertTrue(first.wasCalled());
        assertFalse(second.wasCalled());
    }

    @Test
    public void registerSeveralHandlers() {
        FirstHandler first = new FirstHandler();
        SecondHandler second = new SecondHandler();

        EventResolver resolver = new InMemoryEventResolver();
        resolver.register(first, NameChanged.class);
        resolver.register(second, NameChanged.class);

        UUID aggregateId = UUID.randomUUID();
        NameChanged nameChanged = new NameChanged(aggregateId, "Super name");

        EventPublisher publisher = new InMemoryEventPublisher(resolver);
        publisher.publish(aggregateId, nameChanged);

        assertTrue(first.wasCalled());
        assertTrue(second.wasCalled());
    }

    @Test
    public void registerDifferentHandlers() {
        FirstHandler first = new FirstHandler();
        SecondHandler second = new SecondHandler();
        NewEventHandler newHandler = new NewEventHandler();

        EventResolver resolver = new InMemoryEventResolver();
        resolver.register(first, NameChanged.class);
        resolver.register(second, NameChanged.class);
        resolver.register(newHandler, NewEvent.class);

        EventPublisher publisher = new InMemoryEventPublisher(resolver);

        UUID aggregateId = UUID.randomUUID();
        NameChanged nameChanged = new NameChanged(aggregateId, "Super name");
        publisher.publish(aggregateId, nameChanged);

        assertTrue(first.wasCalled());
        assertTrue(second.wasCalled());
        assertFalse(newHandler.wasCalled());

        NewEvent newEvent = new NewEvent();
        publisher.publish(aggregateId, newEvent);

        assertTrue(first.wasCalled());
        assertTrue(second.wasCalled());
        assertTrue(newHandler.wasCalled());
    }

    private class FirstHandler extends TestHandler<NameChanged> {
    }

    private class SecondHandler extends TestHandler<NameChanged> {
    }

    private class NewEventHandler extends TestHandler<NewEvent> {
    }

    private class NewEvent extends Event {
        private static final long serialVersionUID = 1L;
    }

    private abstract class TestHandler<T extends Event> implements EventHandler<T>{

        private Boolean called;

        public TestHandler() {
            called = false;
        }

        @Override
        public void handle(T event) {
            called = true;
        }

        public Boolean wasCalled() {
            return called;
        }
    }
}