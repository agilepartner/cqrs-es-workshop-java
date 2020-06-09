package net.agilepartner.workshops.cqrs.core.infrastructure;

import net.agilepartner.workshops.cqrs.core.Command;
import net.agilepartner.workshops.cqrs.core.CommandHandler;

public interface CommandResolver {
    <T extends Command> CommandHandler<T> findHandlerFor(Class<T> cmdClass);
    <T extends Command> void register(CommandHandler<T> handler, Class<T> cmdClass);
}
