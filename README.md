# Agile Partner's CQRS / Event Sourcing workshop in Java

This code is a workshop to build a Domain-Driven Design / CQRS / Event Sourcing app in Java

## Requirements

* Java 8+
* Spring Boot
* Gradle
* Akka
* Apache Kafka
* Apache Kafka Streams

## Acknowledgement

The code for this workshop is inspired by the following repositories

* [cqrs-eventsourcing-kafka](https://github.com/vgoldin/cqrs-eventsourcing-kafka)
* [event-sourcing-cqrs-examples](https://github.com/andreschaffer/event-sourcing-cqrs-examples)

## Steps

* [Step 01 : Create an empty Spring Boot app](/Step01/Step01.md)
* [Step 02 : Create your first aggregate](/Step02/Step02.md)
* [Step 03 : Implementing our domain](/Step03/Step03.md)
* [Step 04 : Wire everything up](/Step04/Step04.md)
* Step 05 : Materialized view (a.k.a. read models a.k.a. projections)
* Step 06 : Concurrency with actors
* Step 07 : Scaling with Kafka
* Step 08 : React to events (process manager)
* Step 09 : Define an API
* Step 10 : Scale out