package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.tinkerpop.gremlin.arangodb.simple.SimpleArangoDBSuite;
import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.junit.runner.RunWith;

@RunWith(SimpleArangoDBSuite.class)
@GraphProviderClass(provider = SimpleGraphProvider.class, graph = SimpleTestGraph.class)
public class SimpleArangoDBSuiteTest {

}
