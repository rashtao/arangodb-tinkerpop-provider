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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Objects;

@JsonDeserialize(using = PropertyValue.PropertyValueDeserializer.class)
public final class PropertyValue {

    private final Object value;

    public PropertyValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @JsonProperty
    public String getType() {
        return (value != null ? value.getClass() : Void.class).getCanonicalName();
    }

    @Override
    public String toString() {
        return "PropertyValue{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PropertyValue)) return false;
        PropertyValue that = (PropertyValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    public static class PropertyValueDeserializer extends StdDeserializer<PropertyValue> {
        public PropertyValueDeserializer() {
            super(PropertyValue.class);
        }

        @Override
        public PropertyValue deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            ObjectCodec c = p.getCodec();
            JsonNode node = c.readTree(p);
            Class<?> type = c.treeToValue(node.get("type"), Class.class);
            Object value = c.treeToValue(node.get("value"), type);
            return new PropertyValue(value);
        }
    }
}
    
