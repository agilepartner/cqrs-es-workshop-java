package net.agilepartner.workshops.cqrs.core;

public interface EventHandler<T extends Event> {
    public void handle(T event);
}