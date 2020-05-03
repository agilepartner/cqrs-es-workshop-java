package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.agilepartner.workshops.cqrs.core.Command;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckInventoryItemIn extends Command {
    private static final long serialVersionUID = 1L;

    private int quantity;

    public static CheckInventoryItemIn create(UUID aggregateId, int quantity) {
        CheckInventoryItemIn cmd = new CheckInventoryItemIn();
        cmd.setId(UUID.randomUUID());
        cmd.setAggregateId(aggregateId);
        cmd.quantity = quantity;

        return cmd;
    }
}