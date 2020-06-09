package net.agilepartner.workshops.cqrs.core;

public interface CommandDispatcher {
    <T extends Command> void dispatch(T command) throws DomainException;
}