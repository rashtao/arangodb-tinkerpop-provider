package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.tinkerpop.gremlin.TestGraphProvider;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.tinkerpop.gremlin.LoadGraphWith;

public class SimpleGraphProvider extends TestGraphProvider {

    @Override
    protected void configure(ArangoDBConfigurationBuilder builder, Class<?> test, String testMethodName, LoadGraphWith.GraphData loadGraphWith) {
        builder.simpleGraph(true);
    }

}
