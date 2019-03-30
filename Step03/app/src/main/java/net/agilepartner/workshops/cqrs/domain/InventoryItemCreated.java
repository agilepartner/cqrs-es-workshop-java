package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Event;

public class InventoryItemCreated extends Event {
    private static final long serialVersionUID = -5604800934233512172L;
    public final String name;
    public final int quantity;

    public InventoryItemCreated(UUID aggregateId, String name, int quantity) {
        this.aggregateId  = aggregateId;
        this.name = name;
        this.quantity = quantity;
    }
}