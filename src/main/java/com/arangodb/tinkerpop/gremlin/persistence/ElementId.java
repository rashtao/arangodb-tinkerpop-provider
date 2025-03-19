package com.arangodb.tinkerpop.gremlin.persistence;

public interface ElementId {

    ElementId withKey(String newKey);

    String getPrefix();

    String getLabel();

    String getCollection();

    String getId();

    String getKey();

    String toJson();

    static void validateIdParts(String... names) {
        for (String name : names) {
            if (name == null)
                continue;
            if (name.contains("_")) {
                throw new IllegalArgumentException(String.format("key (%s) contains invalid character '_'", name));
            }
            if (name.contains("/")) {
                throw new IllegalArgumentException(String.format("key (%s) contains invalid character '/'", name));
            }
        }
    }

}
