package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.Command;

public class CreateInventoryItem extends Command {

    private static final long serialVersionUID = 1L;
    
    public String name;
    public int initialQuantity;

    public static CreateInventoryItem create(String name, int initialQuantity) {
        CreateInventoryItem cmd = new CreateInventoryItem();
        cmd.id = UUID.randomUUID();
        cmd.aggregateId = UUID.randomUUID();
        cmd.name = name;
        cmd.initialQuantity = initialQuantity;

        return cmd;
    }
}