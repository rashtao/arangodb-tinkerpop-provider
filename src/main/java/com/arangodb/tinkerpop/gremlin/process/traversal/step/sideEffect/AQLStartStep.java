package com.arangodb.tinkerpop.gremlin.process.traversal.step.sideEffect;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.StartStep;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.Iterator;


public final class AQLStartStep extends StartStep<Object> {

    private final String query;

    public AQLStartStep(final Traversal.Admin traversal, final String query, final Iterator<?> aqlCursor) {
        super(traversal, aqlCursor);
        this.query = query;
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.query);
    }
}
