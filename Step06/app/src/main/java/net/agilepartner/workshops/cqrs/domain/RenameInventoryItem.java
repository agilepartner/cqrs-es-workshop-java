package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.Command;

import java.util.UUID;

public class RenameInventoryItem extends Command {
    private static final long serialVersionUID = -5605660277028203474L;
    private String name;

    private RenameInventoryItem() {
    }

    public static RenameInventoryItem create(UUID aggregateId, String name) {
        RenameInventoryItem cmd = new RenameInventoryItem();
        cmd.setId(UUID.randomUUID());
        cmd.setAggregateId(aggregateId);
        cmd.name = name;

        return cmd;
    }

    public String getName() {
        return name;
    }
}