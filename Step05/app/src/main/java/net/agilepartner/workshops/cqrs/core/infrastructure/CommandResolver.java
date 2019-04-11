package net.agilepartner.workshops.cqrs.core.infrastructure;

import net.agilepartner.workshops.cqrs.core.Command;
import net.agilepartner.workshops.cqrs.core.CommandHandler;

public interface CommandResolver {
    public <T extends Command> CommandHandler<T> findHandlerFor(Class<?> cmdClass);
    public <T extends Command> void register(CommandHandler<T> handler, Class<?> cmdClass);
}
