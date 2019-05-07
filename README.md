# Agile Partner's CQRS / Event Sourcing workshop in Java

This code is a workshop to build a Domain-Driven Design / CQRS / Event Sourcing app in Java

## Requirements

* Java 8+
* Spring Boot
* Gradle
* Akka
* Apache Kafka
* Apache Kafka Streams
* GraphQL

## Recommended development environment

### Visual Studio Code

This workshop code was develop with [Visual Studio Code](https://code.visualstudio.com/).
We recommend you install [Java Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack). 

For more details, just follow the tutorial [Writing Java with Visual Studio Code](https://code.visualstudio.com/docs/java/java-tutorial).

## GitPod

If you don't want to install a code editor locally, you can also use [GitPod](https://www.gitpod.io/). Gitpod launches ready-to-code dev environments from any GitHub page. Simply go to the [GitPod App](https://gitpod.io/workspaces/) and start coding. You can also install the [GitPod Browser Extension](https://www.gitpod.io/docs/20_Browser_Extension/).

For more details, just follow the [Getting Startd](https://www.gitpod.io/docs/10_Getting_Started/).

## Get the code

To get the code, just clone this repository

``` bash
git clone https://github.com/agilepartner/cqrs-es-workshop-java.git
```

## Acknowledgement

The code for this workshop is inspired by the following repositories

* [cqrs-eventsourcing-kafka](https://github.com/vgoldin/cqrs-eventsourcing-kafka)
* [event-sourcing-cqrs-examples](https://github.com/andreschaffer/event-sourcing-cqrs-examples)

## Steps

* [Step 01 : Create an empty Spring Boot app](/Step01/Step01.md)
* [Step 02 : Create your first aggregate](/Step02/Step02.md)
* [Step 03 : Implementing our domain](/Step03/Step03.md)
* [Step 04 : Wire everything up](/Step04/Step04.md)
* [Step 05 : Persisting and publishing events](/Step05/Step05.md)
* [Step 06 : Materialized view (a.k.a. read models a.k.a. projections)](/Step06/Step06.md)
* [Step 07 : Define an API in GraphQL](/Step07/Step07.md)
* Step 08 : Create a simple front-end in Vue.js
* Step 09 : Become reactive with Websocket
* Step 10 : Concurrency with actors
* Step 11 : Introducing Kafka as messaging middleware
* Step 12 : React to events with a process manager
* Step 13 : Scaling out
