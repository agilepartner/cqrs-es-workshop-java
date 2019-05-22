package net.agilepartner.workshops.cqrs.core.infrastructure;

import net.agilepartner.workshops.cqrs.core.Command;
import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.DomainException;
import net.agilepartner.workshops.cqrs.core.CommandDispatcher;
import net.agilepartner.workshops.cqrs.core.infrastructure.CommandResolver;

public class SimpleCommandDispatcher implements CommandDispatcher {
    private final CommandResolver resolver;

    public SimpleCommandDispatcher(CommandResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T extends Command> void dispatch(T command) throws DomainException {
        CommandHandler<T> handler = resolver.findHandlerFor(command.getClass());
        if (handler != null) {
            handler.handle(command);
        }
    }
}