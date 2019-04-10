package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Event;

public class InventoryItemRenamed extends Event {
    private static final long serialVersionUID = 1L;
    public String name;

    public static InventoryItemRenamed create(UUID aggregateId, String name) {
        InventoryItemRenamed evt = new InventoryItemRenamed();
        evt.aggregateId  = aggregateId;
        evt.name = name;
        return evt;
    }
}