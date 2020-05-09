package net.agilepartner.workshops.cqrs.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AggregateRoot {
    private final List<Event> changes = new ArrayList<>();

    protected UUID id;
    protected int version;

    protected AggregateRoot(UUID id) {
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

    private void applyChange(Event event, boolean isNew) {
        apply(event);

        if (isNew) {
            version++;
            event.setVersion(version);
            changes.add(event);
        }
    }

    protected abstract <T extends Event> void apply(T event);
}