# Step 04 : Wire everything up

## Command dispatcher

In order to be able to handle all kind of commands, we will need a bit of infrastructure. This piece of infrastructure is called a *command dispatcher*. The `CommandDispatcher`'s responsibility is to delegate the handling of the command to the proper command handler.

```Java
public interface CommandDispatcher {
    <T extends Command> void dispatch(T command) throws DomainException;
}
```

The *command dispatcher* will need a *command resolver*. The `CommandResolver` will keep track of all the command handlers.

```Java
public interface CommandResolver {
    public <T extends Command> CommandHandler<T> findHandlersFor(Class<?> cmdClass);
    public <T extends Command> void register(CommandHandler<T> handler, Class<?> cmdClass);
}
```

Here is the concrete implementation of an in-memory command resolver. Pay attention here, that there should only be one handler for a given command. That is a best practice. 

The `InMemoryCommandResolver` is implemented as a singleton. We use a `ConcurrentHashMap` to make sure we are thread-safe.

```Java
public class InMemoryCommandResolver implements CommandResolver {
    private final static CommandResolver instance = new InMemoryCommandResolver();

    public static CommandResolver getInstance() {
        return instance;
    }

    private final ConcurrentHashMap<String, CommandHandler<?>>  map = new ConcurrentHashMap<String, CommandHandler<?>>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Command> CommandHandler<T> findHandlersFor(Class<?> cmdClass) {
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
```

The *dispatcher* uses the *resolver* to find the right *handler* and calls it.

```Java
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
```

We can easily test this implementation.

```Java
@RunWith(SpringRunner.class)
public class InMemoryCommandDispatcherTests {
    private Boolean handlerCalled;

    public class MyCommand extends Command {
        private static final long serialVersionUID = 7729006766074319990L;

        public MyCommand() {
        }
    }

    public class MyCommandHandler implements CommandHandler<MyCommand> {

        @Override
        public void handle(MyCommand command) throws DomainException {
            handlerCalled = true;
        }
    }

    @Test
    public void findHandlersForMyCommand() {
        CommandResolver resolver = InMemoryCommandResolver.getInstance();
        resolver.register(new MyCommandHandler(), MyCommand.class);

        CommandHandler<MyCommand> handler = resolver.findHandlersFor(MyCommand.class);
        assertNotNull(handler);
        try {
            handler.handle(new MyCommand());
        } catch (DomainException e) {
            Assert.fail("Should not have raised an exception");
        }
    }

    @Test
    public void dispatchMyCommand() {
        handlerCalled = false;
        CommandResolver resolver = InMemoryCommandResolver.getInstance();
        resolver.register(new MyCommandHandler(), MyCommand.class);

        InMemoryCommandDispatcher dispatcher = new InMemoryCommandDispatcher(resolver);

        try {
            dispatcher.dispatch(new MyCommand());
        } catch (DomainException e) {
            Assert.fail("Should not have raised an exception");
        }
        assertTrue(handlerCalled);
    }
}
```

Note that this is a very naive and simple implementation. Later in this workshop, we will implement a more robust dispatcher using Akka and Kafka.

## Repository

Now we have a way to wire all *command handler*s. However, we need one more thing before we can move on building a quick test app. All *command handler*s have a dependency on a `Repository` that has for the moment no concrete implementation. We need to solve that.

A repository has a clear responsibility to load and save an aggregate. For the moment, we can start with a simple in-memory implementation.

```Java
public class InMemoryRepository<T extends AggregateRoot> implements Repository<T> {
    private final ConcurrentHashMap<UUID, T> map = new ConcurrentHashMap<UUID, T>();

    @Override
    public T getById(UUID id) {
        return map.get((Object) id);
    }

    @Override
    public void save(T aggregate) {
        map.putIfAbsent(aggregate.getId(), aggregate);
    }
}
```

Note that we had to add `getId` on `AggregateRoot`. This does break encapsulation a little, but it's acceptable at this stage.

Here are the tests for the repository

```Java
@RunWith(SpringRunner.class)
public class InMemoryRepositoryTests {

    @Test
    public void getByIdDoesNotReturnValue() {
        InMemoryRepository<MyAggregate> repository = new InMemoryRepository<MyAggregate>();
        MyAggregate aggregate = repository.getById(UUID.randomUUID());
        assertNull(aggregate);
    }

    @Test
    public void saveAndGetByIdReturnsValue() {
        InMemoryRepository<MyAggregate> repository = new InMemoryRepository<MyAggregate>();
        UUID aggregateId = UUID.randomUUID();

        repository.save(new MyAggregate(aggregateId));
        MyAggregate aggregate = repository.getById(aggregateId);

        assertNotNull(aggregate);
        assertEquals(aggregateId, aggregate.getId());
    }
}
```

## Test app

```Java

```

## What's next

In the next step, we will ...

* Go to [Step 05](../Step05/Step05.md)
* Go back to [Home](../README.md)