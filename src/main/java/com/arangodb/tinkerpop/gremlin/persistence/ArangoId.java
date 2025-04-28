package com.arangodb.tinkerpop.gremlin.persistence;

class ArangoId extends ElementId {

    ArangoId(String prefix, String collection, String key) {
        super(prefix, collection, key);
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
    public String getId() {
        return collection + "/" + key;
    }

}
