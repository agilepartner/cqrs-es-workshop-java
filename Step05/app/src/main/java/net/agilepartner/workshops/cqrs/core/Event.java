package net.agilepartner.workshops.cqrs.core;

import java.util.UUID;

public abstract class Event implements Message {
    public UUID aggregateId;
    public int version;

    private static final long serialVersionUID = 8922791526755347386L;
}