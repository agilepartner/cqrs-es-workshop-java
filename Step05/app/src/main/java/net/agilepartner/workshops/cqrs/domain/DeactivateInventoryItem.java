package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Command;

public class DeactivateInventoryItem extends Command {

    private static final long serialVersionUID = 1L;

    private DeactivateInventoryItem() {
    }

    public static DeactivateInventoryItem create(UUID aggregateId) {
        DeactivateInventoryItem cmd = new DeactivateInventoryItem();
        cmd.setId(UUID.randomUUID());
        cmd.setAggregateId(aggregateId);

        return cmd;
     }
}