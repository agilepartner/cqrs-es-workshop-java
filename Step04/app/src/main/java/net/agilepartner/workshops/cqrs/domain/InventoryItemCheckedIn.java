package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Event;

public class InventoryItemCheckedIn extends Event {
    private static final long serialVersionUID = -8744398303363497614L;
    public int quantity;

    public static InventoryItemCheckedIn create(UUID aggregateId, int quantity) {
        InventoryItemCheckedIn evt = new InventoryItemCheckedIn();
        evt.aggregateId = aggregateId;
        evt.quantity = quantity;
        return evt;
    }
}