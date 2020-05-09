package net.agilepartner.workshops.cqrs.views;

public class InventoryItemReadModel {
    private final String name;
    private final int quantity;

    public InventoryItemReadModel(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }
}
