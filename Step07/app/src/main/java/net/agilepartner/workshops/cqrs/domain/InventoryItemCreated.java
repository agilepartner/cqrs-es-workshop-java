package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.agilepartner.workshops.cqrs.core.Event;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryItemCreated extends Event {
    private static final long serialVersionUID = -5604800934233512172L;

    private String name;
    private int quantity;

    public static InventoryItemCreated create(UUID aggregateId, String name, int quantity) {
        InventoryItemCreated evt = new InventoryItemCreated();
        evt.setAggregateId(aggregateId);
        evt.name = name;
        evt.quantity = quantity;
        return evt;
    }
}