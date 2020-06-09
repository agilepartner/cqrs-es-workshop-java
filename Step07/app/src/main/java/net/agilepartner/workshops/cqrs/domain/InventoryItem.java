package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.AggregateRoot;
import net.agilepartner.workshops.cqrs.core.Guards;

import java.util.UUID;
import java.util.function.Consumer;

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

    @Override
    protected void registerEventsConsumer() {
        eventsConsumer.put(InventoryItemCreated.class, (Consumer<InventoryItemCreated>) this::apply);
        eventsConsumer.put(InventoryItemRenamed.class, (Consumer<InventoryItemRenamed>) this::apply);
        eventsConsumer.put(InventoryItemCheckedIn.class, (Consumer<InventoryItemCheckedIn>) this::apply);
        eventsConsumer.put(InventoryItemCheckedOut.class, (Consumer<InventoryItemCheckedOut>) this::apply);
        eventsConsumer.put(InventoryItemDeactivated.class, (Consumer<InventoryItemDeactivated>) this::apply);
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

    private void apply(InventoryItemCreated event) {
        this.name = event.getName();
        this.stock = event.getQuantity();
        this.active = true;
    }

    private void apply(InventoryItemRenamed event) {
        this.name = event.getName();
    }

    private void apply(InventoryItemCheckedIn event) {
        this.stock += event.getQuantity();
    }

    private void apply(InventoryItemCheckedOut event) {
        this.stock -= event.getQuantity();
    }

    private void apply(InventoryItemDeactivated event) {
        this.active = false;
    }

}