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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil.getArangoDirectionFromGremlinDirection;

/**
 * The arangodb graph client class handles the HTTP connection to arangodb and performs database
 * operations on the ArangoDatabase.
 */

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
            public ElementId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return idFactory.parseId(p.getText());
            }
        });
        return module;
    }

    /**
     * Shutdown the client and free resources.
     */

    public void shutdown() {
        logger.debug("Shutdown");
        db.arango().shutdown();
    }

    public VariablesData getGraphVariables() {
        logger.debug("Get graph variables");
        try {
            return db
                    .collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION)
                    .getDocument(config.graphName, VariablesData.class);
        } catch (ArangoDBException e) {
            // collection not found
            if (e.getErrorNum() == 1203) {
                return null;
            }
            logger.error("Failed to retrieve graph variables: {}", e.getErrorMessage());
            throw new ArangoDBGraphException("Failed to retrieve graph variables.", e);
        }
    }

    public VariablesData insertGraphVariables(VariablesData document) {
        logger.debug("Insert graph variables {} in {}", document, config.graphName);
        ArangoCollection col = db.collection(ArangoDBGraph.GRAPH_VARIABLES_COLLECTION);
        if (!col.exists()) {
            col.create();
        }
        try {
            col.insertDocument(document);
        } catch (ArangoDBException e) {
            logger.error("Failed to insert document: {}", e.getMessage());
            ArangoDBGraphException arangoDBException = ArangoDBExceptions.getArangoDBException(e);
            if (arangoDBException.getErrorCode() == 1210) {
                throw Graph.Exceptions.vertexWithIdAlreadyExists(document.getKey());
            }
            throw arangoDBException;
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
            logger.error("Failed to update document: {}", e.getErrorMessage());
            throw ArangoDBExceptions.getArangoDBException(e);
        }
    }

    /**
     * Get vertices of a graph. If no ids are provided, get all vertices.
     *
     * @param ids the ids to match
     * @return ArangoDBBaseQuery    the query object
     */

    // FIXME: use multi-docs API
    public ArangoIterable<VertexData> getGraphVertices(final List<ElementId> ids) {
        logger.debug("Get all {} graph vertices, filtered by ids: {}", config.graphName, ids);
        return getGraphDocuments(ids, config.vertices, VertexData.class);
    }

    /**
     * Get edges of a graph. If no ids are provided, get all edges.
     *
     * @param ids the ids to match
     * @return ArangoDBBaseQuery    the query object
     */
    // FIXME: use multi-docs API
    public ArangoIterable<EdgeData> getGraphEdges(List<ElementId> ids) {
        logger.debug("Get all {} graph edges, filtered by ids: {}", config.graphName, ids);
        return getGraphDocuments(ids, config.edges, EdgeData.class);
    }

    private <V> ArangoIterable<V> getGraphDocuments(List<ElementId> ids, Set<String> prefixedColNames, Class<V> clazz) {
        Map<String, Object> bindVars = new HashMap<>();
        ArangoDBQueryBuilder queryBuilder = new ArangoDBQueryBuilder();
        if (ids.isEmpty()) {
            if (prefixedColNames.size() > 1) {
                queryBuilder.union(prefixedColNames, "d", bindVars);
            } else {
                queryBuilder.iterateCollection("d", prefixedColNames.iterator().next(), bindVars);
            }
        } else {
            List<ElementId> prunedIds = ids.stream()
                    .filter(it -> prefixedColNames.contains(it.getCollection()))
                    .collect(Collectors.toList());
            queryBuilder.with(prefixedColNames, bindVars).documentsById(prunedIds, "d", bindVars);
        }
        queryBuilder.ret("d");
        String query = queryBuilder.toString();
        logger.debug("AQL {}", query);
        return executeAqlQuery(query, bindVars, null, clazz);
    }

    /**
     * Create a new graph.
     *
     * @param name              the name of the new graph
     * @param edgeDefinitions   the edge definitions for the graph
     * @param orphanCollections orphan collections
     * @throws ArangoDBGraphException If the graph can not be created
     */

    public void createGraph(String name,
                            Set<ArangoDBGraphConfig.EdgeDef> edgeDefinitions,
                            Set<String> orphanCollections)
            throws ArangoDBGraphException {
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
            throw ArangoDBExceptions.getArangoDBException(e);
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

    /**
     * Execute AQL query.
     *
     * @param <V>             the generic type of the returned values
     * @param query           the query string
     * @param bindVars        the value of the bind parameters
     * @param aqlQueryOptions the aql query options
     * @param type            the type of the result (POJO class, VPackSlice, String for Json, or Collection/List/Map)
     * @return the cursor result
     * @throws ArangoDBGraphException if executing the query raised an exception
     */

    public <V> ArangoCursor<V> executeAqlQuery(
            String query,
            Map<String, Object> bindVars,
            AqlQueryOptions aqlQueryOptions,
            final Class<V> type)
            throws ArangoDBGraphException {
        logger.debug("Executing AQL query ({}) against db, with bind vars: {}", query, bindVars);
        try {
            return db.query(query, type, bindVars, aqlQueryOptions);
        } catch (ArangoDBException e) {
            logger.error("Error executing query", e);
            throw ArangoDBExceptions.getArangoDBException(e);
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
            logger.error("Failed to insert edge: {}", e.getErrorMessage());
            ArangoDBGraphException arangoDBException = ArangoDBExceptions.getArangoDBException(e);
            if (arangoDBException.getErrorCode() == 1210) {
                throw Graph.Exceptions.edgeWithIdAlreadyExists(edge.collection() + "/" + edge.key());
            }
            throw arangoDBException;
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
            logger.error("Failed to delete vertex: {}", e.getErrorMessage());
            throw ArangoDBExceptions.getArangoDBException(e);
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
            logger.error("Failed to update edge: {}", e.getErrorMessage());
            throw ArangoDBExceptions.getArangoDBException(e);
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
            logger.error("Failed to read vertex: {}", e.getErrorMessage());
            throw ArangoDBExceptions.getArangoDBException(e);
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
            logger.error("Failed to insert document: {}", e.getMessage());
            ArangoDBGraphException arangoDBException = ArangoDBExceptions.getArangoDBException(e);
            if (arangoDBException.getErrorCode() == 1210) {
                throw Graph.Exceptions.vertexWithIdAlreadyExists(vertex.key());
            }
            throw arangoDBException;
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
            logger.error("Failed to delete vertex: {}", e.getErrorMessage());
            throw ArangoDBExceptions.getArangoDBException(e);
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
            logger.error("Failed to update document: {}", e.getErrorMessage());
            throw ArangoDBExceptions.getArangoDBException(e);
        }
        logger.debug("Document updated, new rev {}", vertexEntity.getRev());
    }

    public Iterator<VertexData> getVertexNeighbors(ElementId vertexId, Set<String> edgeCollections, Direction direction, String[] labels) {
        logger.debug("Get vertex {}:{} Neighbors, in {}, from collections {}", vertexId, direction, config.graphName, edgeCollections);
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("start", vertexId);
        bindVars.put("graph", config.graphName);
        bindVars.put("edgeCollections", edgeCollections);
        StringBuilder query = new StringBuilder()
                .append("FOR v, e IN 1..1 ")
                .append(getArangoDirectionFromGremlinDirection(direction).getAqlName())
                .append(" @start GRAPH @graph OPTIONS { edgeCollections: @edgeCollections }");
        if (labels.length > 0) {
            bindVars.put("labels", labels);
            query.append(" FILTER e.label IN @labels");
        }
        query.append(" RETURN v");
        return executeAqlQuery(query.toString(), bindVars, null, VertexData.class);
    }

    public Iterator<EdgeData> getVertexEdges(ElementId vertexId, Set<String> edgeCollections, Direction direction, String[] labels) {
        logger.debug("Get vertex {}:{} Edges, in {}, from collections {}", vertexId, direction, config.graphName, edgeCollections);
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("start", vertexId);
        bindVars.put("graph", config.graphName);
        bindVars.put("edgeCollections", edgeCollections);
        StringBuilder query = new StringBuilder()
                .append("FOR v, e IN 1..1 ")
                .append(getArangoDirectionFromGremlinDirection(direction).getAqlName())
                .append(" @start GRAPH @graph OPTIONS { edgeCollections: @edgeCollections }");
        if (labels.length > 0) {
            bindVars.put("labels", labels);
            query.append(" FILTER e.label IN @labels");
        }
        query.append(" RETURN e");
        return executeAqlQuery(query.toString(), bindVars, null, EdgeData.class);
    }

    /**
     * Common exceptions to use with an ArangoDB. This class is intended to translate ArangoDB error codes into
     * meaningful exceptions with standard messages. ArangoDBException exception is a RuntimeException intended to
     * break execution.
     */
    public static class ArangoDBExceptions {

        /**
         * The error code regex. Matches response messages from the ArangoDB client
         */

        public static final Pattern ERROR_CODE = Pattern.compile("^Response:\\s\\d+,\\sError:\\s(\\d+)\\s-\\s([a-z\\s]+).+");

        /**
         * Instantiation happens via factory method
         */

        private ArangoDBExceptions() {
        }

        /**
         * Translate ArangoDB Error code into exception (@see <a href="https://docs.arangodb.com/latest/Manual/Appendix/ErrorCodes.html">Error codes</a>)
         *
         * @param ex the ex
         * @return The ArangoDBClientException
         */
        // FIXME: match errors on code and error num instead of pattern matching on message string
        public static ArangoDBGraphException getArangoDBException(ArangoDBException ex) {
            if (ex.getCause() instanceof InterruptedException) {
                TraversalInterruptedException ie = new TraversalInterruptedException();
                ie.initCause(ex);
                throw ie;
            }

            String errorMessage = ex.getMessage();
            Matcher m = ERROR_CODE.matcher(errorMessage);
            if (m.matches()) {
                int code = Integer.parseInt(m.group(1));
                String msg = m.group(2);
                switch (code / 100) {
                    case 10:    // Internal ArangoDB storage errors
                        return new ArangoDBGraphException(code, String.format("Internal ArangoDB storage error (%s): %s", code, msg), ex);
                    case 11:
                        return new ArangoDBGraphException(code, String.format("External ArangoDB storage error (%s): %s", code, msg), ex);
                    case 12:
                        return new ArangoDBGraphException(code, String.format("General ArangoDB storage error (%s): %s", code, msg), ex);
                    case 13:
                        return new ArangoDBGraphException(code, String.format("Checked ArangoDB storage error (%s): %s", code, msg), ex);
                    case 14:
                        return new ArangoDBGraphException(code, String.format("ArangoDB replication/cluster error (%s): %s", code, msg), ex);
                    case 15:
                        return new ArangoDBGraphException(code, String.format("ArangoDB query error (%s): %s", code, msg), ex);
                    case 19:
                        return new ArangoDBGraphException(code, String.format("Graph / traversal errors (%s): %s", code, msg), ex);
                }
            }
            return new ArangoDBGraphException("General ArangoDB error (unkown error code)", ex);
        }

    }

}