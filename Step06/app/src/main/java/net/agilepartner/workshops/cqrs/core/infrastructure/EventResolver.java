package net.agilepartner.workshops.cqrs.core.infrastructure;

import net.agilepartner.workshops.cqrs.core.*;

public interface EventResolver {
    public <T extends Event> Iterable<EventHandler<T>> findHandlersFor(Class<?> evtClass);
    public <T extends Event> void register(EventHandler<T> handler, Class<?> evtClass);
}