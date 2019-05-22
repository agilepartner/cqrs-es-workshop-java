# Step 07 : Define an API in GraphQL

To implement our GraphQL API, we are going to use [GraphQL Java](https://www.graphql-java.com) and [GraphQL Java Spring Boot](https://github.com/graphql-java/graphql-java-spring).

## Adding necessary dependencies

Open build.gradle and add some dependencies

```javascript
dependencies {
    implementation 'com.graphql-java:graphql-java:11.0'
    implementation "com.graphql-java:graphql-java-spring-boot-starter-webflux:1.0"
    implementation 'com.google.guava:guava:26.0-jre'

    [...]
}
```

Then add a file `schema.graphql` in `src/main/resources` with the following content:

```GraphQL
type InventoryItem {
  id: ID!
  name: String!
  quantity: Int!
}

type Inventory {
    items: [InventoryItem!]
}
```

## What's next

In the next step, we will...

* Go to [Step 08](../Step08/Step08.md)
* Go back to [Home](../README.md)