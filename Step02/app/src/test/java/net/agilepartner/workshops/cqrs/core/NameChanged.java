package net.agilepartner.workshops.cqrs.core;

import java.util.UUID;

public class NameChanged extends Event {
    private static final long serialVersionUID = -1557213692423050873L;
    public final String name;

    public NameChanged(UUID aggregateId, String name) {
        super();
        super.aggregateId = aggregateId;
        this.name = name;
    }
}