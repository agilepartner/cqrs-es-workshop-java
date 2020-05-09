package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.Event;

import java.util.UUID;

public class InventoryItemDeactivated extends Event {

    private static final long serialVersionUID = 1L;

    private InventoryItemDeactivated() {
    }

    public static InventoryItemDeactivated create(UUID aggregateId) {
        InventoryItemDeactivated evt = new InventoryItemDeactivated();
        evt.setAggregateId(aggregateId);
        return evt;
    }
}