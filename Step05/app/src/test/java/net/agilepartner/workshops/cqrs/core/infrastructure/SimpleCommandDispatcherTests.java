package net.agilepartner.workshops.cqrs.core.infrastructure;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import net.agilepartner.workshops.cqrs.core.Command;
import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.DomainException;
import net.agilepartner.workshops.cqrs.core.infrastructure.CommandResolver;
import net.agilepartner.workshops.cqrs.core.infrastructure.memory.InMemoryCommandResolver;

public class SimpleCommandDispatcherTests {
    private Boolean handlerCalled;

    public class MyCommand extends Command {
        private static final long serialVersionUID = 7729006766074319990L;

        public MyCommand() {
        }
    }

    public class MyCommandHandler implements CommandHandler<MyCommand> {

        @Override
        public void handle(MyCommand command) throws DomainException {
            handlerCalled = true;
        }
        
    }

    @Test
    public void findHandlerForMyCommand() {
        CommandResolver resolver = InMemoryCommandResolver.getInstance();
        resolver.register(new MyCommandHandler(), MyCommand.class);

        CommandHandler<MyCommand> handler = resolver.findHandlerFor(MyCommand.class);
        assertNotNull(handler);
        try {
            handler.handle(new MyCommand());
        } catch (DomainException e) {
            Assert.fail("Should not have raised an exception");
        }
    }

    @Test
    public void dispatchMyCommand() {
        handlerCalled = false;
        CommandResolver resolver = InMemoryCommandResolver.getInstance();
        resolver.register(new MyCommandHandler(), MyCommand.class);

        SimpleCommandDispatcher dispatcher = new SimpleCommandDispatcher(resolver);

        try {
            dispatcher.dispatch(new MyCommand());
        } catch (DomainException e) {
            Assert.fail("Should not have raised an exception");
        }
        assertTrue(handlerCalled);
    }

}