package net.agilepartner.workshops.cqrs.core;

import java.util.UUID;

public abstract class Event implements Message {
    private static final long serialVersionUID = 8922791526755347386L;

    private UUID aggregateId;
    private int version;

    public UUID getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}