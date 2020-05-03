package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.agilepartner.workshops.cqrs.core.Event;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryItemRenamed extends Event {
    private static final long serialVersionUID = 1L;

    private String name;

    public static InventoryItemRenamed create(UUID aggregateId, String name) {
        InventoryItemRenamed evt = new InventoryItemRenamed();
        evt.setAggregateId(aggregateId);
        evt.name = name;
        return evt;
    }
}