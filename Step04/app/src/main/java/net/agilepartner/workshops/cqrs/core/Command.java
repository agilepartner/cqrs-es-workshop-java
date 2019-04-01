package net.agilepartner.workshops.cqrs.core;

import java.util.UUID;

public abstract class Command implements Message {
    public UUID id;
    public UUID aggregateId;

    private static final long serialVersionUID = -840035726759327475L;

}