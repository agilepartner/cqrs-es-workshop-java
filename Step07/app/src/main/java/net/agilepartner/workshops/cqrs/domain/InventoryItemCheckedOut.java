package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.agilepartner.workshops.cqrs.core.Event;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryItemCheckedOut extends Event {
    private static final long serialVersionUID = 1L;
    private int quantity;

    public static InventoryItemCheckedOut create(UUID aggregateId, int quantity) {
        InventoryItemCheckedOut evt = new InventoryItemCheckedOut();
        evt.setAggregateId(aggregateId);
        evt.quantity = quantity;
        return evt;
    }
}