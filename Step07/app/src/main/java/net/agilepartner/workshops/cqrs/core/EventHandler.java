package net.agilepartner.workshops.cqrs.core;

public interface EventHandler<T extends Event> {
    void handle(T event);
}