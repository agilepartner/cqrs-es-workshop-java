package net.agilepartner.workshops.cqrs.core.infrastructure;

import net.agilepartner.workshops.cqrs.core.*;

public interface EventResolver {
    <T extends Event> Iterable<EventHandler<T>> findHandlersFor(Class<T> evtClass);
    <T extends Event> void register(EventHandler<T> handler, Class<T> evtClass);
}