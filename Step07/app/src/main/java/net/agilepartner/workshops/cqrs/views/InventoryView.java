package net.agilepartner.workshops.cqrs.views;

import java.util.*;
import java.util.concurrent.*;

import net.agilepartner.workshops.cqrs.core.*;
import net.agilepartner.workshops.cqrs.domain.*;

public class InventoryView {
    private final Map<UUID, InventoryItemReadModel> inventory = new ConcurrentHashMap<>();

    public final InventoryItemCreatedHandler createdHandler = new InventoryItemCreatedHandler();
    public final InventoryItemRenamedHandler renamedHandler = new InventoryItemRenamedHandler();
    public final InventoryItemCheckedInHandler checkedInHandler = new InventoryItemCheckedInHandler();
    public final InventoryItemCheckedOutHandler checkedOutHandler = new InventoryItemCheckedOutHandler();
    public final InventoryItemDeactivatedHandler deactivatedHandler = new InventoryItemDeactivatedHandler();

    public InventoryItemReadModel get(UUID aggregateId) {
        return inventory.get(aggregateId);
    }

    class InventoryItemCreatedHandler implements EventHandler<InventoryItemCreated> {
        @Override
        public void handle(InventoryItemCreated event) {
            InventoryItemReadModel item = new InventoryItemReadModel(event.name, event.quantity);
            inventory.put(event.aggregateId, item);
        }
    }

    class InventoryItemRenamedHandler implements EventHandler<InventoryItemRenamed> {
        @Override
        public void handle(InventoryItemRenamed event) {
            InventoryItemReadModel existingItem = inventory.get(event.aggregateId);
            inventory.put(event.aggregateId, new InventoryItemReadModel(event.name, existingItem.quantity));
        }
    }

    class InventoryItemCheckedInHandler implements EventHandler<InventoryItemCheckedIn> {
        @Override
        public void handle(InventoryItemCheckedIn event) {
            InventoryItemReadModel existingItem = inventory.get(event.aggregateId);
            inventory.put(event.aggregateId, new InventoryItemReadModel(existingItem.name, existingItem.quantity + event.quantity));
        }
    }

    class InventoryItemCheckedOutHandler implements EventHandler<InventoryItemCheckedOut> {
        @Override
        public void handle(InventoryItemCheckedOut event) {
            InventoryItemReadModel existingItem = inventory.get(event.aggregateId);
            inventory.put(event.aggregateId, new InventoryItemReadModel(existingItem.name, existingItem.quantity - event.quantity));
        }
    }

    class InventoryItemDeactivatedHandler implements EventHandler<InventoryItemDeactivated> {
        @Override
        public void handle(InventoryItemDeactivated event) {
            inventory.remove(event.aggregateId);
        }
    }
}