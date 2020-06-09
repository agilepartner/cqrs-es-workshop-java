package net.agilepartner.workshops.cqrs.core;

import java.util.*;
import java.util.function.Consumer;

public abstract class AggregateRoot {
    private final List<Event> changes = new ArrayList<>();

    protected Map<Class<? extends Event>, Consumer<? extends Event>> eventsConsumer = new HashMap<>();

    protected UUID id;
    protected int version;

    protected AggregateRoot(UUID id) {
        registerEventsConsumer();
        this.id = id;
    }

    public UUID getId() { return id; }

    public int getVersion() { return version; }

    public int getOriginalVersion() { return version - changes.size(); }

    public void markChangesAsCommitted() {
        changes.clear();
    }

    public final Iterable<? extends Event> getUncommittedChanges() {
        return changes;
    }

    public final void loadFromHistory(Iterable<? extends Event> history) {
        for (Event e : history) {
            if(version + 1 == e.getVersion()) {
                version = e.getVersion();
            }
            applyChange(e, false);
        }
    }

    protected void raise(Event event) {
        applyChange(event, true);
    }

    private <T extends Event> void applyChange(T event, boolean isNew) {
        eventsConsumer.entrySet().stream()
                .filter(entry -> entry.getKey() == event.getClass())
                .findFirst()
                .map(entry -> (Consumer<T>) entry.getValue())
                .ifPresent(consumer -> consumer.accept(event));


        if (isNew) {
            version++;
            event.setVersion(version);
            changes.add(event);
        }
    }

    protected abstract void registerEventsConsumer();

}