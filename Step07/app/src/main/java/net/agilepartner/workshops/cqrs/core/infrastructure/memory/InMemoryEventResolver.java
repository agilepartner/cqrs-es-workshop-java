package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import java.util.*;
import java.util.concurrent.*;

import net.agilepartner.workshops.cqrs.core.*;
import net.agilepartner.workshops.cqrs.core.infrastructure.*;

public class InMemoryEventResolver implements EventResolver {
    
    private final Map<String, List<EventHandler<? extends Event>>> eventHandlers = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> Iterable<EventHandler<T>> findHandlersFor(Class<?> evtClass) {
        List<EventHandler<?>> handlers = eventHandlers.get(evtClass.getSimpleName());
        if (handlers == null)
            throw new UnsupportedOperationException(String.format("No handlers defined for event %s", evtClass.getSimpleName()));

        List<EventHandler<T>> concreteHandlers = new ArrayList<>();
        for (EventHandler<?> handler : handlers) {
            concreteHandlers.add((EventHandler<T>) handler);
        }
        
        return concreteHandlers;
    }

    @Override
    public <T extends Event> void register(EventHandler<T> handler, Class<?> evtClass) {
        List<EventHandler<?>> handlers;
        if (eventHandlers.containsKey(evtClass.getSimpleName())) {
            handlers = eventHandlers.get(evtClass.getSimpleName());
        } else {
            handlers = new ArrayList<>();
            eventHandlers.put(evtClass.getSimpleName(), handlers);
        }
        handlers.add(handler);
    }
}