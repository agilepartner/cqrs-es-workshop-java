package net.agilepartner.workshops.cqrs.domain;

import net.agilepartner.workshops.cqrs.core.DomainException;

public class NotEnoughStockException extends DomainException {

    private static final long serialVersionUID = -6578758996745570912L;

    public NotEnoughStockException(String message) {
        super(message);
    }
}