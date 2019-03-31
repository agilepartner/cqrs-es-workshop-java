package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Command;

public class CheckInventoryItemIn extends Command {
    private static final long serialVersionUID = 1L;
    public int quantity;

    public static CheckInventoryItemIn create(UUID aggregateId, int quantity) {
        CheckInventoryItemIn cmd = new CheckInventoryItemIn();
        cmd.id = UUID.randomUUID();
        cmd.aggregateId = aggregateId;
        cmd.quantity = quantity;

        return cmd;
    }
}