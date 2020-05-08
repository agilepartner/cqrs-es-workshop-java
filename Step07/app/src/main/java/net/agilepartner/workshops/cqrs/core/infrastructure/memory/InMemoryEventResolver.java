package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import java.util.*;
import java.util.concurrent.*;

import net.agilepartner.workshops.cqrs.core.*;
import net.agilepartner.workshops.cqrs.core.infrastructure.*;

public class InMemoryEventResolver implements EventResolver {
    
    private final Map<Class<? extends Event>, List<EventHandler<? extends Event>>> eventHandlers = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> Iterable<EventHandler<T>> findHandlersFor(Class<T> evtClass) {
        List<EventHandler<? extends Event>> handlers = eventHandlers.get(evtClass);
        if (handlers == null)
            throw new UnsupportedOperationException(String.format("No handlers defined for event %s", evtClass.getSimpleName()));

        List<EventHandler<T>> concreteHandlers = new ArrayList<>();
        handlers.forEach(handler -> concreteHandlers.add((EventHandler<T>) handler));

        return concreteHandlers;
    }

    @Override
    public <T extends Event> void register(EventHandler<T> handler, Class<T> evtClass) {
        eventHandlers.computeIfPresent(evtClass, (aClass, handlers) -> {
            handlers.add(handler);
            return handlers;
        });
        eventHandlers.computeIfAbsent(evtClass, aClass -> {
            List<EventHandler<? extends Event>> handlers = new ArrayList<>();
            handlers.add(handler);
            return handlers;
        });
    }
}