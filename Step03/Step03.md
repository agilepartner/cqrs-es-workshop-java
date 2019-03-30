# Step 03 : Implementing our domain

To continue this workshop, we will use a fairly naive domain : an inventory system. Our goal here is to understand how to write an event-sourced system, not to implement a complex domain.

## Discovering the domain with *Event Storming*

The best way to explore a domain is most definitely through [Event Storming](https://www.eventstorming.com/). Here, we will not enter into details on how to run an *Event Storming* session. There are many resources out there you can find on the subject; including [Alberto Brandolini's book](https://www.eventstorming.com/book/). This is not the point of this workshop. We do recommend that you get familiar with *Event Storming* though, since most of the *Domain-Driven Design* recognize it as **the best tool** for domain modeling.

Usually, the output of an *Event Storming* session would probably be photos of sticky notes. For this workshop, we will simulate an *Event Storming* and use pixels instead of actual sticky notes.

![Inventory Item Domain](files/InventoryItemDomain.jpg)

*Event Stormers* use some conventions regarding colors. It's not mandatory, but it's usually better to stick to this convention to make your model generally understandable by the community.

Colors are generally:

* Orange sticky : Event
* Blue sticky : Command
* Larger yellow sticky : Aggregate

We have talked about *event* and *aggregate* before, but what is a command ?

## Command and command handlers

A *command* is the way the user will interact with the system. It is an order given by the user to the system to execute some kind of process. The command will be handled by the system and applied to an aggregate, that will generate one or more events.

### Command

A `Command` is a special type of `Message` that represents a user decision, an action request sent to the system. It has a unique id and the system should take care of implementing [idempotence](https://en.wikipedia.org/wiki/Idempotence). The `id` will be useful for that.

A command will generally apply to a given *aggregate*, so it is safe to assume that there should be an `aggregateId`.

```Java
public abstract class Command implements Message {
    public UUID id;
    public UUID aggregateId;
```

### Command handler

A *command* should be handled by the system. We need to add the notion of a `CommandHandler`, that will be generic to handle any kind of command.

```Java
public interface CommandHandler<T extends Command> {
    public void handle(T command);
}
```

A `CommandHandler` will generally do three things:

1. Create or load the *aggregate*
2. Call a public method on the *aggregate*, using the parameters provided in the *command*, to trigger some kind of business behavior.
3. Save the *aggregate*.

To create the *aggregate*, we will need a *factory* that will ensure the *aggregate* is in a correct initial state. A [factory method](https://en.wikipedia.org/wiki/Factory_method_pattern) is quite useful for that. 

Loading and saving the *aggregate* is delegated to another component that is called a `Repository`.

### Repository

A `Repository` has two methods, one for loading, one for saving.

```Java
public interface Repository<T extends AggregateRoot> {
    public T getById(UUID id);
    public void save(T aggregate);
}
```

## Implementing the domain

Now that we have all we need, how would we move on implementing the domain. Well, let's start with implementing our first use case *Creation* and then we will move to the next.

The development cycle for each use case will mostly be the same:

1. Define the command
2. Define the domain event(s)
3. Add a new public method to the aggregate
4. Define the command handler
5. Write some tests

This flow would be slightly different if we were doing TDD, but for the sake of your understanding, let's not.

First, we need to create a new folder *domain* for our domain code *app/src/**main**/java/net/agilepartner/workshops/cqrs* and our tests *app/src/**test**/java/net/agilepartner/workshops/cqrs*

### Create inventory item

#### Create inventory item command

We need a *command* that will allow us to define the name of the inventory item and the initial quantity. The `aggregateId` will be randomly generated at this point, as well as the `id` of the *command* itself.

```Java
public class CreateInventoryItem extends Command {

    private static final long serialVersionUID = 1L;

    public String name;
    public int initialQuantity;

    public static CreateInventoryItem Create(String name, int initialQuantity) {
        CreateInventoryItem cmd = new CreateInventoryItem();
        cmd.id = UUID.randomUUID();
        cmd.aggregateId = UUID.randomUUID();
        cmd.name = name;
        cmd.initialQuantity = initialQuantity;

        return cmd;
    }
}
```

#### Create inventory item event

The domain event is pretty straight forward. It needs the `aggregateId`, a `name` and the `quantity`.

```Java
public class InventoryItemCreated extends Event {
    public final String name;
    public final int quantity;

    public InventoryItemCreated(UUID aggregateId, String name, int quantity) {
        this.aggregateId  = aggregateId;
        this.name = name;
        this.quantity = quantity;
    }
}
```

#### Create inventory item aggregate

To create the *aggregate*, we just need a constructor. No real need for a *factory method* here, because the *aggregate* is very simple and only composed of the `AggregateRoot` itself.

```Java
public class InventoryItem extends AggregateRoot {

    public InventoryItem(UUID aggregateId, String name, int quantity) {
        super(aggregateId);
        raise(new InventoryItemCreated(aggregateId, name, quantity));
    }
}
```

#### Create inventory item command handler

The *command handler* does only two things. First create the *aggregate*, second save it. To save the *aggregate*, we need a `Repository` that will be injected in the constructor of the *command handler* itself. We will see later on how to wire everything up.

```Java
public class CreateInventoryItemHandler implements CommandHandler<CreateInventoryItem> {

    private Repository<InventoryItem> repository;

    public CreateInventoryItemHandler(Repository<InventoryItem> repository) {
        this.repository = repository;
    } 

    public void handle(CreateInventoryItem command) {
        InventoryItem item = new InventoryItem(command.aggregateId, command.name, command.initialQuantity);
        repository.save(item);
    }
}
```

#### Test create inventory item

The test is also straight forward. We just created a new `Helper` class that defines a generic method to get all events of a specific type. We also refactored `AggregateRootTests` to use this *helper*.

```Java
@RunWith(SpringRunner.class)
public class InventoryItemTests {

    @Test
    public void createInventoryItem() {
        UUID aggregateId = UUID.randomUUID();
        String name = "My awesome item";
        int quantity = 5;
        InventoryItem item = new InventoryItem(aggregateId, name, quantity);

        ArrayList<InventoryItemCreated> events = Helper.getEvents(item, InventoryItemCreated.class);
        assertEquals(1, events.size());
        InventoryItemCreated evt = events.get(0);
        assertEquals(aggregateId, evt.aggregateId);
        assertEquals(name, evt.name);
        assertEquals(quantity, evt.quantity);
        assertEquals(1, evt.version);
    }
}
```

We can also implement a simple test for the *command handler*

```Java
@RunWith(SpringRunner.class)
public class InventoryItemTests {

    [...]

    @Test
    public void handleCreateInventoryItem() {
        CreateInventoryItemHandler handler = new CreateInventoryItemHandler(repository);
        CreateInventoryItem cmd = CreateInventoryItem.Create("Awesome name", 5);
        handler.handle(cmd);

        verify(repository).save(any());
    }
}
```

### Rename inventory item

From there, we move on to the *rename* use case.

#### Rename inventory item command

```Java
public class RenameInventoryItem extends Command {
    private static final long serialVersionUID = -5605660277028203474L;
    public String name;

    public static RenameInventoryItem Create(String name) {
        RenameInventoryItem cmd = new RenameInventoryItem();
        cmd.id = UUID.randomUUID();
        cmd.aggregateId = UUID.randomUUID();
        cmd.name = name;

        return cmd;
    }
}
```

#### Rename inventory item event

```Java
public class InventoryItemRenamed extends Event {
    private static final long serialVersionUID = 1L;
    public final String name;

    public InventoryItemRenamed(UUID aggregateId, String name) {
        this.aggregateId  = aggregateId;
        this.name = name;
    }
}
```

#### Rename inventory public method

Before we can rename an item, we need to check if the new name is valid (i.e. not null and not empty) and that it has actually changed. We don't want to raise an event if it hasn't. For that, we need to add some internal state in the aggregate to store the current value of the `name`.

Before we actually had a business rule checking the `name` value, we did not need to maintain the current state. Now we need to, means we need to implement the two extra `apply` methods, one for `InventoryItemCreated`, the other for `InventoryItemRenamed`.

```Java
public class InventoryItem extends AggregateRoot {
    private String name;

    public InventoryItem(UUID aggregateId, String name, int quantity) {
        super(aggregateId);
        raise(new InventoryItemCreated(aggregateId, name, quantity));
    }

    public void rename(String name) {
        Guards.checkNotNull(name);
        if (this.name != name)
            raise(new InventoryItemRenamed(id, name));
    }

    @SuppressWarnings("unused")
    private void apply(InventoryItemCreated evt) {
        this.name = evt.name;
    }

    @SuppressWarnings("unused")
    private void apply(InventoryItemRenamed evt) {
        this.name = evt.name;
    }
}
```

#### Rename inventory item command handler

The *command handler* does three things. It loads the inventory item, calls the public method `rename`, and save the item back.

```Java
public class RenameInventoryItemHandler implements CommandHandler<RenameInventoryItem> {
    private Repository<InventoryItem> repository;

    public RenameInventoryItemHandler(Repository<InventoryItem> repository) {
        this.repository = repository;
    }

    @Override
    public void handle(RenameInventoryItem command) {
        InventoryItem item = repository.getById(command.aggregateId);
        item.rename(command.name);
        repository.save(item);
    }
}
```

#### Rename inventory item tests

```Java

```

### Check inventory item in

```Java

```

### Check inventory item out

```Java

```

### Deactivate inventory item

```Java

```
