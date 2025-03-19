package com.arangodb.tinkerpop.gremlin.simple;

import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.junit.runner.RunWith;

@RunWith(SimpleCustomStandardSuite.class)
@GraphProviderClass(provider = SimpleGraphProvider.class, graph = SimpleTestGraph.class)
public class SimpleCustomStandardSuiteTest {

}
