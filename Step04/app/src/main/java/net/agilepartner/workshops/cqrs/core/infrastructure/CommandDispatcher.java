package net.agilepartner.workshops.cqrs.core.infrastructure;

import net.agilepartner.workshops.cqrs.core.Command;
import net.agilepartner.workshops.cqrs.core.DomainException;

public interface CommandDispatcher {
    <T extends Command> void dispatch(T command) throws DomainException;
}