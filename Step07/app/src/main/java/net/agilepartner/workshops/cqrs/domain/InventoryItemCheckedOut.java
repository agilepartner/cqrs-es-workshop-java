package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.Event;

import java.util.UUID;

public class InventoryItemCheckedOut extends Event {
    private static final long serialVersionUID = 1L;
    private int quantity;

    private InventoryItemCheckedOut() {}

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