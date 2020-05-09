package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Event;

public class InventoryItemCheckedOut extends Event {
    private static final long serialVersionUID = 1L;
    private int quantity;

    public InventoryItemCheckedOut() {
    }

    public static InventoryItemCheckedOut create(UUID aggregateId, int quantity) {
        InventoryItemCheckedOut evt = new InventoryItemCheckedOut();
        evt.setAggregateId(aggregateId);
        evt.quantity = quantity;
        return evt;
    }

    public int getQuantity() {
        return quantity;
    }
}