package com.arangodb.tinkerpop.gremlin.client;

import com.arangodb.ArangoDatabase;

public interface Driver {

    class DatabaseCreationException extends Exception {
        public DatabaseCreationException(String message) {
            super(message);
        }
    }

    /**
     * Get the ArangoDB database with the given name
     * @param name                  the database name
     * @return
     */
    ArangoDatabase getDatabase(String name);

    ArangoDatabase createDatabase(String name) throws DatabaseCreationException;
}
