package net.agilepartner.workshops.cqrs.domain;

import java.util.UUID;

import net.agilepartner.workshops.cqrs.core.AggregateRoot;
import net.agilepartner.workshops.cqrs.core.Guards;

public class InventoryItem extends AggregateRoot {
    private String name;
    private int stock;

    private InventoryItem(UUID aggregateId, String name, int quantity) {
        super(aggregateId);
        raise(InventoryItemCreated.create(aggregateId, name, quantity));
    }

    public static InventoryItem create(UUID aggregateId, String name, int quantity) {
        return new InventoryItem(aggregateId, name, quantity);
    }

    public void rename(String name) {
        Guards.checkNotNullOrEmpty(name);
        if (this.name != name)
            raise(InventoryItemRenamed.create(id, name));
    }

    public void checkIn(int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        raise(InventoryItemCheckedIn.create(id, quantity));
    }

    public void checkOut(int quantity) throws NotEnoughStockException {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        if (this.stock < quantity)
            throw new NotEnoughStockException(String.format("Cannot check %d %s out because there is only %d left", quantity, name,this.stock));

        raise(InventoryItemCheckedOut.create(id, quantity));
    }

    @SuppressWarnings("unused")
    private void apply(InventoryItemCreated evt) {
        this.name = evt.name;
        this.stock = evt.quantity;
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
}