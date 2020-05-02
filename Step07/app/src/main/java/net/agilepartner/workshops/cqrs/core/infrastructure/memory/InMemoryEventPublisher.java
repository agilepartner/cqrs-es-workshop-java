package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import java.util.*;

import net.agilepartner.workshops.cqrs.core.*;
import net.agilepartner.workshops.cqrs.core.infrastructure.*;

public class InMemoryEventPublisher implements EventPublisher {
    private final EventResolver resolver;

    public InMemoryEventPublisher(EventResolver resolver) {
        this.resolver = resolver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> void publish(UUID aggregateId, T event) {
        resolver.findHandlersFor(event.getClass())
                .forEach(eventHandler -> ((EventHandler<T>) eventHandler).handle(event));
    }


}