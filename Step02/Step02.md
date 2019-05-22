# Step 02 : Create your first aggregate

Let's start by defining the classes and interfaces that will constitute the core of our CQRS / event-sourced system.

We are going to implement tests of course, but in this specific case, we are not going to drive the development through tests *aka TDD*, because it will be easier to explain the workshop by writing the tests afterwards.

Create a new folder *core* for the code *app/src/main/java/net/agilepartner/workshops/cqrs/core*

## Message

We start by defining a marker interface called `Message`

```Java
public interface Message extends Serializable {
}
```

## Event

An `Event` is a special type of `Message` that represents something that occurred in the system. An `Event` cannot be disproved and as such is always immutable.

```Java
public abstract class Event implements Message {
    public UUID aggregateId;
    public int version;
}
```

## Aggregate

The *aggregate* is a central notion in *Domain-Driven Design*. An *aggregate* is always accessed though to its `AggregateRoot`, which is the top level object in the cluster of objects that constitute the *aggregate*. The `AggregateRoot` is responsible for maintaining a valid state according to specific business rules.

In a event-sourced system, the state of the `AggregateRoot` is represented though a stream of `Event`s.

```Java
public abstract class AggregateRoot {
    private static final String APPLY_METHOD_NAME = "apply";
    private final List<Event> changes = new ArrayList<>();

    protected UUID id;
    protected int version;

    protected AggregateRoot() {
        this(UUID.randomUUID());
    }

    protected AggregateRoot(UUID id) {
        this.id = id;
    }

    public void markChangesAsCommitted() {
        changes.clear();
    }

    public final Iterable<? extends Event> getUncommittedChanges() {
        return changes;
    }

    public final void loadFromHistory(Iterable<? extends Event> history) {
        for (Event e : history) {
            if(version < e.version) {
                version = e.version;
            }
            applyChange(e, false);
        }
    }

    protected void raise(Event event) {
        applyChange(event, true);
    }

    private void applyChange(Event event, boolean isNew) {
        invokeApplyIfEntitySupports(event);

        if (isNew) {
            version++;
            event.version = version;
            changes.add(event);
        }
    }

    private void invokeApplyIfEntitySupports(Event event) {
        Class<?> eventType = nonAnonymous(event.getClass());
        try {
            Method method = this.getClass().getDeclaredMethod(APPLY_METHOD_NAME, eventType);
            method.setAccessible(true);
            method.invoke(this, event);
        } catch (SecurityException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex) {
            // Ugly exception swallowing. This should be logged somewhere
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> nonAnonymous(Class<T> cl) {
        return cl.isAnonymousClass() ? (Class<T>) cl.getSuperclass() : cl;
    }

}
```

## Testing the *AggregateRoot*

Create a new folder *core* for the tests *app/src/test/java/net/agilepartner/workshops/cqrs/core*

### Create an *event*

Let's start by defining a simple immutable event. An *event* should always be named with verb at the past tense. Obviously, it should extends the `Event` base class that we just created.

```Java
public class NameChanged extends Event {
    public final String name;

    public NameChanged(UUID aggregateId, String name) {
        super();
        super.aggregateId = aggregateId;
        this.name = name;
    }
}
```

### Create a test *aggregate*

We continue by implementing a simple *aggregate* that will only have an inner state with only one attribute called *name* as `String`. We will be able to create the *aggregate* and to call a public method called `changeName`.

```Java
public class MyAggregate extends AggregateRoot {
    private String name;

    public MyAggregate(UUID id, String name) {
        super(id);
        NameChanged evt = new NameChanged(id, name);
        raise(evt);
    }

    public void changeName(String name) {
        Guards.checkNotNull(name);
        if (this.name != name) {
            raise(new NameChanged(id, name));
        }
    }

    @SuppressWarnings("unused")
    private void apply(NameChanged evt) {
        name = evt.name;
    }

}
```

There are several things to notice here

* The *aggregate* only exposes behavior, in that case, the public constructor and the `changeName` method
* The state of the *aggregate* is well encapsulated and not exposed to the outside world
* The state represented by the attribute `name` is only used to test the rule that a name has changed **only** if it is not equal to the previous value. If this rule did not exist, we would not even need to maintain inner state. The state would only be maintained through *event*s
* The last thing a public method should do is `raise` an event
* The `apply` method is *private* and is in fact called through reflection by `invokeApplyIfEntitySupports` that is called in `AggregateRoot`
* The `apply` method is the only one actually changing the state of the *aggregate*
* The `apply` method will be called in the future when we reload the *aggregate* from persistence using the method `loadFromHistory`

