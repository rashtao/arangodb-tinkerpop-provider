/// ///////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the TinkerPop OLTP Provider API for ArangoDB
//
// Copyright triAGENS GmbH Cologne and The University of York
//
/// ///////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.tinkerpop.gremlin.structure;

import java.util.*;

import com.arangodb.tinkerpop.gremlin.PackageVersion;
import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.ElementIdFactory;
import com.arangodb.tinkerpop.gremlin.persistence.VariablesData;
import com.arangodb.tinkerpop.gremlin.process.traversal.step.sideEffect.AQLStartStep;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoGraph;
import com.arangodb.tinkerpop.gremlin.client.ArangoDBGraphClient;

public class ArangoDBGraph implements Graph {
    public static final String GRAPH_VARIABLES_COLLECTION = "TINKERPOP-GRAPH-VARIABLES";
    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoDBGraph.class);
    private static final Features FEATURES = new ArangoDBGraphFeatures();

    private final ArangoDBGraphClient client;
    private final ElementIdFactory idFactory;
    private final ArangoDBGraphConfig config;

    /**
     * Open a new {@code ArangoDBGraph} instance.
     * <p/>
     * This method is used by the {@link GraphFactory} to instantiate {@link Graph} instances.
     *
     * @param configuration the configuration for the instance
     * @return a newly opened {@link Graph}
     */
    public static ArangoDBGraph open(Configuration configuration) {
        return new ArangoDBGraph(configuration);
    }

    protected ArangoDBGraph(Configuration cfg) {
        LOGGER.debug("Creating new ArangoDB Graph from configuration");
        config = new ArangoDBGraphConfig(cfg);
        idFactory = new ElementIdFactory(config);
        client = new ArangoDBGraphClient(config, idFactory, this);

        ArangoGraph graph = client.getArangoGraph();
        if (graph.exists()) {
            ArangoDBUtil.checkExistingGraph(graph.getInfo(), config);
        } else {
            client.createGraph(name(), config.edgeDefinitions, config.orphanCollections);
        }

        client.ensureVariablesDataCollection();
        VariablesData variablesData = Optional
                .ofNullable(client.getGraphVariables())
                .orElseGet(() -> client.insertGraphVariables(new VariablesData(name(), PackageVersion.VERSION)));
        ArangoDBGraphVariables variables = new ArangoDBGraphVariables(this, variablesData);
        ArangoDBUtil.checkVersion(variables.getVersion());
        variables.updateVersion(PackageVersion.VERSION);
    }

    public Set<String> edgeCollections() {
        return config.edges;
    }

    public Set<String> vertexCollections() {
        return config.vertices;
    }

    public ArangoDBGraphConfig.GraphType type() {
        return config.graphType;
    }

    @Override
    public Vertex addVertex(Object... keyValues) {
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        String label = ElementHelper.getLabelValue(keyValues).orElse(null);
        ElementId id = idFactory.createVertexId(label, keyValues);
        for (int i = 1; i < keyValues.length; i = i + 2) {
            ArangoDBUtil.validatePropertyValue(keyValues[i]);
        }
        ArangoDBVertex vertex = ArangoDBVertex.of(label, id, this);
        if (!config.vertices.contains(vertex.collection())) {
            throw new IllegalArgumentException(String.format("Vertex collection (%s) not in graph (%s).", vertex.collection(), name()));
        }

        vertex.doInsert();
        ElementHelper.attachProperties(vertex, keyValues);
        return vertex;
    }

    @Override
    public void close() {
        client.shutdown();
    }


    @Override
    public GraphComputer compute() throws IllegalArgumentException {
        throw Graph.Exceptions.graphComputerNotSupported();
    }

    @Override
    public <C extends GraphComputer> C compute(Class<C> graphComputerClass) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration configuration() {
        return config.configuration;
    }

    @Override
    public Iterator<Edge> edges(Object... edgeIds) {
        return getClient().getGraphEdges(idFactory.parseEdgeIds(edgeIds)).stream()
                .map(it -> (Edge) new ArangoDBEdge(this, it))
                .iterator();
    }

    @Override
    public Iterator<Vertex> vertices(Object... vertexIds) {
        return getClient().getGraphVertices(idFactory.parseVertexIds(vertexIds)).stream()
                .map(it -> (Vertex) new ArangoDBVertex(this, it))
                .iterator();
    }

    @Override
    public Features features() {
        return FEATURES;
    }

    public ArangoDBGraphClient getClient() {
        return client;
    }

    ElementIdFactory getIdFactory() {
        return idFactory;
    }

    public String name() {
        return config.graphName;
    }

    @Override
    public Transaction tx() {
        throw Graph.Exceptions.transactionsNotSupported();
    }

    @Override
    public Variables variables() {
        return new ArangoDBGraphVariables(this, client.getGraphVariables());
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, config.toString());
    }

    /**
     * Execute the AQL query and get the result set as a {@link GraphTraversal}.
     *
     * @param query the AQL query to execute
     * @return a fluent Gremlin traversal
     */
    public <S, E> GraphTraversal<S, E> aql(final String query) {
        return aql(query, Collections.emptyMap());
    }

    /**
     * Execute the AQL query with provided parameters and get the result set as a {@link GraphTraversal}.
     *
     * @param query      the AQL query to execute
     * @param parameters the parameters of the AQL query
     * @return a fluent Gremlin traversal
     */
    public <S, E> GraphTraversal<S, E> aql(final String query, final Map<String, Object> parameters) {
        GraphTraversal.Admin<S, E> traversal = new DefaultGraphTraversal<>(this);
        traversal.addStep(new AQLStartStep(traversal, query, client.execute(query, parameters)));
        return traversal;
    }

    String getPrefixedCollectionName(String collectionName) {
        if (collectionName.startsWith(config.graphName + "_")) {
            return collectionName;
        }
        return config.graphName + "_" + collectionName;
    }

}
