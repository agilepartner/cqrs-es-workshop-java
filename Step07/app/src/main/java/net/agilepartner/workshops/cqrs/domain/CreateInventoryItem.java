package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.agilepartner.workshops.cqrs.core.Command;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateInventoryItem extends Command {

    private static final long serialVersionUID = 1L;

    private String name;
    private int initialQuantity;

    public static CreateInventoryItem create(String name, int initialQuantity) {
        CreateInventoryItem cmd = new CreateInventoryItem();
        cmd.setId(UUID.randomUUID());
        cmd.setAggregateId(UUID.randomUUID());
        cmd.name = name;
        cmd.initialQuantity = initialQuantity;

        return cmd;
    }
}