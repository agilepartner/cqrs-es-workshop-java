package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.DomainException;

public class InventoryItemDeactivatedException extends DomainException {
    private static final long serialVersionUID = 5162367558570334467L;

    public InventoryItemDeactivatedException(String message) {
        super(message);
    }
}