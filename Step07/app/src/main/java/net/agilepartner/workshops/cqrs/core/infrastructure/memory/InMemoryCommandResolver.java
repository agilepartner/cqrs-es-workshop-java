package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import java.util.concurrent.ConcurrentHashMap;

import net.agilepartner.workshops.cqrs.core.Command;
import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.infrastructure.CommandResolver;

public class InMemoryCommandResolver implements CommandResolver {
    private final static CommandResolver instance = new InMemoryCommandResolver();

    public static CommandResolver getInstance() {
        return instance;
    }

    private final ConcurrentHashMap<String, CommandHandler<?>>  map = new ConcurrentHashMap<String, CommandHandler<?>>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Command> CommandHandler<T> findHandlerFor(Class<?> cmdClass) {
        CommandHandler<?> handler = map.get((Object) cmdClass.getSimpleName());
        if (handler == null)
            throw new UnsupportedOperationException(String.format("No handler defined for command %s", cmdClass.getSimpleName()));

        return (CommandHandler<T>) handler;
    }

    @Override
    public <T extends Command> void register(CommandHandler<T> handler, Class<?> cmdClass) {
        map.put(cmdClass.getSimpleName(), handler);
    }
}