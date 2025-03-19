package com.arangodb.tinkerpop.gremlin.simple;

import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.apache.tinkerpop.gremlin.process.ProcessStandardSuite;
import org.junit.runner.RunWith;

@RunWith(ProcessStandardSuite.class)
@GraphProviderClass(provider = SimpleGraphProvider.class, graph = SimpleTestGraph.class)
public class SimpleProcessStandardSuiteTest {

}
