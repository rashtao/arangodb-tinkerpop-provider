package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.tinkerpop.gremlin.arangodb.ArangoDBSuite;
import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.junit.runner.RunWith;

@RunWith(ArangoDBSuite.class)
@GraphProviderClass(provider = ComplexGraphProvider.class, graph = ComplexTestGraph.class)
public class ComplexArangoDBSuiteTest {

}
