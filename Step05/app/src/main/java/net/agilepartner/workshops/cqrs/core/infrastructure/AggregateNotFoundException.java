package net.agilepartner.workshops.cqrs.core.infrastructure;

import java.util.UUID;

public class AggregateNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -3750447531677604222L;

    public AggregateNotFoundException(UUID id) {
        super("Aggregate not found. Id=" + id.toString());
    }
}