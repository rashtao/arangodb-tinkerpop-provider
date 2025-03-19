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

import com.arangodb.entity.DocumentEntity;

public interface PersistentData {

    ElementId getElementId();

    void setId(ElementId id);

    void setKey(String key);

    default String getId() {
        return getElementId().getId();
    }

    default String getLabel() {
        return getElementId().getLabel();
    }

    default String getKey() {
        return getElementId().getKey();
    }

    default String getCollection() {
        return getElementId().getCollection();
    }

    default void update(DocumentEntity entity) {
        String k = entity.getKey();
        setKey(k);
        setId(getElementId().withKey(k));
    }

}
