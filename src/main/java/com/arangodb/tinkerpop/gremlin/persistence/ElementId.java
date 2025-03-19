package com.arangodb.tinkerpop.gremlin.persistence;

import com.arangodb.shaded.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.arangodb.shaded.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ArangoId.Serializer.class)
@JsonDeserialize(using = ArangoId.Deserializer.class)
public interface ElementId {

    ElementId withKey(String newKey);

    String getCollection();

    String getLabel();

    String getKey();

    String getId();
}
