package com.arangodb.tinkerpop.gremlin.simple;

import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.apache.tinkerpop.gremlin.structure.StructureStandardSuite;
import org.junit.runner.RunWith;

@RunWith(StructureStandardSuite.class)
@GraphProviderClass(provider = SimpleGraphProvider.class, graph = SimpleTestGraph.class)
public class SimpleStructureStandardSuiteTest {

}
