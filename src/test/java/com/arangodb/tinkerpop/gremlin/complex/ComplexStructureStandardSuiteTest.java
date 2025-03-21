package com.arangodb.tinkerpop.gremlin.complex;

import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.apache.tinkerpop.gremlin.structure.StructureStandardSuite;
import org.junit.runner.RunWith;

@RunWith(StructureStandardSuite.class)
@GraphProviderClass(provider = ComplexGraphProvider.class, graph = ComplexTestGraph.class)
public class ComplexStructureStandardSuiteTest {

}
