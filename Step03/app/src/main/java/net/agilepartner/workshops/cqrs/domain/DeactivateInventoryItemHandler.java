package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.Repository;

public class DeactivateInventoryItemHandler implements CommandHandler<DeactivateInventoryItem> {
    private Repository<InventoryItem> repository;

    public DeactivateInventoryItemHandler(Repository<InventoryItem> repository) {
        this.repository = repository;
    } 

    @Override
    public void handle(DeactivateInventoryItem command) {
        InventoryItem item = repository.getById(command.aggregateId);
        item.deactivate();;
        repository.save(item);
    }
}