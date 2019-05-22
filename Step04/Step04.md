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
    public <T extends Command> CommandHandler<T> findHandlerFor(Class<?> cmdClass);
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
```

The *dispatcher* uses the *resolver* to find the right *handler* and calls it.

```Java
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
```

We can easily test this implementation.

```Java
public class SimpleCommandDispatcherTests {
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
    public void findHandlerForMyCommand() {
        CommandResolver resolver = InMemoryCommandResolver.getInstance();
        resolver.register(new MyCommandHandler(), MyCommand.class);

        CommandHandler<MyCommand> handler = resolver.findHandlerFor(MyCommand.class);
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

        SimpleCommandDispatcher dispatcher = new SimpleCommandDispatcher(resolver);

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

## End to end tests

We have now the ability to write end to end tests

```Java
public class End2EndTests {

    @Test
    public void run() {
        Repository<InventoryItem> repository = new InMemoryRepository<InventoryItem>();

        CommandResolver resolver = InMemoryCommandResolver.getInstance();
        resolver.register(new CreateInventoryItemHandler(repository), CreateInventoryItem.class);
        resolver.register(new RenameInventoryItemHandler(repository), RenameInventoryItem.class);
        resolver.register(new CheckInventoryItemInHandler(repository), CheckInventoryItemIn.class);
        resolver.register(new CheckInventoryItemOutHandler(repository), CheckInventoryItemOut.class);
        resolver.register(new DeactivateInventoryItemHandler(repository), DeactivateInventoryItem.class);

        CommandDispatcher dispatcher = new SimpleCommandDispatcher(resolver);

        CreateInventoryItem createApple = CreateInventoryItem.create("Apple", 10);
        CreateInventoryItem createBanana = CreateInventoryItem.create("Banana", 7);
        CreateInventoryItem createOrange = CreateInventoryItem.create("Orange", 5);

        try {
            //Create fruits
            dispatcher.dispatch(createApple);
            dispatcher.dispatch(createBanana);
            dispatcher.dispatch(createOrange);

            //Check out
            dispatcher.dispatch(CheckInventoryItemOut.create(createApple.aggregateId, 5)); // 5 apples left
            dispatcher.dispatch(CheckInventoryItemOut.create(createBanana.aggregateId, 5)); // 2 bananas left
            dispatcher.dispatch(CheckInventoryItemOut.create(createOrange.aggregateId, 5)); // 0 oranges left

            //Checking out too many oranges
            try {
                dispatcher.dispatch(CheckInventoryItemOut.create(createOrange.aggregateId, 5)); // Cannot check more oranges out
                Assert.fail("Should have raised NotEnoughStockException");
            } catch (NotEnoughStockException ex) { }

            //Renaming orange to pear
            dispatcher.dispatch(RenameInventoryItem.create(createOrange.aggregateId, "Pear")); // 0 pears left

            //Resupplying bananas (everybody loves bananas)
            dispatcher.dispatch(CheckInventoryItemIn.create(createBanana.aggregateId, 3)); // 5 bananas left

            //Nobody wants apples anymore
            dispatcher.dispatch(DeactivateInventoryItem.create(createApple.aggregateId));  // apple item deactivated

            //Can't check in an item that is deactivated
            try {
                dispatcher.dispatch(CheckInventoryItemIn.create(createApple.aggregateId, 5));
                Assert.fail("Should not be able to check apples in because the item is deactivated");
            } catch (InventoryItemDeactivatedException ex) { }
        } catch (DomainException e) {
            Assert.fail("Should not have raised any exception");
        }
    }
}
```

## Conclusion

In this step, we have wired up our domain to our command handlers thanks to some infrastructure, mainly running in memory. In memory is ok for testing, but it's definitely not sufficient for production grade software. We will need something more robust. Concurrent hash map are great when you run in a single process, but we will need to handle concurrency better than that if we want our application to scale out.

With our current implementation, all the calls are synchronous. This means that the user has to wait until the whole process is over before we get the hand back to him/her. This is fine for our naive example where processing time is very short, but imagine what is would be with longer processes. We probably want to level the load and start implementing asynchronous processing for long running process.

But there is something even more disturbing. We are implementing a CQRS system. Where is the *query* side ?

## What's next

In the next step, we will tackle the read side of our system by implementing materialized views.

* Go to [Step 05](../Step05/Step05.md)
* Go back to [Home](../README.md)