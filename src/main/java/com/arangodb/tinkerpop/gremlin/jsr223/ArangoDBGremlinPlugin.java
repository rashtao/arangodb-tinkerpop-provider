/// ///////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the TinkerPop OLTP Provider API for ArangoDB
//
// Copyright triAGENS GmbH Cologne and The University of York
//
/// ///////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.tinkerpop.gremlin.jsr223;

import com.arangodb.tinkerpop.gremlin.persistence.*;
import org.apache.tinkerpop.gremlin.jsr223.AbstractGremlinPlugin;
import org.apache.tinkerpop.gremlin.jsr223.DefaultImportCustomizer;
import org.apache.tinkerpop.gremlin.jsr223.ImportCustomizer;

import com.arangodb.tinkerpop.gremlin.client.*;
import com.arangodb.tinkerpop.gremlin.structure.*;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;

public class ArangoDBGremlinPlugin extends AbstractGremlinPlugin {

    private static final String NAME = "tinkerpop.arangodb";
    private static final ImportCustomizer IMPORTS;

    static {
        try {
            IMPORTS = DefaultImportCustomizer.build().addClassImports(
                            ArangoDBGraphClient.class,
                            ArangoDBQueryBuilder.class,
                            ArangoDBUtil.class,

                            // structure
                            ArangoDBEdge.class,
                            ArangoDBElement.class,
                            ArangoDBGraph.class,
                            ArangoDBGraphVariables.class,
                            ArangoDBPersistentElement.class,
                            ArangoDBProperty.class,
                            ArangoDBSimpleElement.class,
                            ArangoDBVertex.class,
                            ArangoDBVertexProperty.class,

                            // persistence
                            PropertyValue.class,
                            EdgeData.class,
                            PersistentData.class,
                            PropertiesContainer.class,
                            SimplePropertiesContainer.class,
                            VariablesData.class,
                            VertexData.class,
                            VertexPropertyData.class
                    )
                    .create();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final ArangoDBGremlinPlugin INSTANCE = new ArangoDBGremlinPlugin();

    public ArangoDBGremlinPlugin() {
        super(NAME, IMPORTS);
    }

    public static ArangoDBGremlinPlugin instance() {
        return INSTANCE;
    }

    @Override
    public boolean requireRestart() {
        return true;
    }

}
