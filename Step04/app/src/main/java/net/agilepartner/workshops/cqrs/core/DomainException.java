package net.agilepartner.workshops.cqrs.core;

public class DomainException extends Exception {

    private static final long serialVersionUID = 1L;

    public DomainException(String message) {
        super(message);
    }
}