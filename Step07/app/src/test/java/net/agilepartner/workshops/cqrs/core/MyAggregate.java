package net.agilepartner.workshops.cqrs.core;

import java.util.UUID;
import java.util.function.Consumer;

public class MyAggregate extends AggregateRoot {
    private String name;

    public MyAggregate(UUID id, String name) {
        super(id);
        NameChanged evt = new NameChanged(id, name);
        raise(evt);
    }

    @Override
    protected void registerEventsConsumer() {
        eventsConsumer.put(NameChanged.class, (Consumer<NameChanged>) this::apply);
    }

    public void changeName(String name) {
        Guards.checkNotNull(name);
        if (!name.equals(this.name)) {
            raise(new NameChanged(id, name));
        }
    }

    private  void apply(NameChanged event) {
            name = event.name;
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