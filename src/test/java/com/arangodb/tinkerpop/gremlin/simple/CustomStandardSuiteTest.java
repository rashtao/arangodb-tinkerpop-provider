package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.tinkerpop.gremlin.TestGraph;
import com.arangodb.tinkerpop.gremlin.simple.custom.CustomStandardSuite;
import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.junit.runner.RunWith;

@RunWith(CustomStandardSuite.class)
@GraphProviderClass(provider = SimpleGraphProvider.class, graph = TestGraph.class)
public class CustomStandardSuiteTest {

}
