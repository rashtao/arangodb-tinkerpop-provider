package com.arangodb.tinkerpop.gremlin.arangodb.complex;

import org.apache.tinkerpop.gremlin.AbstractGremlinSuite;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalEngine;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class ComplexArangoDBSuite extends AbstractGremlinSuite {

    private static final Class<?>[] allTests = new Class<?>[]{
            ComplexElementIdTest.class,
    };

    public ComplexArangoDBSuite(final Class<?> klass, final RunnerBuilder builder) throws InitializationError {
        super(klass, builder, allTests, null, false, TraversalEngine.Type.STANDARD);
    }

}
