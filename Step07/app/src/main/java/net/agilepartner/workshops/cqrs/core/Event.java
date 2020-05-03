package net.agilepartner.workshops.cqrs.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public abstract class Event implements Message {
    private static final long serialVersionUID = 8922791526755347386L;

    private UUID aggregateId;
    private int version;

}