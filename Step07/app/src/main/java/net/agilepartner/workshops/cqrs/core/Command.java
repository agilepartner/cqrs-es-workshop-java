package net.agilepartner.workshops.cqrs.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter(AccessLevel.PROTECTED)
public abstract class Command implements Message {
    private static final long serialVersionUID = -840035726759327475L;

    private UUID id;
    private UUID aggregateId;


}