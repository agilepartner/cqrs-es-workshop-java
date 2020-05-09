package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.Event;

import java.util.UUID;

public class InventoryItemRenamed extends Event {
    private static final long serialVersionUID = 1L;

    private String name;

    private InventoryItemRenamed() {
    }

    public String getName() {
        return name;
    }

    public static InventoryItemRenamed create(UUID aggregateId, String name) {
        InventoryItemRenamed evt = new InventoryItemRenamed();
        evt.setAggregateId(aggregateId);
        evt.name = name;
        return evt;
    }
}