package com.arangodb.tinkerpop.gremlin;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;

public abstract class DataDefinitionTest extends AbstractTest {

    protected abstract ArangoDBGraphConfig.GraphType graphType();

    protected ArangoDBConfigurationBuilder confBuilder() {
        return super.confBuilder().graphType(graphType());
    }
}
