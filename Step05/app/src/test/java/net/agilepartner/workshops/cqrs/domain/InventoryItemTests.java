package net.agilepartner.workshops.cqrs.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import net.agilepartner.workshops.cqrs.Helper;
import net.agilepartner.workshops.cqrs.core.Repository;

@RunWith(SpringRunner.class)
public class InventoryItemTests {

    @Mock
    public Repository<InventoryItem> repository;

    @Test
    public void createInventoryItem() {
        UUID aggregateId = UUID.randomUUID();
        String name = "My awesome item";
        int quantity = 5;
        InventoryItem item = InventoryItem.create(aggregateId, name, quantity);

        ArrayList<InventoryItemCreated> events = Helper.getEvents(item, InventoryItemCreated.class);
        assertEquals(1, events.size());
        InventoryItemCreated evt = events.get(0);
        assertEquals(aggregateId, evt.aggregateId);
        assertEquals(name, evt.name);
        assertEquals(quantity, evt.quantity);
        assertEquals(1, evt.version);
    }

    @Test
    public void handleCreateInventoryItem() {
        CreateInventoryItemHandler handler = new CreateInventoryItemHandler(repository);
        CreateInventoryItem cmd = CreateInventoryItem.create("Awesome name", 5);
        handler.handle(cmd);

        verify(repository).save(any());
    }

    @Test
    public void renameInventoryItem() {
        UUID aggregateId = UUID.randomUUID();
        String name = "My awesome item";
        int quantity = 5;
        InventoryItem item = InventoryItem.create(aggregateId, name, quantity);
        String newName = "My even awesomer item";

        try {
            item.rename(newName);
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }

        ArrayList<InventoryItemRenamed> events = Helper.getEvents(item, InventoryItemRenamed.class);
        assertEquals(1, events.size());
        InventoryItemRenamed evt = events.get(0);
        assertEquals(aggregateId, evt.aggregateId);
        assertEquals(newName, evt.name);
        assertEquals(2, evt.version);
    }

