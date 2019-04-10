package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.AggregateRoot;
import net.agilepartner.workshops.cqrs.core.Guards;

public class InventoryItem extends AggregateRoot {
    private String name;
    private int stock;
    private Boolean active;
    
    public InventoryItem(UUID aggregateId) {
        super(aggregateId);
    }

    private InventoryItem(UUID aggregateId, String name, int quantity) {
        super(aggregateId);
        raise(InventoryItemCreated.create(aggregateId, name, quantity));
    }

    public static InventoryItem create(UUID aggregateId, String name, int quantity) {
        return new InventoryItem(aggregateId, name, quantity);
    }

    public void rename(String name) throws InventoryItemDeactivatedException {
        checkActivated();
        Guards.checkNotNullOrEmpty(name);
        if (this.name != name)
            raise(InventoryItemRenamed.create(id, name));
    }

    public void checkIn(int quantity) throws InventoryItemDeactivatedException {
        checkActivated();
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        raise(InventoryItemCheckedIn.create(id, quantity));
    }

    public void checkOut(int quantity) throws NotEnoughStockException, InventoryItemDeactivatedException {
        checkActivated();
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        if (this.stock < quantity)
            throw new NotEnoughStockException(String.format("Cannot check %d %s out because there is only %d left", quantity, name,this.stock));

        raise(InventoryItemCheckedOut.create(id, quantity));
    }

    public void deactivate() {
        if (active)
            raise(InventoryItemDeactivated.create(id));
    }

    private void checkActivated() throws InventoryItemDeactivatedException {
        if (!active)
            throw new InventoryItemDeactivatedException(String.format("Inventory Item %s (id %s) is deactivated", name, id.toString()));
    }

    @SuppressWarnings("unused")
    private void apply(InventoryItemCreated evt) {
        this.name = evt.name;
        this.stock = evt.quantity;
        this.active = true;
    }

    @SuppressWarnings("unused")
    private void apply(InventoryItemRenamed evt) {
        this.name = evt.name;
    }

    @SuppressWarnings("unused")
    private void apply(InventoryItemCheckedIn evt) {
        this.stock += evt.quantity;
    }

    @SuppressWarnings("unused")
    private void apply(InventoryItemCheckedOut evt) {
        this.stock -= evt.quantity;
    }

    @SuppressWarnings("unused")
    private void apply(InventoryItemDeactivated evt) {
        this.active = false;
    }
}