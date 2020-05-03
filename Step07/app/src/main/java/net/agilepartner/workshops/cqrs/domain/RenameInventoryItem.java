package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.agilepartner.workshops.cqrs.core.Command;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameInventoryItem extends Command {
    private static final long serialVersionUID = -5605660277028203474L;

    private String name;

    public static RenameInventoryItem create(UUID aggregateId, String name) {
        RenameInventoryItem cmd = new RenameInventoryItem();
        cmd.setId(UUID.randomUUID());
        cmd.setAggregateId(aggregateId);
        cmd.name = name;

        return cmd;
    }
}