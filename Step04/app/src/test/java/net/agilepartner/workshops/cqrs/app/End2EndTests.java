package net.agilepartner.workshops.cqrs.app;

import org.junit.Assert;
import org.junit.Test;

import net.agilepartner.workshops.cqrs.core.*;
import net.agilepartner.workshops.cqrs.core.infrastructure.*;
import net.agilepartner.workshops.cqrs.core.infrastructure.memory.*;
import net.agilepartner.workshops.cqrs.domain.*;

public class End2EndTests {

    @Test
	public void run() {
		Repository<InventoryItem> repository = new InMemoryRepository<InventoryItem>();

		CommandResolver resolver = InMemoryCommandResolver.getInstance();
		resolver.register(new CreateInventoryItemHandler(repository), CreateInventoryItem.class);
		resolver.register(new RenameInventoryItemHandler(repository), RenameInventoryItem.class);
		resolver.register(new CheckInventoryItemInHandler(repository), CheckInventoryItemIn.class);
		resolver.register(new CheckInventoryItemOutHandler(repository), CheckInventoryItemOut.class);
		resolver.register(new DeactivateInventoryItemHandler(repository), DeactivateInventoryItem.class);

        CommandDispatcher dispatcher = new SimpleCommandDispatcher(resolver);
        
        CreateInventoryItem createApple = CreateInventoryItem.create("Apple", 10);
        CreateInventoryItem createBanana = CreateInventoryItem.create("Banana", 7);
        CreateInventoryItem createOrange = CreateInventoryItem.create("Orange", 5);

		try {
            //Create fruits
			dispatcher.dispatch(createApple);
			dispatcher.dispatch(createBanana);
            dispatcher.dispatch(createOrange);
            
            //Check out
			dispatcher.dispatch(CheckInventoryItemOut.create(createApple.getAggregateId(), 5)); // 5 apples left
			dispatcher.dispatch(CheckInventoryItemOut.create(createBanana.getAggregateId(), 5)); // 2 bananas left
            dispatcher.dispatch(CheckInventoryItemOut.create(createOrange.getAggregateId(), 5)); // 0 oranges left
            
            //Checking out too many oranges
            try {
                dispatcher.dispatch(CheckInventoryItemOut.create(createOrange.getAggregateId(), 5)); // Cannot check more oranges out
                Assert.fail("Should have raised NotEnoughStockException");
            } catch (NotEnoughStockException ex) { }

            //Renaming orange to pear
            dispatcher.dispatch(RenameInventoryItem.create(createOrange.getAggregateId(), "Pear")); // 0 pears left

            //Resupplying bananas (everybody loves bananas)
            dispatcher.dispatch(CheckInventoryItemIn.create(createBanana.getAggregateId(), 3)); // 5 bananas left

            //Nobody wants apples anymore
            dispatcher.dispatch(DeactivateInventoryItem.create(createApple.getAggregateId()));  // apple item deactivated

            //Can't check in an item that is deactivated
            try {
                dispatcher.dispatch(CheckInventoryItemIn.create(createApple.getAggregateId(), 5));
                Assert.fail("Should not be able to check apples in because the item is deactivated");
            } catch (InventoryItemDeactivatedException ex) { }
            
		} catch (DomainException e) {
            Assert.fail("Should not have raised any exception");
		}

	}
}