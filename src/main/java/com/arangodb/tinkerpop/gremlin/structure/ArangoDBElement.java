/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.arangodb.tinkerpop.gremlin.structure;

import com.arangodb.tinkerpop.gremlin.persistence.PropertyData;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ArangoDBElement<P, D extends PropertyData<P>> implements Element {

    protected final ArangoDBGraph graph;
    protected final D data;
    private boolean removed = false;

    public ArangoDBElement(ArangoDBGraph graph, D data) {
        this.graph = graph;
        this.data = data;
    }

    protected abstract String stringify();

    // properties transformers
    protected abstract <V> Stream<Property<V>> toProperties(String key, P value);

    protected abstract <V> Property<V> createProperty(String key, Object value);

    protected abstract P toData(Object value);

    // CRUD ops
    protected abstract void update();

    protected abstract void doRemove();

    protected abstract void insert();

    public D data() {
        return data;
    }

    protected boolean removed() {
        return removed;
    }

    @Override
    public ArangoDBGraph graph() {
        return graph;
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        if (removed) throw Exceptions.elementAlreadyRemoved(id());
        ElementHelper.validateProperty(key, value);
        P data = toData(value);
        setProperty(key, data);
        update();
        return createProperty(key, value);
    }

    @Override
    public void remove() {
        if (removed) return;
        doRemove();
        removed = true;
    }

    @Override
    public <V> Iterator<? extends Property<V>> properties(String... propertyKeys) {
        if (removed) return Collections.emptyIterator();
        return data.getProperties()
                .entrySet()
                .stream()
                .filter(entry -> ElementHelper.keyExists(entry.getKey(), propertyKeys))
                .flatMap((Map.Entry<String, P> e) -> this.<V>toProperties(e.getKey(), e.getValue()))
                .collect(Collectors.toList()) // avoids ConcurrentModificationException on removal from downstream
                .iterator();
    }

    public void removeProperty(String key) {
        if (removed) throw Exceptions.elementAlreadyRemoved(id());
        data.getProperties().remove(key);
        update();
    }

    private void setProperty(String key, P value) {
        data.getProperties().put(key, value);
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

    @Override
    public String toString() {
        return stringify();
    }

    public static class Exceptions {
        private Exceptions() {
        }

        public static IllegalStateException elementAlreadyRemoved(final Object id) {
            return new IllegalStateException(String.format("Element with id %s was removed.", id));
        }

        public static IllegalStateException unsupportedIdType(final Object id) {
            return new IllegalStateException(String.format("Unsupported id type [%s]: %s", id.getClass().getSimpleName(), id));
        }
    }
}

