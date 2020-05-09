package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.agilepartner.workshops.cqrs.core.Command;
import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.infrastructure.CommandResolver;

public class InMemoryCommandResolver implements CommandResolver {
    private final static CommandResolver instance = new InMemoryCommandResolver();

    public static CommandResolver getInstance() {
        return instance;
    }

    private final Map<Class<? extends Command>, CommandHandler<? extends Command>> map = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Command> CommandHandler<T> findHandlerFor(Class<T> cmdClass) {
        CommandHandler<T> handler = (CommandHandler<T>) map.get(cmdClass);
        if (handler == null)
            throw new UnsupportedOperationException(String.format("No handler defined for command %s", cmdClass.getSimpleName()));

        return handler;
    }

    @Override
    public <T extends Command> void register(CommandHandler<T> handler, Class<T> cmdClass) {
        map.put(cmdClass, handler);
    }
}