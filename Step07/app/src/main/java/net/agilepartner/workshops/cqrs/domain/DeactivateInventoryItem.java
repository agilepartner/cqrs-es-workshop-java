package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.Command;

import java.util.UUID;

public class DeactivateInventoryItem extends Command {

    private static final long serialVersionUID = 1L;

    private DeactivateInventoryItem() {}

    public static DeactivateInventoryItem create(UUID aggregateId) {
        DeactivateInventoryItem cmd = new DeactivateInventoryItem();
        cmd.setId(UUID.randomUUID());
        cmd.setAggregateId(aggregateId);

        return cmd;
     }
}