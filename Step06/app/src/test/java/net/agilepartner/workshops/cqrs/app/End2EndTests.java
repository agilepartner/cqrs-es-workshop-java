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

    class Fruits {
        public static final String Apple = "Apple";
        public static final String Banana = "Banana";
        public static final String Orange = "Orange";
        public static final String Pear = "Pear";
    }

    @Test
	public void wireUpWithInMemoryRepository() {
        Repository<InventoryItem> repository = new InMemoryRepository<InventoryItem>();
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
        assertEquals(Fruits.Banana, bananas.name);
        assertEquals(5, bananas.quantity);
        InventoryItemReadModel oranges = view.get(orangeId);
        assertEquals(Fruits.Pear, oranges.name);
        assertEquals(0, oranges.quantity);
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
        Repository<InventoryItem> repository = new EventStoreAwareRepository<InventoryItem>(eventStore,
                aggregateId -> new InventoryItem(aggregateId));
        return repository;
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
        CreateInventoryItem createApple = CreateInventoryItem.create(Fruits.Apple, 10);
        appleId = createApple.aggregateId;
        CreateInventoryItem createBanana = CreateInventoryItem.create(Fruits.Banana, 7);
        bananaId = createBanana.aggregateId;
        CreateInventoryItem createOrange = CreateInventoryItem.create(Fruits.Orange, 5);
        orangeId = createOrange.aggregateId;

        try {
            // Create fruits
            dispatcher.dispatch(createApple);
            dispatcher.dispatch(createBanana);
            dispatcher.dispatch(createOrange);

            // Check out
            dispatcher.dispatch(CheckInventoryItemOut.create(createApple.aggregateId, 5)); // 5 apples left
            dispatcher.dispatch(CheckInventoryItemOut.create(createBanana.aggregateId, 5)); // 2 bananas left
            dispatcher.dispatch(CheckInventoryItemOut.create(createOrange.aggregateId, 5)); // 0 oranges left

            // Checking out too many oranges
            try {
                dispatcher.dispatch(CheckInventoryItemOut.create(createOrange.aggregateId, 5)); // Cannot check more
                                                                                                // oranges out
                Assert.fail("Should have raised NotEnoughStockException");
            } catch (NotEnoughStockException ex) {
            }

            // Renaming orange to pear
            dispatcher.dispatch(RenameInventoryItem.create(createOrange.aggregateId, "Pear")); // 0 pears left

            // Resupplying bananas (everybody loves bananas)
            dispatcher.dispatch(CheckInventoryItemIn.create(createBanana.aggregateId, 3)); // 5 bananas left

            // Nobody wants apples anymore
            dispatcher.dispatch(DeactivateInventoryItem.create(createApple.aggregateId)); // apple item deactivated

            // Can't check in an item that is deactivated
            try {
                dispatcher.dispatch(CheckInventoryItemIn.create(createApple.aggregateId, 5));
                Assert.fail("Should not be able to check apples in because the item is deactivated");
            } catch (InventoryItemDeactivatedException ex) {
            }

        } catch (DomainException e) {
            Assert.fail("Should not have raised any exception");
        }
    }
}