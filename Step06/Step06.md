# Step 06 : Materialized view, a.k.a. read models, a.k.a. projections

So fare we have mainly focused on the *write side* of CQRS, dealing with *commands*, *aggregates* and *event streams*. But what about the *read side* ?

The main idea behind CQRS is to make the *query/read side* as fast and efficient as possible by denormalizing data as much as needed, to create what we call *materialized views*. We also call these read models or projections. Because we know that, in average, 80% of requests on an application are reads and only 20% of writes, it makes sense to optimize for read.

To be able to react to events and denormalize the data they contain to create materialized views, we need some more infrastructure, starting with something like `EventHandler`.

## Event handler

An *event handler* is quite symmetrical to a *command handler*. Its role is to react to a specific type of event.

```Java
public interface EventHandler<T extends Event>  {
    public void handle(T event);
}
```

## Event resolver

Opposite to *command handlers*, where you can have only one handler for a given *command*, we actually want to give the possibility to have several *event handlers* for a given *event*.

```Java
public interface EventResolver {
    public <T extends Event> Iterable<EventHandler<T>> findHandlersFor(Class<?> evtClass);
    public <T extends Event> void register(EventHandler<T> handler, Class<?> evtClass);
}
```

Note that the return type of `findHandlersFor` returns an `Iterable`.

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