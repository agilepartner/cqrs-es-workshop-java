package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.Event;

import java.util.UUID;

public class InventoryItemCheckedIn extends Event {
    private static final long serialVersionUID = -8744398303363497614L;

    private int quantity;

    private InventoryItemCheckedIn() {}

    public static InventoryItemCheckedIn create(UUID aggregateId, int quantity) {
        InventoryItemCheckedIn evt = new InventoryItemCheckedIn();
        evt.setAggregateId(aggregateId);
        evt.quantity = quantity;
        return evt;
    }

    public int getQuantity() {
        return quantity;
    }
}