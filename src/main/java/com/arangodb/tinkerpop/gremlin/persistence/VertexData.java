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

package com.arangodb.tinkerpop.gremlin.persistence;

import com.arangodb.serde.jackson.Id;
import com.arangodb.serde.jackson.Key;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Stream;

public class VertexData implements PropertyData<VertexPropertyData>, PersistentData {

    @Id
    private ElementId id;

    @JsonProperty
    private String label;

    @Key
    private String key;

    @JsonProperty
    private final Map<String, List<VertexPropertyData>> properties = new HashMap<>();

    public VertexData() {
    }

    public static VertexData of(String label, ElementId id) {
        VertexData data = new VertexData();
        data.id = id;
        data.label = label;
        data.key = id.getKey();
        return data;
    }

    @Override
    public ElementId elementId() {
        return id;
    }

    @Override
    public void setId(ElementId id) {
        this.id = id;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Stream<Map.Entry<String, VertexPropertyData>> entries() {
        return properties.entrySet().stream().flatMap(e -> e.getValue().stream()
                .map(v -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), v)));
    }

    @Override
    public void add(String key, VertexPropertyData value) {
        properties.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void remove(String key, VertexPropertyData value) {
        List<VertexPropertyData> props = properties.getOrDefault(key, Collections.emptyList());
        props.remove(value);
        if (props.isEmpty()) {
            properties.remove(key);
        }
    }

    @Override
    public String toString() {
        return "VertexData{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", key='" + key + '\'' +
                ", properties=" + properties +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VertexData)) return false;
        VertexData that = (VertexData) o;
        return Objects.equals(id, that.id) && Objects.equals(label, that.label) && Objects.equals(key, that.key) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, key, properties);
    }
}
