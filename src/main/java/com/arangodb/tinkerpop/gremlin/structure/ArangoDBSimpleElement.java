package com.arangodb.tinkerpop.gremlin.structure;

import com.arangodb.tinkerpop.gremlin.persistence.SimplePropertiesContainer;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;

public abstract class ArangoDBSimpleElement<D extends SimplePropertiesContainer> extends ArangoDBElement<Object, D> {
    ArangoDBSimpleElement(ArangoDBGraph graph, D data) {
        super(graph, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <V> Property<V> createProperty(String key, Object value) {
        return new ArangoDBProperty<>(this, key, (V) value);
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        if (removed()) throw Exceptions.elementAlreadyRemoved(id());
        ElementHelper.validateProperty(key, value);
        ArangoDBUtil.validatePropertyValue(value);
        data().add(key, value);
        doUpdate();
        return createProperty(key, value);
    }

    void removeProperty(String key) {
        if (removed()) throw Exceptions.elementAlreadyRemoved(id());
        data.remove(key);
        doUpdate();
    }

}
