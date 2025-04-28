![ArangoDB-Logo](https://docs.arangodb.com/assets/arangodb_logo_2016_inverted.png)

# arangodb-tinkerpop-provider

An implementation of
the [Apache TinkerPop OLTP Provider](https://tinkerpop.apache.org/docs/3.3.3/dev/provider/#_provider_documentation) API
for ArangoDB

## Compatibility

This Provider supports:

* Apache TinkerPop 3.7
* ArangoDB 3.11+ (via ArangoDB Java Driver 7.18+).

## Maven

To add the provider to your project via maven you need to add the following dependency (shown is the latest version -
you can replace the version with the one you need)

```XML

<dependencies>
    <dependency>
        <groupId>org.arangodb</groupId>
        <artifactId>arangodb-tinkerpop-provider</artifactId>
        <version>2.0.3</version>
    </dependency>
</dependencies>
```

## Configuration

Graph configuration properties are prefixed with `gremlin.arangodb.conf.graph`.

Driver configuration properties are prefixed with `gremlin.arangodb.conf.driver`.
All properties keys from `com.arangodb.config.ArangoConfigProperties` are supported.

Example:

```yaml
gremlin:
  graph: "com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph"
  arangodb:
    conf:
      graph:
        db: ArangoDBGraphConfigTest
        name: g
        type: COMPLEX
        orphanCollections: [ x, y, z ]
        edgeDefinitions:
          - "e1:[a]->[b]"
          - "e2:[b,c]->[e,f]"
      driver:
        hosts: [ "127.0.0.1:8529" ]
        password: test
```

`com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder` provides a 
convenient method for creating ArangoDB graph configurations.

## Using ArangoDBGraph via the TinkerPop API

This example is based on the TinkerPop
documentation ([Creating a graph](https://tinkerpop.apache.org/docs/3.7.3/tutorials/getting-started/#_creating_a_graph)):

```java
public static void main(String[] args) {
    Configuration conf = new ArangoDBConfigurationBuilder()
            .graph("modern")
            .hosts("localhost:8529")
            .user("root")
            .password("test")
            .build();

    Graph graph = GraphFactory.open(conf);
    GraphTraversalSource g = graph.traversal();

    // Add vertices
    Vertex v1 = g.addV("person")
            .property(T.id, "1")
            .property("name", "marko")
            .property("age", 29)
            .next();
    System.out.println("added vertex: " + v1);

    Vertex v2 = g.addV("software")
            .property(T.id, "3")
            .property("name", "lop")
            .property("lang", "java")
            .next();
    System.out.println("added vertex: " + v2);

    // Add edges
    Edge e1 = g.addE("created")
            .from(v1)
            .to(v2)
            .property(T.id, "9")
            .property("weight", 0.4)
            .next();
    System.out.println("added edge: " + e1);

    // Graph traversal
    // Find "marko" in the graph
    Vertex rv = g.V()
            .has("name", "marko")
            .next();
    System.out.println("found vertex: " + rv);

    // Walk along the "created" edges to "software" vertices
    Edge re = g.V()
            .has("name", "marko")
            .outE("created")
            .next();
    System.out.println("found edge: " + re);

    rv = g.V()
            .has("name", "marko")
            .outE("created")
            .inV()
            .next();
    System.out.println("found vertex: " + rv);

    rv = g.V()
            .has("name", "marko")
            .out("created")
            .next();
    System.out.println("found vertex: " + rv);

    // Select the "name" property of the "software" vertices
    String name = (String) g.V()
            .has("name", "marko")
            .out("created")
            .values("name")
            .next();
    System.out.println("name: " + name);

    // close the graph and the traversal source
    graph.close();
}
```

## Graph Type

TODO

## Element IDs

TODO
