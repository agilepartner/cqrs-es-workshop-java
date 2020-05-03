package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.agilepartner.workshops.cqrs.core.Command;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeactivateInventoryItem extends Command {

    private static final long serialVersionUID = 1L;

    public static DeactivateInventoryItem create(UUID aggregateId) {
        DeactivateInventoryItem cmd = new DeactivateInventoryItem();
        cmd.setId(UUID.randomUUID());
        cmd.setAggregateId(aggregateId);

        return cmd;
     }
}