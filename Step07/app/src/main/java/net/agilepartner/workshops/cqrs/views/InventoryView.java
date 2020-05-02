package net.agilepartner.workshops.cqrs.views;

import net.agilepartner.workshops.cqrs.core.EventHandler;
import net.agilepartner.workshops.cqrs.domain.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryView {
    private final Map<UUID, InventoryItemReadModel> inventory = new ConcurrentHashMap<>();

    public final EventHandler<InventoryItemCreated> createdHandler = event -> handle(event);
    public final EventHandler<InventoryItemRenamed> renamedHandler = event -> handle(event);
    public final EventHandler<InventoryItemCheckedIn> checkedInHandler = event -> handle(event);
    public final EventHandler<InventoryItemCheckedOut> checkedOutHandler = event -> handle(event);
    public final EventHandler<InventoryItemDeactivated> deactivatedHandler = event -> handle(event);

    public InventoryItemReadModel get(UUID aggregateId) {
        return inventory.get(aggregateId);
    }

    private void handle(InventoryItemCreated event) {
        InventoryItemReadModel item = new InventoryItemReadModel(event.name, event.quantity);
        inventory.put(event.aggregateId, item);
    }

    private void handle(InventoryItemRenamed event) {
        InventoryItemReadModel existingItem = inventory.get(event.aggregateId);
        inventory.put(event.aggregateId, new InventoryItemReadModel(event.name, existingItem.quantity));
    }

    private void handle(InventoryItemCheckedIn event) {
        InventoryItemReadModel existingItem = inventory.get(event.aggregateId);
        inventory.put(event.aggregateId, new InventoryItemReadModel(existingItem.name, existingItem.quantity + event.quantity));
    }

    private void handle(InventoryItemCheckedOut event) {
        InventoryItemReadModel existingItem = inventory.get(event.aggregateId);
        inventory.put(event.aggregateId, new InventoryItemReadModel(existingItem.name, existingItem.quantity - event.quantity));
    }

    private void handle(InventoryItemDeactivated event) {
        inventory.remove(event.aggregateId);
    }
}