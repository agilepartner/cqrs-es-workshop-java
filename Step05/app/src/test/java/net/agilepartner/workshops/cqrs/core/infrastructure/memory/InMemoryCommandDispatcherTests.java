package net.agilepartner.workshops.cqrs.core.infrastructure.memory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import net.agilepartner.workshops.cqrs.core.Command;
import net.agilepartner.workshops.cqrs.core.CommandHandler;
import net.agilepartner.workshops.cqrs.core.DomainException;
import net.agilepartner.workshops.cqrs.core.infrastructure.CommandResolver;

@RunWith(SpringRunner.class)
public class InMemoryCommandDispatcherTests {
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
    public void findHandlersForMyCommand() {
        CommandResolver resolver = InMemoryCommandResolver.getInstance();
        resolver.register(new MyCommandHandler(), MyCommand.class);

        CommandHandler<MyCommand> handler = resolver.findHandlersFor(MyCommand.class);
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

        InMemoryCommandDispatcher dispatcher = new InMemoryCommandDispatcher(resolver);

        try {
            dispatcher.dispatch(new MyCommand());
        } catch (DomainException e) {
            Assert.fail("Should not have raised an exception");
        }
        assertTrue(handlerCalled);
    }

}