    @Test()
    public void renameInventoryItemDoesNotApplyWhenSameName() {
        String name = "My awesome item";
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), name, 5);

        try {
            item.rename(name);
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }
        ArrayList<InventoryItemRenamed> events = Helper.getEvents(item, InventoryItemRenamed.class);
        assertEquals(0, events.size());
    }

    @Test(expected = NullPointerException.class)
    public void renameInventoryItemFailsBecauseNull() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), "My awesome item", 5);

        try {
            item.rename(null);
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void renameInventoryItemFailsBecauseEmpty() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), "My awesome item", 5);

        try {
            item.rename("");
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }
    }

    @Test
    public void handleRenameInventoryItem() {
        UUID aggregateId = UUID.randomUUID();
        RenameInventoryItemHandler handler = new RenameInventoryItemHandler(repository);
        RenameInventoryItem cmd = RenameInventoryItem.create(aggregateId, "Awesome name");
        InventoryItem item = InventoryItem.create(aggregateId, "Stupid name", 2);

        when(repository.getById(aggregateId)).thenReturn(item);

        try {
            handler.handle(cmd);
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }

        verify(repository).save(any());
    }

    @Test
    public void checkInventoryItemIn() {
        UUID aggregateId = UUID.randomUUID();
        String name = "My awesome item";
        int quantity = 5;
        InventoryItem item = InventoryItem.create(aggregateId, name, quantity);
        int checkedInQuantity = 2;

        try {
            item.checkIn(checkedInQuantity);
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }

        ArrayList<InventoryItemCheckedIn> events = Helper.getEvents(item, InventoryItemCheckedIn.class);
        assertEquals(1, events.size());
        InventoryItemCheckedIn evt = events.get(0);
        assertEquals(aggregateId, evt.aggregateId);
        assertEquals(checkedInQuantity, evt.quantity);
        assertEquals(2, evt.version);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkInventoryItemInFailsBecauseQuantityIsNotPositive() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), "My awesome item", 5);

        try {
            item.checkIn(-1);
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }
    }

    @Test
    public void handleCheckInventoryItemIn() {
        UUID aggregateId = UUID.randomUUID();
        CheckInventoryItemInHandler handler = new CheckInventoryItemInHandler(repository);
        CheckInventoryItemIn cmd = CheckInventoryItemIn.create(aggregateId, 2);
        InventoryItem item = InventoryItem.create(aggregateId, "My awesome item", 5);

        when(repository.getById(aggregateId)).thenReturn(item);

        try {
            handler.handle(cmd);
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }

        verify(repository).save(any());
    }

    @Test
    public void checkInventoryItemOut() {
        UUID aggregateId = UUID.randomUUID();
        String name = "My awesome item";
        int quantity = 5;
        InventoryItem item = InventoryItem.create(aggregateId, name, quantity);
        int checkedOutQuantity = 2;

        try {
            item.checkOut(checkedOutQuantity);
        } catch (NotEnoughStockException e) {
            Assert.fail("Should not have raised NotEnoughStockException");
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }

        ArrayList<InventoryItemCheckedOut> events = Helper.getEvents(item, InventoryItemCheckedOut.class);
        assertEquals(1, events.size());
        InventoryItemCheckedOut evt = events.get(0);
        assertEquals(aggregateId, evt.aggregateId);
        assertEquals(checkedOutQuantity, evt.quantity);
        assertEquals(2, evt.version);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkInventoryItemOutFailsBecauseQuantityIsNotPositive() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), "My awesome item", 5);

        try {
            item.checkOut(-1);
        } catch (NotEnoughStockException e) {
            Assert.fail("Should not have raised NotEnoughStockException");
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }
    }

    @Test
    public void checkInventoryItemOutFailsBecauseNotEnoughStock() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), "My awesome item", 5);

        try {
            item.checkOut(10);
            Assert.fail("Should have raised NotEnoughStockException");
        } catch (NotEnoughStockException e) {
            assertEquals("Cannot check 10 My awesome item out because there is only 5 left", e.getMessage());
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }
    }

    @Test
    public void handleCheckInventoryItemOut() {
        UUID aggregateId = UUID.randomUUID();
        CheckInventoryItemOutHandler handler = new CheckInventoryItemOutHandler(repository);
        CheckInventoryItemOut cmd = CheckInventoryItemOut.create(aggregateId, 2);
        InventoryItem item = InventoryItem.create(aggregateId, "My awesome item", 5);

        when(repository.getById(aggregateId)).thenReturn(item);

        try {
            handler.handle(cmd);
        } catch (NotEnoughStockException e) {
            Assert.fail("Should not have raised NotEnoughStockException");
        } catch (InventoryItemDeactivatedException e) {
            Assert.fail("Should not have raised InventoryItemDeactivatedException");
        }

        verify(repository).save(any());
    }

    @Test
    public void deactivateInventoryItem() {
        UUID aggregateId = UUID.randomUUID();
        String name = "My awesome item";
        int quantity = 5;
        InventoryItem item = InventoryItem.create(aggregateId, name, quantity);

        item.deactivate();

        ArrayList<InventoryItemDeactivated> events = Helper.getEvents(item, InventoryItemDeactivated.class);
        assertEquals(1, events.size());
        InventoryItemDeactivated evt = events.get(0);
        assertEquals(aggregateId, evt.aggregateId);
        assertEquals(2, evt.version);
    }

    @Test
    public void deactivateInventoryItemIsIdempotent() {
        UUID aggregateId = UUID.randomUUID();
        String name = "My awesome item";
        int quantity = 5;
        InventoryItem item = InventoryItem.create(aggregateId, name, quantity);

        item.deactivate();
        item.deactivate();

        ArrayList<InventoryItemDeactivated> events = Helper.getEvents(item, InventoryItemDeactivated.class);
        assertEquals(1, events.size());
        InventoryItemDeactivated evt = events.get(0);
        assertEquals(aggregateId, evt.aggregateId);
        assertEquals(2, evt.version);
    }

    @Test
    public void cannotDoAnythingWithDeactivatedInventoryItem() {
        UUID aggregateId = UUID.randomUUID();
        String name = "My awesome item";
        int quantity = 5;
        InventoryItem item = InventoryItem.create(aggregateId, name, quantity);

        item.deactivate();

        try {
            item.checkIn(5);
            Assert.fail("Should have raised InventoryItemDeactivatedException");
        } catch (InventoryItemDeactivatedException e) { }

        try {
            item.checkOut(5);
            Assert.fail("Should have raised InventoryItemDeactivatedException");
        } catch (InventoryItemDeactivatedException e)  { 
        } catch (NotEnoughStockException e) {
            Assert.fail("Should not have raised NotEnoughStockException");
		}

        try {
            item.rename("New name");
            Assert.fail("Should have raised InventoryItemDeactivatedException");
        } catch (InventoryItemDeactivatedException e) { }
    }

    @Test
    public void handleDeactivateInventoryItem() {
        UUID aggregateId = UUID.randomUUID();
        DeactivateInventoryItemHandler handler = new DeactivateInventoryItemHandler(repository);
        DeactivateInventoryItem cmd = DeactivateInventoryItem.create(aggregateId);
        InventoryItem item = InventoryItem.create(aggregateId, "My awesome item", 5);

        when(repository.getById(aggregateId)).thenReturn(item);

        handler.handle(cmd);

        verify(repository).save(any());
    }
}