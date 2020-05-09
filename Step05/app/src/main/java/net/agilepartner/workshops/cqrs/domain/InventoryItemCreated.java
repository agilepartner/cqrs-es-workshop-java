package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Event;

public class InventoryItemCreated extends Event {
    private static final long serialVersionUID = -5604800934233512172L;
    private String name;
    private int quantity;

    private InventoryItemCreated() {
    }

    public static InventoryItemCreated create(UUID aggregateId, String name, int quantity) {
        InventoryItemCreated evt = new InventoryItemCreated();
        evt.setAggregateId(aggregateId);
        evt.name = name;
        evt.quantity = quantity;
        return evt;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }
}