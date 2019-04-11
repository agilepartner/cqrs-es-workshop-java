package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Event;

public class InventoryItemDeactivated extends Event {

    private static final long serialVersionUID = 1L;
    
    public static InventoryItemDeactivated create(UUID aggregateId) {
        InventoryItemDeactivated evt = new InventoryItemDeactivated();
        evt.aggregateId = aggregateId;
        return evt;
    }
}