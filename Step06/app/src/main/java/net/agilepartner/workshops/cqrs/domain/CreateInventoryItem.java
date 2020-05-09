package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.Command;

import java.util.UUID;

public class CreateInventoryItem extends Command {

    private static final long serialVersionUID = 1L;
    
    private String name;
    private int initialQuantity;

    private CreateInventoryItem() {
    }

    public static CreateInventoryItem create(String name, int initialQuantity) {
        CreateInventoryItem cmd = new CreateInventoryItem();
        cmd.setId(UUID.randomUUID());
        cmd.setAggregateId(UUID.randomUUID());
        cmd.name = name;
        cmd.initialQuantity = initialQuantity;

        return cmd;
    }

    public String getName() {
        return name;
    }

    public int getInitialQuantity() {
        return initialQuantity;
    }
}