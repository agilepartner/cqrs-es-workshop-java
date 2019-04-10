# Step 06 : Materialized view, a.k.a. read models, a.k.a. projections

So fare we have mainly focused on the write side of CQRS. But what about the read side ? The main philosophy behind CQRS is to make the query side as fast and efficient as possible by denormalizing data as much as needed, to create what we call materialized views.

## Event handler

```Java
public interface EventHandler<T extends Event>  {
    public void handle(T event);
}
```

## Event resolver

```Java
public interface EventResolver {
    public <T extends Event> Iterable<EventHandler<T>> findHandlersFor(Class<?> evtClass);
    public <T extends Event> void register(EventHandler<T> handler, Class<?> evtClass);
}
```

## In-memory event resolver implementation

```Java

```

## Denormalizing InventoryItem events

```Java

```

## What's next

In the next step, we will...

* Go to [Step 07](../Step07/Step07.md)
* Go back to [Home](../README.md)