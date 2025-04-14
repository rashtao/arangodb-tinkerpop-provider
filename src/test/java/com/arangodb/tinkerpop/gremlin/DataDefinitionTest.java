package com.arangodb.tinkerpop.gremlin;

import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;

public abstract class DataDefinitionTest extends AbstractTest {

    protected abstract boolean isSimple();

    protected ArangoDBConfigurationBuilder confBuilder() {
        return super.confBuilder().simpleGraph(isSimple());
    }
}
