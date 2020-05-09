package net.agilepartner.workshops.cqrs.core;

import java.util.UUID;

public abstract class Command implements Message {
    private static final long serialVersionUID = -840035726759327475L;

    private UUID id;
    private UUID aggregateId;

    public UUID getId() {
        return id;
    }

    protected void setId(UUID id) {
        this.id = id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    protected void setAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }
}