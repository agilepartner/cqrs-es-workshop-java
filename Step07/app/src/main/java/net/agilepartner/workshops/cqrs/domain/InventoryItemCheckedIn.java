package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.agilepartner.workshops.cqrs.core.Event;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryItemCheckedIn extends Event {
    private static final long serialVersionUID = -8744398303363497614L;

    private int quantity;

    public static InventoryItemCheckedIn create(UUID aggregateId, int quantity) {
        InventoryItemCheckedIn evt = new InventoryItemCheckedIn();
        evt.setAggregateId(aggregateId);
        evt.quantity = quantity;
        return evt;
    }
}