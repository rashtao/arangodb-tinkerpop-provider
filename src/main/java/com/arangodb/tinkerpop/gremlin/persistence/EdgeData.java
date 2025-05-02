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

import com.arangodb.serde.jackson.From;
import com.arangodb.serde.jackson.Id;
import com.arangodb.serde.jackson.Key;
import com.arangodb.serde.jackson.To;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class EdgeData extends SimplePropertiesContainer implements PersistentData {

    @Id
    private ElementId id;

    @JsonProperty
    private String label;

    @Key
    private String key;

    @From
    private ElementId from;

    @To
    private ElementId to;

    public static EdgeData of(
            String label,
            ElementId id,
            ElementId from,
            ElementId to
    ) {
        EdgeData data = new EdgeData();
        data.id = id;
        data.label = label != null ? label : id.getLabel();
        data.key = id.getKey();
        data.from = from;
        data.to = to;
        return data;
    }

    public EdgeData() {
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

    public ElementId getFrom() {
        return from;
    }

    @SuppressWarnings("unused")
    public void setFrom(ElementId from) {
        this.from = from;
    }

    public ElementId getTo() {
        return to;
    }

    @SuppressWarnings("unused")
    public void setTo(ElementId to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "EdgeData{" +
                "from=" + from +
                ", id=" + id +
                ", label='" + label + '\'' +
                ", key='" + key + '\'' +
                ", to=" + to +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EdgeData)) return false;
        if (!super.equals(o)) return false;
        EdgeData edgeData = (EdgeData) o;
        return Objects.equals(id, edgeData.id) && Objects.equals(label, edgeData.label) && Objects.equals(key, edgeData.key) && Objects.equals(from, edgeData.from) && Objects.equals(to, edgeData.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, label, key, from, to);
    }
}
