package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import net.agilepartner.workshops.cqrs.core.Command;
import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.DomainException;
import net.agilepartner.workshops.cqrs.core.infrastructure.CommandDispatcher;
import net.agilepartner.workshops.cqrs.core.infrastructure.CommandResolver;

public class InMemoryCommandDispatcher implements CommandDispatcher {
    private final CommandResolver resolver;

    public InMemoryCommandDispatcher(CommandResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T extends Command> void dispatch(T command) throws DomainException {
        CommandHandler<T> handler = resolver.findHandlersFor(command.getClass());
        if (handler != null) {
            handler.handle(command);
        }
    }
}