package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.agilepartner.workshops.cqrs.core.Event;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryItemDeactivated extends Event {

    private static final long serialVersionUID = 1L;
    
    public static InventoryItemDeactivated create(UUID aggregateId) {
        InventoryItemDeactivated evt = new InventoryItemDeactivated();
        evt.setAggregateId(aggregateId);
        return evt;
    }
}