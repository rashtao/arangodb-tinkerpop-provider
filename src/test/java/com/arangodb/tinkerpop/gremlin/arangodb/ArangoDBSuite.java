package com.arangodb.tinkerpop.gremlin.arangodb;

import com.arangodb.tinkerpop.gremlin.arangodb.complex.ComplexElementIdTest;
import com.arangodb.tinkerpop.gremlin.arangodb.simple.SimpleElementIdTest;
import org.apache.tinkerpop.gremlin.AbstractGremlinSuite;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalEngine;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class ArangoDBSuite extends AbstractGremlinSuite {

    private static final Class<?>[] allTests = new Class<?>[]{
            SimpleElementIdTest.class,
            ComplexElementIdTest.class,
    };

    public ArangoDBSuite(final Class<?> klass, final RunnerBuilder builder) throws InitializationError {
        super(klass, builder, allTests, null, false, TraversalEngine.Type.STANDARD);
    }

}
