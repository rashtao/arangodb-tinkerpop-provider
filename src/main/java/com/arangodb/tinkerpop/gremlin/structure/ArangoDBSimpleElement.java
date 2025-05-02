package com.arangodb.tinkerpop.gremlin.structure;

import com.arangodb.tinkerpop.gremlin.persistence.PropertyValue;
import com.arangodb.tinkerpop.gremlin.persistence.SimplePropertiesContainer;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;

public abstract class ArangoDBSimpleElement<D extends SimplePropertiesContainer> extends ArangoDBElement<PropertyValue, D> {
    ArangoDBSimpleElement(ArangoDBGraph graph, D data) {
        super(graph, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <V> Property<V> createProperty(String key, PropertyValue value) {
        return new ArangoDBProperty<>(this, key, (V) value.getValue());
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        if (removed()) throw Exceptions.elementAlreadyRemoved(id());
        ElementHelper.validateProperty(key, value);
        PropertyValue dataValue = new PropertyValue(value);
        data().add(key, dataValue);
        doUpdate();
        return createProperty(key, dataValue);
    }

    void removeProperty(String key) {
        if (removed()) throw Exceptions.elementAlreadyRemoved(id());
        data.remove(key);
        doUpdate();
    }

}
