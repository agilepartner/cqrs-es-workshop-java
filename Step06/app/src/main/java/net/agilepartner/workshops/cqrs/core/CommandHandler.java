package net.agilepartner.workshops.cqrs.core;

public interface CommandHandler<T extends Command> {
    void handle(T command) throws DomainException;
}
