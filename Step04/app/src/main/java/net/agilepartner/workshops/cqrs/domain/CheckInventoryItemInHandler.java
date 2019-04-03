package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.Repository;

public class CheckInventoryItemInHandler implements CommandHandler<CheckInventoryItemIn> {
    private Repository<InventoryItem> repository;

    public CheckInventoryItemInHandler(Repository<InventoryItem> repository) {
        this.repository = repository;
    } 

    @Override
    public void handle(CheckInventoryItemIn command) throws InventoryItemDeactivatedException  {
        InventoryItem item = repository.getById(command.aggregateId);
        item.checkIn(command.quantity);
        repository.save(item);
    }
}