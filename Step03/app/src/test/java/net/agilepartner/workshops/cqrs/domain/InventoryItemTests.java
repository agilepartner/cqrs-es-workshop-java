package net.agilepartner.workshops.cqrs.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

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

        item.rename(newName);

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

        item.rename(name);
        ArrayList<InventoryItemRenamed> events = Helper.getEvents(item, InventoryItemRenamed.class);
        assertEquals(0, events.size());
    }

    @Test(expected=NullPointerException.class)
    public void renameInventoryItemFailsBecauseNull() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), "My awesome item", 5);

        item.rename(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void renameInventoryItemFailsBecauseEmpty() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), "My awesome item", 5);

        item.rename("");
    }

    @Test
    public void handleRenameInventoryItem() {
        UUID aggregateId = UUID.randomUUID();
        RenameInventoryItemHandler handler = new RenameInventoryItemHandler(repository);
        RenameInventoryItem cmd = RenameInventoryItem.create(aggregateId, "Awesome name");
        InventoryItem item = InventoryItem.create(aggregateId, "Stupid name", 2);

        when(repository.getById(aggregateId)).thenReturn(item);

        handler.handle(cmd);

        verify(repository).save(any());
    }

    @Test
    public void checkInventoryItemIn() {
        UUID aggregateId = UUID.randomUUID();
        String name = "My awesome item";
        int quantity = 5;
        InventoryItem item = InventoryItem.create(aggregateId, name, quantity);
        int checkedInQuantity = 2;

        item.checkIn(checkedInQuantity);

        ArrayList<InventoryItemCheckedIn> events = Helper.getEvents(item, InventoryItemCheckedIn.class);
        assertEquals(1, events.size());
        InventoryItemCheckedIn evt = events.get(0);
        assertEquals(aggregateId, evt.aggregateId); 
        assertEquals(checkedInQuantity, evt.quantity); 
        assertEquals(2, evt.version); 
    }

    @Test(expected=IllegalArgumentException.class)
    public void checkInventoryItemInFailsBecauseQuantityIsNotPositive() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), "My awesome item", 5);

        item.checkIn(-1);
    }

    @Test
    public void handleCheckInventoryItemIn() {
        UUID aggregateId = UUID.randomUUID();
        CheckInventoryItemInHandler handler = new CheckInventoryItemInHandler(repository);
        CheckInventoryItemIn cmd = CheckInventoryItemIn.create(aggregateId, 2);
        InventoryItem item = InventoryItem.create(aggregateId, "My awesome item", 5);

        when(repository.getById(aggregateId)).thenReturn(item);

        handler.handle(cmd);

        verify(repository).save(any());
    }
}