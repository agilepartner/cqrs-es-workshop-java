package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.AggregateRoot;
import net.agilepartner.workshops.cqrs.core.Event;
import net.agilepartner.workshops.cqrs.core.Guards;
import net.agilepartner.workshops.cqrs.core.infrastructure.UnsupportedEventException;

import java.util.UUID;

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
        if (!this.name.equals(name))
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
            throw new NotEnoughStockException(String.format("Cannot check %d %s out because there is only %d left", quantity, name, this.stock));

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

    @Override
    protected <T extends Event> void apply(T event) {
        if (event instanceof InventoryItemCreated) {
            InventoryItemCreated evt = (InventoryItemCreated) event;
            this.name = evt.getName();
            this.stock = evt.getQuantity();
            this.active = true;
        } else if (event instanceof InventoryItemRenamed) {
            InventoryItemRenamed evt = (InventoryItemRenamed) event;
            this.name = evt.getName();
        } else if (event instanceof InventoryItemCheckedIn) {
            InventoryItemCheckedIn evt = (InventoryItemCheckedIn) event;
            this.stock += evt.getQuantity();
        } else if (event instanceof InventoryItemCheckedOut) {
            InventoryItemCheckedOut evt = (InventoryItemCheckedOut) event;
            this.stock -= evt.getQuantity();
        } else if (event instanceof InventoryItemDeactivated) {
            this.active = false;
        } else {
            throw new UnsupportedEventException(event.getClass());
        }
    }

}