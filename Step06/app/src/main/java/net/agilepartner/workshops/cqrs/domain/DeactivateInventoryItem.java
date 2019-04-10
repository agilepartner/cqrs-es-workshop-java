package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Command;

public class DeactivateInventoryItem extends Command {

    private static final long serialVersionUID = 1L;

    public static DeactivateInventoryItem create(UUID aggregateId) {
        DeactivateInventoryItem cmd = new DeactivateInventoryItem();
        cmd.id = UUID.randomUUID();
        cmd.aggregateId = aggregateId;

        return cmd;
     }
}