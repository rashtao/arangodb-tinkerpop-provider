package com.arangodb.tinkerpop.gremlin.structure;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;


public abstract class ArangoDBElement<T extends ArangoDBData<?>> implements Element {
    protected final ArangoDBGraph graph;
    protected final T data;
    protected boolean removed = false;

    protected ArangoDBElement(final ArangoDBGraph graph, final T data) {
        this.graph = graph;
        this.data = data;
    }

    @Override
    public String id() {
        String key = data.getKey();
        if (key == null) {
            return null;
        }
        return graph.getPrefixedCollectioName(label()) + "/" + key;
    }

    @Override
    public String label() {
        return data.getLabel();
    }

    @Override
    public ArangoDBGraph graph() {
        return graph;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return ElementHelper.hashCode(this);
    }

}
