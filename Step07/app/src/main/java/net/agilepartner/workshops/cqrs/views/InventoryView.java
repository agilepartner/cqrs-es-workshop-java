package net.agilepartner.workshops.cqrs.views;

import net.agilepartner.workshops.cqrs.core.EventHandler;
import net.agilepartner.workshops.cqrs.domain.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryView {
    private final Map<UUID, InventoryItemReadModel> inventory = new ConcurrentHashMap<>();

    public final EventHandler<InventoryItemCreated> createdHandler = this::handle;
    public final EventHandler<InventoryItemRenamed> renamedHandler = this::handle;
    public final EventHandler<InventoryItemCheckedIn> checkedInHandler = this::handle;
    public final EventHandler<InventoryItemCheckedOut> checkedOutHandler = this::handle;
    public final EventHandler<InventoryItemDeactivated> deactivatedHandler = this::handle;

    public InventoryItemReadModel get(UUID aggregateId) {
        return inventory.get(aggregateId);
    }

    private void handle(InventoryItemCreated event) {
        InventoryItemReadModel item = new InventoryItemReadModel(event.getName(), event.getQuantity());
        inventory.put(event.getAggregateId(), item);
    }

    private void handle(InventoryItemRenamed event) {
        InventoryItemReadModel existingItem = inventory.get(event.getAggregateId());
        inventory.put(event.getAggregateId(), new InventoryItemReadModel(event.getName(), existingItem.getQuantity()));
    }

    private void handle(InventoryItemCheckedIn event) {
        InventoryItemReadModel existingItem = inventory.get(event.getAggregateId());
        inventory.put(event.getAggregateId(), new InventoryItemReadModel(existingItem.getName(), existingItem.getQuantity() + event.getQuantity()));
    }

    private void handle(InventoryItemCheckedOut event) {
        InventoryItemReadModel existingItem = inventory.get(event.getAggregateId());
        inventory.put(event.getAggregateId(), new InventoryItemReadModel(existingItem.getName(), existingItem.getQuantity() - event.getQuantity()));
    }

    private void handle(InventoryItemDeactivated event) {
        inventory.remove(event.getAggregateId());
    }
}