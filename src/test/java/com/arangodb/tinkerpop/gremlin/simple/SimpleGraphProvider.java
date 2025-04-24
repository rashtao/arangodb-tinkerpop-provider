package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.tinkerpop.gremlin.TestGraphProvider;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;

public class SimpleGraphProvider extends TestGraphProvider {

    @Override
    protected void customizeBuilder(ArangoDBConfigurationBuilder builder) {
        builder
                .graphType(ArangoDBGraphConfig.GraphType.SIMPLE)
                .graphClass(SimpleTestGraph.class);
    }

}
