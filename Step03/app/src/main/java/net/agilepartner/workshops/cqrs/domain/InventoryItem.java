package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.AggregateRoot;
import net.agilepartner.workshops.cqrs.core.Guards;

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