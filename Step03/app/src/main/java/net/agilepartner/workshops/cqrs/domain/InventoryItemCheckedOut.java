package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Event;

public class InventoryItemCheckedOut extends Event {
    private static final long serialVersionUID = 1L;
    public int quantity;

    public static InventoryItemCheckedOut create(UUID aggregateId, int quantity) {
        InventoryItemCheckedOut evt = new InventoryItemCheckedOut();
        evt.aggregateId = aggregateId;
        evt.quantity = quantity;
        return evt;
    }
}