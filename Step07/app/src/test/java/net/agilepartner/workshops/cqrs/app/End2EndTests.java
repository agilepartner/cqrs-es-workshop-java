package net.agilepartner.workshops.cqrs.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import net.agilepartner.workshops.cqrs.core.*;
import net.agilepartner.workshops.cqrs.core.infrastructure.*;
import net.agilepartner.workshops.cqrs.core.infrastructure.memory.*;
import net.agilepartner.workshops.cqrs.domain.*;
import net.agilepartner.workshops.cqrs.views.*;

public class End2EndTests {

    private UUID appleId;
    private UUID bananaId;
    private UUID orangeId;

    enum Fruits {
        APPLE("Apple"),
        BANANA("Banana"),
        ORANGE("Orange"),
        PEAR("Pear");

        private final String name;

        Fruits(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Test
	public void wireUpWithInMemoryRepository() {
        Repository<InventoryItem> repository = new InMemoryRepository<>();
        CommandDispatcher dispatcher = buildCommandDispatcher(repository);
		runEnd2EndTests(dispatcher);
    }

    @Test
    public void wireUpWithEventStoreAwareRepository() {
        EventPublisher eventPublisher = new NoopPublisher();
        Repository<InventoryItem> repository = buildRepository(eventPublisher);
        CommandDispatcher dispatcher = buildCommandDispatcher(repository);

        runEnd2EndTests(dispatcher);
    }

    @Test
    public void wireUpWithMaterializedView() {
        InventoryView view = new InventoryView();
        EventPublisher eventPublisher = buildEventPublisher(view);
        Repository<InventoryItem> repository = buildRepository(eventPublisher);
        CommandDispatcher dispatcher = buildCommandDispatcher(repository);

        runEnd2EndTests(dispatcher);

        InventoryItemReadModel apples = view.get(appleId);
        assertNull(apples);
        InventoryItemReadModel bananas = view.get(bananaId);
        assertEquals(Fruits.BANANA.getName(), bananas.getName());
        assertEquals(5, bananas.getQuantity());
        InventoryItemReadModel oranges = view.get(orangeId);
        assertEquals(Fruits.PEAR.getName(), oranges.getName());
        assertEquals(0, oranges.getQuantity());
    }

    private EventPublisher buildEventPublisher(InventoryView view) {
        EventResolver eventResolver = new InMemoryEventResolver();
        eventResolver.register(view.createdHandler, InventoryItemCreated.class);
        eventResolver.register(view.renamedHandler, InventoryItemRenamed.class);
        eventResolver.register(view.checkedInHandler, InventoryItemCheckedIn.class);
        eventResolver.register(view.checkedOutHandler, InventoryItemCheckedOut.class);
        eventResolver.register(view.deactivatedHandler, InventoryItemDeactivated.class);

        return new InMemoryEventPublisher(eventResolver);
    }

    private Repository<InventoryItem> buildRepository(EventPublisher publisher) {
        EventStore eventStore = new InMemoryEventStore(publisher);
        return new EventStoreAwareRepository<>(eventStore,
                InventoryItem::new);
    }

    private CommandDispatcher buildCommandDispatcher(Repository<InventoryItem> repository) {
        CommandResolver resolver = InMemoryCommandResolver.getInstance();
        resolver.register(new CreateInventoryItemHandler(repository), CreateInventoryItem.class);
        resolver.register(new RenameInventoryItemHandler(repository), RenameInventoryItem.class);
        resolver.register(new CheckInventoryItemInHandler(repository), CheckInventoryItemIn.class);
        resolver.register(new CheckInventoryItemOutHandler(repository), CheckInventoryItemOut.class);
        resolver.register(new DeactivateInventoryItemHandler(repository), DeactivateInventoryItem.class);

        return new SimpleCommandDispatcher(resolver);
    }

    private void runEnd2EndTests(CommandDispatcher dispatcher) {
        CreateInventoryItem createApple = CreateInventoryItem.create(Fruits.APPLE.getName(), 10);
        appleId = createApple.getAggregateId();
        CreateInventoryItem createBanana = CreateInventoryItem.create(Fruits.BANANA.getName(), 7);
        bananaId = createBanana.getAggregateId();
        CreateInventoryItem createOrange = CreateInventoryItem.create(Fruits.ORANGE.getName(), 5);
        orangeId = createOrange.getAggregateId();

        try {
            // Create fruits
            dispatcher.dispatch(createApple);
            dispatcher.dispatch(createBanana);
            dispatcher.dispatch(createOrange);

            // Check out
            dispatcher.dispatch(CheckInventoryItemOut.create(createApple.getAggregateId(), 5)); // 5 apples left
            dispatcher.dispatch(CheckInventoryItemOut.create(createBanana.getAggregateId(), 5)); // 2 bananas left
            dispatcher.dispatch(CheckInventoryItemOut.create(createOrange.getAggregateId(), 5)); // 0 oranges left

            // Checking out too many oranges
            try {
                dispatcher.dispatch(CheckInventoryItemOut.create(createOrange.getAggregateId(), 5)); // Cannot check more
                                                                                                // oranges out
                Assert.fail("Should have raised NotEnoughStockException");
            } catch (NotEnoughStockException ex) {
            }

            // Renaming orange to pear
            dispatcher.dispatch(RenameInventoryItem.create(createOrange.getAggregateId(), "Pear")); // 0 pears left

            // Resupplying bananas (everybody loves bananas)
            dispatcher.dispatch(CheckInventoryItemIn.create(createBanana.getAggregateId(), 3)); // 5 bananas left

            // Nobody wants apples anymore
            dispatcher.dispatch(DeactivateInventoryItem.create(createApple.getAggregateId())); // apple item deactivated

            // Can't check in an item that is deactivated
            try {
                dispatcher.dispatch(CheckInventoryItemIn.create(createApple.getAggregateId(), 5));
                Assert.fail("Should not be able to check apples in because the item is deactivated");
            } catch (InventoryItemDeactivatedException ex) {
            }

        } catch (DomainException e) {
            Assert.fail("Should not have raised any exception");
        }
    }
}