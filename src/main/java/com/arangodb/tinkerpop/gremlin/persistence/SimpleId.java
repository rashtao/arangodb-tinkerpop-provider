package com.arangodb.tinkerpop.gremlin.persistence;

class SimpleId extends ElementId {

    SimpleId(String prefix, String collection, String key) {
        super(prefix, collection, key);
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
    public String getId() {
        return getKey();
    }

}
