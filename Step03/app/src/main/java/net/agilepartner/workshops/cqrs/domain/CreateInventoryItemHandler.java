package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.Repository;

public class CreateInventoryItemHandler implements CommandHandler<CreateInventoryItem> {

    private Repository<InventoryItem> repository;

    public CreateInventoryItemHandler(Repository<InventoryItem> repository) {
        this.repository = repository;
    } 

    public void handle(CreateInventoryItem command) {
        InventoryItem item = new InventoryItem(command.aggregateId, command.name, command.initialQuantity);
        repository.save(item);
    }
}