### Test our aggregate

We can now test our aggregate to see if everything works fine. We can test the creation of the *aggregate* fist and then test the `changeName` method.

```Java
public class AggregateRootTests {

    @Test
    public void createAggregate() {
        //Arrange
        UUID id = UUID.randomUUID();
        String name = "DDD rocks!";

        //Act
        MyAggregate aggregate = new MyAggregate(id, name);

        //Assert
        assertEquals(id, aggregate.id);
        ArrayList<NameChanged> events = getEvents(aggregate);
        assertEquals(events.size(), 1);
        NameChanged evt = events.get(0);
        assertEquals(id, evt.aggregateId);
        assertEquals(1, evt.version);
        assertEquals(name, evt.name);
    }

    @Test
    public void changeName(){
        //Arrange
        UUID id = UUID.randomUUID();
        String name = "CQRS/ES rocks even more!";
        MyAggregate aggregate = new MyAggregate(id, "DDD rocks!");

        //Act
        aggregate.changeName(name);

        //Assert
        ArrayList<NameChanged> events = getEvents(aggregate);
        assertEquals(events.size(), 2);
        NameChanged evt = events.get(1);
        assertEquals(id, evt.aggregateId);
        assertEquals(2, evt.version);
        assertEquals(name, evt.name);
    }

    private ArrayList<NameChanged> getEvents(AggregateRoot root)
    {
        ArrayList<NameChanged> events = new ArrayList<NameChanged>();
        for (Event evt : root.getUncommittedChanges()) {
            if (evt instanceof NameChanged)
                events.add((NameChanged) evt);
        }
        return events;
    }
}
```

Notice how easy it is to test an *aggregate*. All you need is to setup the *aggregate* (Arrange) and call the public method (Act). You can then retrieve the list of generated *event*s to test against them (Assert). No need to break encapsulation by exposing inner state. All we need is *event*s. We had however to write a short helper method to retrieve the right type of *event*. This could be generalized into an helper class and made generic, so that it can be reused in future tests, but for the moment, let's keep it simple.

Tests are green. We are now happy.

But wait! There is no test for `loadFromHistory`. We need to implement a new test.

### Test for loading from history

At some point, we will have to load/restore our *aggregate* from some kind of persistence layer. In an event-sourced system, the way you restore the current state of an *aggregate* is by replaying all the events that have been generated since the beginning of the *aggregate* life cycle.

We can write this test by simulating *event*s as if the were loaded from a persistence layer.

```Java
public class AggregateRootTests {

    [...]

    @Test
    public void loadFromHistory() {
        //Arrange
        UUID id = UUID.randomUUID();
        String name1 = "DDD rocks!";
        String name2 = "CQRS/ES rocks even more!";

        ArrayList<NameChanged> history = new ArrayList<>();
        NameChanged evt1 = new NameChanged(id, name1);
        evt1.version = 1;
        NameChanged evt2 = new NameChanged(id, name2);
        evt2.version = 2;

        history.add(evt1);
        history.add(evt2);

        //Act
        MyAggregate aggregate = new MyAggregate(id);
        aggregate.loadFromHistory(history);

        //Assert
        ArrayList<NameChanged> events = getEvents(aggregate);
        assertEquals(events.size(), 0);
        assertEquals(2, aggregate.version);
        assertEquals(name2, aggregate.getName());
    }
}
```

Unfortunately here, we end up having to break our nice encapsulation just for testing purpose, because we need to call the constructor and get the inner state `name` to make sure everything went fine. This is something you would **never** do for production code. Proper encapsulation is the essence of Domain-Driven Design. But here, for testing purpose, we can live with that.

```Java
public class MyAggregate extends AggregateRoot {
    private String name;

    [...]

    //Only for testing purpose
    public MyAggregate(UUID id) {
        super(id);
    }

    //Only for testing purpose
    public String getName() {
        return name;
    }
}
```

An alternative would be to use reflection to invoke the protected constructor and then access the private member `name`, but once more, let's keep it simple.

Arguably, the method `loadFromHistory` could be placed somewhere else than in the `AggregateRoot`. Is that really the responsibility of the `AggregateRoot` to reload form its history ? In a production system, we would also probably check that we are processing events in the right order, by checking that the version numbers of the events are sequential and raising an exception in case they are not. Once again, this is just a workshop, so let's not get ahead of ourselves.

## What's next

In the next step, we will implement the Inventory Item domain.

* Go to [Step 03](../Step03/Step03.md)
* Go back to [Home](../README.md)