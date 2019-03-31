package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.Repository;

public class CreateInventoryItemHandler implements CommandHandler<CreateInventoryItem> {
    private Repository<InventoryItem> repository;

    public CreateInventoryItemHandler(Repository<InventoryItem> repository) {
        this.repository = repository;
    } 

    @Override
    public void handle(CreateInventoryItem command) {
        InventoryItem item = InventoryItem.create(command.aggregateId, command.name, command.initialQuantity);
        repository.save(item);
    }
}