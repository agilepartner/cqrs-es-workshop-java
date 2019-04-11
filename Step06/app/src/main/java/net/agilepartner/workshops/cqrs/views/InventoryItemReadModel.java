package net.agilepartner.workshops.cqrs.views;

public class InventoryItemReadModel {
    public final String name;
    public final int quantity;

    public InventoryItemReadModel(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}
