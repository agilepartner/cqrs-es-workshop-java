package net.agilepartner.workshops.cqrs.app;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Event;
import net.agilepartner.workshops.cqrs.core.EventPublisher;

public class NoopPublisher implements EventPublisher {

    @Override
    public <T extends Event> void publish(UUID aggregateId, T event) {
    }
}