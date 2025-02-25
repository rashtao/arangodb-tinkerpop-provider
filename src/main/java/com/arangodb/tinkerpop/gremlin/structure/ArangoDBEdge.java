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

import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil.*;


public class ArangoDBEdge extends ArangoDBElement<ArangoDBEdgeData> implements Edge {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoDBEdge.class);

    public ArangoDBEdge(ArangoDBGraph graph, ArangoDBEdgeData data) {
        super(graph, data);
    }

    public ArangoDBEdge(final String id, final String label, final String outVertexId, final String inVertexId, ArangoDBGraph graph) {
        this(graph, new ArangoDBEdgeData(extractLabel(id, label).orElse(DEFAULT_LABEL), extractKey(id), outVertexId, inVertexId));
    }

    @Override
    public <V> Property<V> property(final String key, final V value) {
        if (removed) throw elementAlreadyRemoved(Edge.class, id());
        LOGGER.trace("set property {} = {}", key, value);
        ElementHelper.validateProperty(key, value);
        data.setProperty(key, value);
        update();
        return new ArangoDBProperty<>(this, key, value);
    }

    @Override
    public void remove() {
        LOGGER.trace("removing edge {} from graph {}.", id(), graph.name());
        graph.getClient().deleteEdge(data);
        this.removed = true;
    }

    @Override
    public String toString() {
        return StringFactory.edgeString(this);
    }

    @Override
    public Vertex outVertex() {
        return new ArangoDBVertex(graph, graph.getClient().readVertex(data.getFrom()));
    }

    @Override
    public Vertex inVertex() {
        return new ArangoDBVertex(graph, graph.getClient().readVertex(data.getTo()));
    }

    @Override
    public Iterator<Vertex> vertices(final Direction direction) {
        if (removed) return Collections.emptyIterator();
        switch (direction) {
            case OUT:
                return IteratorUtils.of(this.outVertex());
            case IN:
                return IteratorUtils.of(this.inVertex());
            default:
                return IteratorUtils.of(this.outVertex(), this.inVertex());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Iterator<Property<V>> properties(final String... propertyKeys) {
        return data.properties()
                .filter(entry -> ElementHelper.keyExists(entry.getKey(), propertyKeys))
                .map(entry -> (Property<V>) new ArangoDBProperty<>(this, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()).iterator();
    }

    public void insert() {
        if (removed) throw elementAlreadyRemoved(Edge.class, id());
        graph.getClient().insertEdge(data);
    }

    public void removeProperty(String key) {
        if (removed) throw elementAlreadyRemoved(Edge.class, id());
        if (data.hasProperty(key)) {
            data.removeProperty(key);
            update();
        }
    }

    public void update() {
        if (removed) throw elementAlreadyRemoved(Edge.class, id());
        graph.getClient().updateEdge(data);
    }

}
