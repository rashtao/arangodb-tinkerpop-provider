package com.arangodb.tinkerpop.gremlin;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.tinkerpop.gremlin.client.ArangoDBGraphClient;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import org.apache.commons.configuration2.Configuration;


public class TestGraphClient extends ArangoDBGraphClient {

    public TestGraphClient(Configuration config) {
        super(new ArangoDBGraphConfig(config), null);
        if (!db.exists()) {
            db.create();
        }
        ArangoCollection varsCol = db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION);
        if (!varsCol.exists()) {
            varsCol.create();
        }
    }

    public void clear(String name) {
        try {
            db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION).deleteDocument(name);
        } catch (ArangoDBException e) {
            if (e.getErrorNum() != 1202         // document not found
                    && e.getErrorNum() != 1203  // collection not found
            ) throw e;
        }

        try {
            db.graph(name).drop(true);
        } catch (ArangoDBException e) {
            if (e.getErrorNum() != 1924) // graph not found
                throw e;
        }
    }

    public ArangoDatabase database() {
        return db;
    }

    public ArangoCollection variablesCollection() {
        return db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION);
    }

}
