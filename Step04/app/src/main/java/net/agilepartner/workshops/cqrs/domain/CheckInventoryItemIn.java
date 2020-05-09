package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Command;

public class CheckInventoryItemIn extends Command {
    private static final long serialVersionUID = 1L;
    private int quantity;

    private CheckInventoryItemIn() {
    }

    public static CheckInventoryItemIn create(UUID aggregateId, int quantity) {
        CheckInventoryItemIn cmd = new CheckInventoryItemIn();
        cmd.setId(UUID.randomUUID());
        cmd.setAggregateId(aggregateId);
        cmd.quantity = quantity;

        return cmd;
    }

    public int getQuantity() {
        return quantity;
    }
}