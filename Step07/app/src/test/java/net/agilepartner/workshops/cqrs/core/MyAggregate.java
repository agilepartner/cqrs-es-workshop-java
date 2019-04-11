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
        if (this.name != name) {
            raise(new NameChanged(id, name));
        }
    }

    @SuppressWarnings("unused")
    private void apply(NameChanged evt) {
        name = evt.name;
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