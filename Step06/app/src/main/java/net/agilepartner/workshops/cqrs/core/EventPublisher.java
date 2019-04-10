package net.agilepartner.workshops.cqrs.core;

import java.util.UUID;

public interface EventPublisher {
    <T extends Event> void publish(UUID aggregateId, T event);
}