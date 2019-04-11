package net.agilepartner.workshops.cqrs.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.Test;

import net.agilepartner.workshops.cqrs.Helper;

public class AggregateRootTests {

	@Test
	public void createAggregate() {
		//Arrange
		UUID id = UUID.randomUUID();
		String name = "DDD rocks!";

		//Act
		MyAggregate aggregate = new MyAggregate(id, name);

		//Assert
		assertEquals(id, aggregate.id);
		ArrayList<NameChanged> events = Helper.getEvents(aggregate, NameChanged.class);
		assertEquals(events.size(), 1);
		NameChanged evt = events.get(0);
		assertEquals(id, evt.aggregateId);
		assertEquals(1, evt.version);
		assertEquals(name, evt.name);
	}

	@Test
	public void changeName(){
		//Arrange
		UUID id = UUID.randomUUID();
		String name = "CQRS/ES rocks even more!";
		MyAggregate aggregate = new MyAggregate(id, "DDD rocks!");

		//Act
		aggregate.changeName(name);

		//Assert
		ArrayList<NameChanged> events = Helper.getEvents(aggregate, NameChanged.class);
		assertEquals(events.size(), 2);
		NameChanged evt = events.get(1);
		assertEquals(id, evt.aggregateId);
		assertEquals(2, evt.version);
		assertEquals(name, evt.name);
	}

	@Test
	public void loadFromHistory() {
		//Arrange
		UUID id = UUID.randomUUID();
		String name1 = "DDD rocks!";
		String name2 = "CQRS/ES rocks even more!";

		ArrayList<NameChanged> history = new ArrayList<>();
		NameChanged evt1 = new NameChanged(id, name1);
		evt1.version = 1;
		NameChanged evt2 = new NameChanged(id, name2);
		evt2.version = 2;

		history.add(evt1);
		history.add(evt2);

		//Act
		MyAggregate aggregate = new MyAggregate(id);
		aggregate.loadFromHistory(history);

		//Assert
		ArrayList<NameChanged> events = Helper.getEvents(aggregate, NameChanged.class);
		assertEquals(events.size(), 0);
		assertEquals(2, aggregate.version);
		assertEquals(name2, aggregate.getName());
	}
}