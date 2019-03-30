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
        InventoryItem item = new InventoryItem(aggregateId, name, quantity);

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
        InventoryItem item = new InventoryItem(aggregateId, name, quantity);
        String newName = "My even awesomer item";

        item.rename(newName);

        ArrayList<InventoryItemRenamed> events = Helper.getEvents(item, InventoryItemRenamed.class);
        assertEquals(1, events.size());
        InventoryItemRenamed evt = events.get(0);
        assertEquals(aggregateId, evt.aggregateId); 
        assertEquals(newName, evt.name); 
        assertEquals(2, evt.version); 
    }

    @Test
    public void handleRenameInventoryItem() {
        UUID aggregateId = UUID.randomUUID();
        RenameInventoryItemHandler handler = new RenameInventoryItemHandler(repository);
        RenameInventoryItem cmd = RenameInventoryItem.create(aggregateId, "Awesome name");
        InventoryItem item = new InventoryItem(aggregateId, "Stupid name", 2);

        when(repository.getById(aggregateId)).thenReturn(item);

        handler.handle(cmd);

        verify(repository).save(any());
    }
}