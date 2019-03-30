package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.AggregateRoot;

public class InventoryItem extends AggregateRoot {

    public InventoryItem(UUID aggregateId, String name, int quantity) {
        super(aggregateId);
        raise(new InventoryItemCreated(aggregateId, name, quantity));
    }
}