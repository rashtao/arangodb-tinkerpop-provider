package com.arangodb.tinkerpop.gremlin.persistence;

import java.util.Objects;

class ArangoId implements ElementId {
    protected final String prefix;
    protected final String collection;
    protected final String key;

    ArangoId(String prefix, String collection, String key) {
        this.prefix = prefix;
        this.collection = collection;
        this.key = key;
    }

    @Override
    public ArangoId withKey(String newKey) {
        ElementId.validateIdParts(newKey);
        return new ArangoId(prefix, collection, newKey);
    }

    @Override
    public String getLabel() {
        return collection;
    }

    @Override
    public String getCollection() {
        return prefix + "_" + collection;
    }

    @Override
    public String getId() {
        return collection + "/" + key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String toJson() {
        if (key == null) {
            return null;
        } else {
            return prefix + "_" + collection + "/" + key;
        }
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArangoId)) return false;
        ArangoId arangoId = (ArangoId) o;
        return Objects.equals(prefix, arangoId.prefix) && Objects.equals(collection, arangoId.collection) && Objects.equals(key, arangoId.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, collection, key);
    }
}
