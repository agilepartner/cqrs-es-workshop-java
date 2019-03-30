package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Event;

public class InventoryItemRenamed extends Event {
    private static final long serialVersionUID = 1L;
    public final String name;

    public InventoryItemRenamed(UUID aggregateId, String name) {
        this.aggregateId  = aggregateId;
        this.name = name;
    }
}