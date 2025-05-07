package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.tinkerpop.gremlin.arangodb.complex.ComplexArangoDBSuite;
import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.junit.runner.RunWith;

@RunWith(ComplexArangoDBSuite.class)
@GraphProviderClass(provider = ComplexGraphProvider.class, graph = ComplexTestGraph.class)
public class ComplexArangoDBSuiteTest {

}
