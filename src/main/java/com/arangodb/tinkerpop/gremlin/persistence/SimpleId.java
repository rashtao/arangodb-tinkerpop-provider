package com.arangodb.tinkerpop.gremlin.persistence;


import java.util.Objects;

class SimpleId implements ElementId {

    protected final String prefix;
    protected final String collection;
    protected final String key;

    SimpleId(String prefix, String collection, String key) {
        this.prefix = prefix;
        this.collection = collection;
        this.key = key;
    }

    @Override
    public SimpleId withKey(String newKey) {
        ElementId.validateIdParts(newKey);
        return new SimpleId(prefix, collection, newKey);
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public String getCollection() {
        return prefix + "_" + collection;
    }

    @Override
    public String getId() {
        return getKey();
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
        if (!(o instanceof SimpleId)) return false;
        SimpleId SimpleId = (SimpleId) o;
        return Objects.equals(prefix, SimpleId.prefix) && Objects.equals(collection, SimpleId.collection) && Objects.equals(key, SimpleId.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, collection, key);
    }
}
