package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.Repository;

public class CheckInventoryItemOutHandler implements CommandHandler<CheckInventoryItemOut> {
    private Repository<InventoryItem> repository;

    public CheckInventoryItemOutHandler(Repository<InventoryItem> repository) {
        this.repository = repository;
    }

    @Override
    public void handle(CheckInventoryItemOut command) throws NotEnoughStockException, InventoryItemDeactivatedException  {
        InventoryItem item = repository.getById(command.aggregateId);
        item.checkOut(command.quantity);
        repository.save(item);
    }
}