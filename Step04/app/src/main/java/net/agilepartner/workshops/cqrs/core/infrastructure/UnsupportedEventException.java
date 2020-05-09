package net.agilepartner.workshops.cqrs.core.infrastructure;

import net.agilepartner.workshops.cqrs.core.Event;

public class UnsupportedEventException extends RuntimeException {

    public UnsupportedEventException(Class< ? extends Event> eventType) {
        super("Unsupported event " + eventType.getSimpleName());
    }
}
