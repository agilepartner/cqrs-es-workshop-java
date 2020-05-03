package net.agilepartner.workshops.cqrs.views;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InventoryItemReadModel {
    private final String name;
    private final int quantity;
}
