package net.agilepartner.workshops.cqrs.core;

import java.util.UUID;

public class MyAggregate extends AggregateRoot {
    private String name;

    public MyAggregate(UUID id, String name) {
        super(id);
        NameChanged evt = new NameChanged(id, name);
        raise(evt);
    }

    public void changeName(String name) {
        Guards.checkNotNull(name);
        if (!name.equals(this.name)) {
            raise(new NameChanged(id, name));
        }
    }

    @Override
    protected <T extends Event> void apply(T event) {
        if(event instanceof NameChanged) {
            NameChanged evt = (NameChanged) event;
            name = evt.getName();
        }
    }

    //Only for testing purpose
    public MyAggregate(UUID id) {
        super(id);
    }

    //Only for testing purpose
    public String getName() {
        return name;
    }

}