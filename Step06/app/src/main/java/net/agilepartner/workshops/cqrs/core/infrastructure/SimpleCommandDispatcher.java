package net.agilepartner.workshops.cqrs.core.infrastructure;

import net.agilepartner.workshops.cqrs.core.Command;
import net.agilepartner.workshops.cqrs.core.CommandDispatcher;
import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.DomainException;

public class SimpleCommandDispatcher implements CommandDispatcher {
    private final CommandResolver resolver;

    public SimpleCommandDispatcher(CommandResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T extends Command> void dispatch(T command) throws DomainException {
        @SuppressWarnings("unchecked")
        CommandHandler<T> handler = (CommandHandler<T>) resolver.findHandlerFor(command.getClass());
        handler.handle(command);
    }
}