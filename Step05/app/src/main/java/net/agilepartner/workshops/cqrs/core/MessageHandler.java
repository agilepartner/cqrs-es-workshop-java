package net.agilepartner.workshops.cqrs.core;

public interface MessageHandler<T extends Message> {
    public void handle(T action);
}