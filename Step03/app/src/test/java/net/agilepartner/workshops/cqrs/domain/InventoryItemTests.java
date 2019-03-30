package net.agilepartner.workshops.cqrs.domain;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import net.agilepartner.workshops.cqrs.Helper;

@RunWith(SpringRunner.class)
public class InventoryItemTests {

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
}