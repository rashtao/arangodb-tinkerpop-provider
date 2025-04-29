/// ///////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the TinkerPop OLTP Provider API for ArangoDB
//
// Copyright triAGENS GmbH Cologne and The University of York
//
/// ///////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.tinkerpop.gremlin.client;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.arangodb.*;
import com.arangodb.entity.*;
import com.arangodb.model.*;
import com.arangodb.serde.jackson.JacksonSerde;
import com.arangodb.tinkerpop.gremlin.persistence.*;
import com.arangodb.tinkerpop.gremlin.structure.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalInterruptedException;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArangoDBGraphClient {

    private static final Logger logger = LoggerFactory.getLogger(ArangoDBGraphClient.class);

    protected final ArangoDatabase db;

    protected final ArangoDBGraphConfig config;

    public ArangoDBGraphClient(ArangoDBGraphConfig config, ElementIdFactory idFactory) {
        logger.debug("Initiating the ArangoDb Client");
        this.config = config;
        db = new ArangoDB.Builder()
                .loadProperties(config.driverConfig)
                .build()
                .db(config.dbName);
        ((JacksonSerde) db.getSerde().getUserSerde()).configure(mapper ->
                mapper.registerModule(createSerdeModule(idFactory)));
    }

    private Module createSerdeModule(ElementIdFactory idFactory) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ElementId.class, new JsonSerializer<ElementId>() {
            @Override
            public void serialize(ElementId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                String json = value.toJson();
                if (json == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(json);
                }
            }
        });
        module.addDeserializer(ElementId.class, new JsonDeserializer<ElementId>() {
            @Override
            public ElementId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
                return idFactory.parseId(p.getText());
            }
        });
        return module;
    }

    public void shutdown() {
        logger.debug("Shutdown");
        db.arango().shutdown();
    }

    public void ensureVariablesDataCollection() {
        ArangoCollection col = db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION);
        if (!col.exists()) {
            col.create();
        }
    }

    public VariablesData getGraphVariables() {
        logger.debug("Get graph variables");
        try {
            return db
                    .collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION)
                    .getDocument(config.graphName, VariablesData.class);
        } catch (ArangoDBException e) {
            logger.error("Failed to retrieve graph variables", e);
            throw mapException(e);
        }
    }

    public VariablesData insertGraphVariables(VariablesData document) {
        logger.debug("Insert graph variables {} in {}", document, config.graphName);
        ArangoCollection col = db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION);
        try {
            col.insertDocument(document);
        } catch (ArangoDBException e) {
            logger.error("Failed to insert document", e);
            throw mapException(e);
        }
        return document;
    }

    public void updateGraphVariables(VariablesData document) {
        logger.debug("Update variables {} in {}", document, config.graphName);
        try {
            db
                    .collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION)
                    .replaceDocument(document.getKey(), document);
        } catch (ArangoDBException e) {
            logger.error("Failed to update document", e);
            throw mapException(e);
        }
    }

    /**
     * Get vertices of a graph. If no ids are provided, get all vertices.
     *
     * @param ids the ids to match
     * @return the documents
     */
    public ArangoIterable<VertexData> getGraphVertices(final List<ElementId> ids) {
        logger.debug("Get all {} graph vertices, filtered by ids: {}", config.graphName, ids);
        return getGraphDocuments(ids, config.vertices, VertexData.class);
    }

    /**
     * Get edges of a graph. If no ids are provided, get all edges.
     *
     * @param ids the ids to match
     * @return the documents
     */
    public ArangoIterable<EdgeData> getGraphEdges(List<ElementId> ids) {
        logger.debug("Get all {} graph edges, filtered by ids: {}", config.graphName, ids);
        return getGraphDocuments(ids, config.edges, EdgeData.class);
    }

    private <V> ArangoIterable<V> getGraphDocuments(List<ElementId> ids, Set<String> colNames, Class<V> clazz) {
        if (ids.isEmpty()) {
            return executeAqlQuery(ArangoDBQueryBuilder.readAllDocuments(colNames), clazz);
        } else {
            List<ElementId> prunedIds = ids.stream()
                    .filter(it -> colNames.contains(it.getCollection()))
                    .collect(Collectors.toList());
            return executeAqlQuery(ArangoDBQueryBuilder.readDocumentsByIds(prunedIds), clazz);
        }
    }

    /**
     * Create a new graph.
     *
     * @param name              the name of the new graph
     * @param edgeDefinitions   the edge definitions for the graph
     * @param orphanCollections orphan collections
     */
    public void createGraph(
            String name,
            Set<ArangoDBGraphConfig.EdgeDef> edgeDefinitions,
            Set<String> orphanCollections
    ) {
        logger.debug("Creating graph {}", name);
        Set<EdgeDefinition> defs = edgeDefinitions.stream()
                .map(ArangoDBGraphConfig.EdgeDef::toDbDefinition)
                .collect(Collectors.toSet());
        try {
            logger.debug("Creating graph in database.");
            db.createGraph(name, defs, new GraphCreateOptions()
                    .orphanCollections(orphanCollections.toArray(new String[0])));
        } catch (ArangoDBException e) {
            logger.debug("Error creating graph in database.", e);
            throw mapException(e);
        }
    }

    /**
     * Get the ArangoGraph that is linked to the client's graph
     *
     * @return the graph or null if the graph was not found
     */
    public ArangoGraph getArangoGraph() {
        return db.graph(config.graphName);
    }

    private <V> ArangoCursor<V> executeAqlQuery(String query, Class<V> type) {
        logger.debug("Executing AQL query: {}", query);
        try {
            return db.query(query, type, new AqlQueryOptions().failOnWarning(true));
        } catch (ArangoDBException e) {
            logger.error("Error executing query", e);
            throw mapException(e);
        }
    }

    public void insertEdge(ArangoDBEdge edge) {
        logger.debug("Insert edge {} in {} ", edge, config.graphName);
        EdgeEntity insertEntity;
        try {
            insertEntity = db.graph(config.graphName)
                    .edgeCollection(edge.collection())
                    .insertEdge(edge.data());
        } catch (ArangoDBException e) {
            logger.error("Failed to insert edge", e);
            throw mapException(e);
        }
        edge.update(insertEntity);
    }

    public void deleteEdge(ArangoDBEdge edge) {
        logger.debug("Delete edge {} in {}", edge, config.graphName);
        try {
            db.graph(config.graphName)
                    .edgeCollection(edge.collection())
                    .deleteEdge(edge.key());
        } catch (ArangoDBException e) {
            if (e.getErrorNum() == 1202) { // document not found
                return;
            }
            logger.error("Failed to delete vertex", e);
            throw mapException(e);
        }
    }

    public void updateEdge(ArangoDBEdge edge) {
        logger.debug("Update edge {} in {}", edge, config.graphName);
        EdgeUpdateEntity updateEntity;
        try {
            updateEntity = db.graph(config.graphName)
                    .edgeCollection(edge.collection())
                    .replaceEdge(edge.key(), edge.data());
        } catch (ArangoDBException e) {
            logger.error("Failed to update edge", e);
            throw mapException(e);
        }
        logger.debug("Edge updated, new rev {}", updateEntity.getRev());
        edge.update(updateEntity);
    }

    public VertexData readVertex(ElementId id) {
        logger.debug("Read vertex {} in {}", id, config.graphName);
        try {
            return db.graph(config.graphName)
                    .vertexCollection(id.getCollection())
                    .getVertex(id.getKey(), VertexData.class);
        } catch (ArangoDBException e) {
            logger.error("Failed to read vertex", e);
            throw mapException(e);
        }
    }

    public void insertVertex(ArangoDBVertex vertex) {
        logger.debug("Insert vertex {} in {}", vertex, config.graphName);
        VertexEntity vertexEntity;
        try {
            vertexEntity = db.graph(config.graphName)
                    .vertexCollection(vertex.collection())
                    .insertVertex(vertex.data());
        } catch (ArangoDBException e) {
            logger.error("Failed to insert document", e);
            throw mapException(e);
        }
        vertex.update(vertexEntity);
    }

    public void deleteVertex(ArangoDBVertex vertex) {
        logger.debug("Delete vertex {} in {}", vertex, config.graphName);
        try {
            db.graph(config.graphName)
                    .vertexCollection(vertex.collection())
                    .deleteVertex(vertex.key());
        } catch (ArangoDBException e) {
            if (e.getErrorNum() == 1202) { // document not found
                return;
            }
            logger.error("Failed to delete vertex", e);
            throw mapException(e);
        }
    }

    public void updateVertex(ArangoDBVertex vertex) {
        logger.debug("Update document {} in {}", vertex, config.graphName);
        VertexUpdateEntity vertexEntity;
        try {
            vertexEntity = db.graph(config.graphName)
                    .vertexCollection(vertex.collection())
                    .replaceVertex(vertex.key(), vertex.data());
        } catch (ArangoDBException e) {
            logger.error("Failed to update document", e);
            throw mapException(e);
        }
        logger.debug("Document updated, new rev {}", vertexEntity.getRev());
    }

    public Iterator<VertexData> getVertexNeighbors(ElementId vertexId, Set<String> edgeCollections, Direction direction, String[] labels) {
        logger.debug("Get vertex {}:{} Neighbors, in {}, from collections {}", vertexId, direction, config.graphName, edgeCollections);
        String query = ArangoDBQueryBuilder.readVertexNeighbors(config.graphName, vertexId, edgeCollections, direction, labels);
        return executeAqlQuery(query, VertexData.class);
    }

    public Iterator<EdgeData> getVertexEdges(ElementId vertexId, Set<String> edgeCollections, Direction direction, String[] labels) {
        logger.debug("Get vertex {}:{} Edges, in {}, from collections {}", vertexId, direction, config.graphName, edgeCollections);
        String query = ArangoDBQueryBuilder.readVertexEdges(config.graphName, vertexId, edgeCollections, direction, labels);
        return executeAqlQuery(query, EdgeData.class);
    }

    private RuntimeException mapException(ArangoDBException ex) {
        if (ex.getCause() instanceof InterruptedException) {
            TraversalInterruptedException ie = new TraversalInterruptedException();
            ie.initCause(ex);
            return ie;
        }
        if (ex.getErrorNum() == 1210) {
            return new IllegalArgumentException("Document with id already exists", ex);
        }
        return ex;
    }

}