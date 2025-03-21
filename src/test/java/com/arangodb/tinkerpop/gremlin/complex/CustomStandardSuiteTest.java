package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.tinkerpop.gremlin.TestGraph;
import com.arangodb.tinkerpop.gremlin.complex.custom.CustomStandardSuite;
import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.junit.runner.RunWith;

@RunWith(CustomStandardSuite.class)
@GraphProviderClass(provider = ComplexGraphProvider.class, graph = TestGraph.class)
public class CustomStandardSuiteTest {

}
