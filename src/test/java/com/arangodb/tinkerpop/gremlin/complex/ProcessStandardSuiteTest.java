package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.tinkerpop.gremlin.TestGraph;
import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.apache.tinkerpop.gremlin.process.ProcessStandardSuite;
import org.junit.runner.RunWith;

@RunWith(ProcessStandardSuite.class)
@GraphProviderClass(provider = ComplexGraphProvider.class, graph = TestGraph.class)
public class ProcessStandardSuiteTest {

}
