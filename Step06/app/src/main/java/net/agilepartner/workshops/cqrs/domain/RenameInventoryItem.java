package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Command;

public class RenameInventoryItem extends Command {
    private static final long serialVersionUID = -5605660277028203474L;
    public String name;

    public static RenameInventoryItem create(UUID aggregateId, String name) {
        RenameInventoryItem cmd = new RenameInventoryItem();
        cmd.id = UUID.randomUUID();
        cmd.aggregateId = aggregateId;
        cmd.name = name;

        return cmd;
    }
}