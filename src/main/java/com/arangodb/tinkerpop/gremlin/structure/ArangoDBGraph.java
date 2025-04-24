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
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoGraph;
import com.arangodb.tinkerpop.gremlin.client.ArangoDBGraphClient;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;

/**
 * The ArangoDB graph class.
 * <p>
 * NOTE: USE OF THIS API REQUIRES A USER WITH <b>ADMINISTRATOR</b> ACCESS IF THE <b>DB</b> USED FOR
 * THE GRAPH DOES NOT EXIST. As per ArangoDB, creating DB is only allowed for the root user, hence
 * only the root user can be used if the DB does not exist.
 * <p>
 * <b>ArangoDB and TinkerPop Ids.</b>
 * <p>
 * In TinkerPop, graph elements are expected to have a unique Id within the graph; in ArangoDB the
 * Id (document handle) consists of the collection's name and the document name (_key attribute)
 * separated by /, hence the only way to hint at ids is by providing a _key during construction.
 * Hence, ArangoDBGraph elements do not strictly support <i>User Supplied Ids</i>. We allow
 * ids to be supplied during vertex creation: {@code graph.addVertex(id,x)}, but this id actually
 * represents the _key. As a result, posterior search/match by id must prefix the vertex's label
 * (collection) followed by a /.
 * <p>
 * An ArangoDBGraph is instantiated from an Apache Commons Configuration instance. The configuration
 * must provide both TinkerPop and ArangoDB configuration options. The ArangoDB options are
 * described in the ArangoDB Java Driver <a href="https://github.com/arangodb/arangodb-java-driver/blob/master/docs/Drivers/Java/Reference/Setup.md">documentation.</a>
 * <p>
 * For the TinkerPop part, the configuration must provide as a minimum the database name and the
 * graph name. If no vertex, edge and relation information is provided, the graph will be considered
 * schema-less.
 * <p>
 * All settings are prefixed with "gremlin.arangodb.conf". So, for example, to set the value of the
 * Arango DB hosts property (arango db configuration), the configuration must read:
 * <pre>gremlin.arangodb.conf.arangodb.hosts = 127.0.0.1:8529
 * </pre>
 * while for the db name (graph configuration) it will be:
 * <pre>gremlin.arangodb.conf.graph.db = myDB
 * </pre>
 * <p>
 * To define the schema, (EdgeCollections in ArangoDB world) three properties can be used:
 * <code>graph.vertex</code>, <code>graph.edge</code> and <code>graph.relation</code>. The graph.vertex and
 * graph.edge properties allow definition of the ArangoDB collections used to store nodes and edges
 * respectively. The relations property is used to describe the allowed edge-node relations. For
 * simple graphs, only one graph.vertex and graph.edge properties need to be provided. In this case
 * edges are allowed to connect to any two nodes. For example:
 * <pre>gremlin.arangodb.conf.graph.vertex = Place
 * gremlin.arangodb.conf.graph.edge = Transition
 * </pre>
 * would allow the user to create Vertices that represent Places, and Edges that represent
 * Transitions. A transition can be created between any two Places. If additional vertices and edges
 * were added, the resulting graph schema would be fully connected, that is, edges would be allowed
 * between any two pair of vertices.
 * <p>
 * For more complex graph structures, the graph.relation property is used to tell the ArangoDB what
 * relations are allowed, e.g.:
 * <ul>
 * <li>One-to-one edges
 * <pre>gremlin.arangodb.conf.graph.vertex = Place
 * gremlin.arangodb.conf.graph.vertex = Transition
 * gremlin.arangodb.conf.graph.edge = PTArc
 * gremlin.arangodb.conf.graph.edge = TPArc
 * gremlin.arangodb.conf.graph.relation = PTArc:Place-&gt;Transition
 * gremlin.arangodb.conf.graph.relation = TPArc:Transition-&gt;Place
 * </pre>
 * would allow the user to create nodes to represent Places and Transitions, and edges to represent
 * Arcs. However, in this case, we have two type of arcs: PTArc and TPArc. The relations specify
 * that PTArcs can only go from Place to Transitions and TPArcs can only go from Transitions to
 * Places. A relation can also specify multiple to/from nodes. In this case, the to/from values is a
 * comma separated list of names.
 * <li>Many-to-many edges
 *  <pre>gremlin.arangodb.conf.graph.vertex = male
 * gremlin.arangodb.conf.graph.vertex = female
 * gremlin.arangodb.conf.graph.edge = relation
 * gremlin.arangodb.conf.graph.relation = relation:male,female-&gt;male,female
 *  </pre>
 * </ul>
 * <p>
 * In order to allow multiple graphs in the same database, vertex and edge collections are prefixed with the
 * graph name in order to avoid collection clashes.
 * <p>
 * The list of allowed settings is:
 * <ul>
 *   <li>  graph.db 								// The name of the database
 *   <li>  graph.name 								// The name of the graph
 *   <li>  graph.vertex 							// The name of a vertices collection
 *   <li>  graph.edge 								// The name of an edges collection
 *   <li>  graph.relation 							// The allowed from/to relations for edges
 *   <li>  arangodb.hosts
 *   <li>  arangodb.timeout
 *   <li>  arangodb.user
 *   <li>  arangodb.password
 *   <li>  arangodb.usessl
 *   <li>  arangodb.chunksize
 *   <li>  arangodb.connections.max
 *   <li>  arangodb.protocol
 *   <li>  arangodb.acquireHostList
 *   <li>  arangodb.loadBalancingStrategy
 * </ul>
 *
 * @author Achim Brandt (http://www.triagens.de)
 * @author Johannes Gocke (http://www.triagens.de)
 * @author Guido Schwab (http://www.triagens.de)
 * @author Horacio Hoyos Rodriguez (https://www.york.ac.uk)
 */

public class ArangoDBGraph implements Graph {
    public static final String GRAPH_VARIABLES_COLLECTION = "TINKERPOP-GRAPH-VARIABLES";
    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoDBGraph.class);
    private static final Features FEATURES = new ArangoDBGraphFeatures();

    private final ArangoDBGraphClient client;
    private final ElementIdFactory idFactory;
    private final ArangoDBGraphConfig config;

    /**
     * Create a new ArangoDBGraph from the provided configuration.
     *
     * @param configuration the Apache Commons configuration
     * @return the Arango DB graph
     */

    public static ArangoDBGraph open(Configuration configuration) {
        return new ArangoDBGraph(configuration);
    }

    /**
     * Creates a Graph (simple configuration).
     *
     * @param cfg the Apache Commons configuration
     */

    public ArangoDBGraph(Configuration cfg) {
        LOGGER.info("Creating new ArangoDB Graph from configuration");
        config = new ArangoDBGraphConfig(cfg);
        idFactory = new ElementIdFactory(config);
        client = new ArangoDBGraphClient(config, idFactory);

        ArangoGraph graph = client.getArangoGraph();
        if (graph.exists()) {
            ArangoDBUtil.checkExistingGraph(graph.getInfo(), config);
        } else {
            client.createGraph(name(), config.edgeDefinitions, config.orphanCollections);
        }

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

    public ArangoDBGraphConfig.GraphType type() {
        return config.graphType;
    }

    @Override
    public Vertex addVertex(Object... keyValues) {
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        String label = ElementHelper.getLabelValue(keyValues).orElse(null);
        ElementId id = idFactory.createVertexId(label, keyValues);
        ArangoDBVertex vertex = ArangoDBVertex.of(label, id, this);
        if (!config.vertices.contains(vertex.collection())) {
            throw new IllegalArgumentException(String.format("Vertex collection (%s) not in graph (%s).", vertex.collection(), name()));
        }

        // TODO: optimize writing only once
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

    /**
     * Returns the ArangoDBGraphClient object.
     *
     * @return the ArangoDBGraphClient object
     */

    public ArangoDBGraphClient getClient() {
        return client;
    }

    public ElementIdFactory getIdFactory() {
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

    public String getPrefixedCollectionName(String collectionName) {
        if (collectionName.startsWith(config.graphName + "_")) {
            return collectionName;
        }
        return config.graphName + "_" + collectionName;
    }

    public static class ArangoDBGraphFeatures implements Features {

        protected static class ArangoDBGraphGraphFeatures implements GraphFeatures {

            @Override
            public boolean supportsComputer() {
                return false;
            }

            @Override
            public boolean supportsThreadedTransactions() {
                return false;
            }

            @Override
            public boolean supportsTransactions() {
                return false;
            }
        }

        protected static class ArangoDBGraphElementFeatures implements ElementFeatures {

            @Override
            public boolean supportsAnyIds() {
                return false;
            }

            @Override
            public boolean supportsCustomIds() {
                return false;
            }

            @Override
            public boolean supportsNumericIds() {
                return false;
            }

            @Override
            public boolean supportsUuidIds() {
                return false;
            }
        }

        protected static class ArangoDBGraphVertexFeatures extends ArangoDBGraphElementFeatures implements VertexFeatures {

            @Override
            public VertexPropertyFeatures properties() {
                return new ArangoDBGraphVertexPropertyFeatures();
            }
        }

        public static class ArangoDBGraphEdgeFeatures extends ArangoDBGraphElementFeatures implements EdgeFeatures {
        }

        protected static class ArangoDBGraphVertexPropertyFeatures implements VertexPropertyFeatures {

            @Override
            public boolean supportsAnyIds() {
                return false;
            }

            @Override
            public boolean supportsCustomIds() {
                return false;
            }

            @Override
            public boolean supportsNumericIds() {
                return false;
            }

            @Override
            public boolean supportsUuidIds() {
                return false;
            }
        }

        @Override
        public GraphFeatures graph() {
            return new ArangoDBGraphGraphFeatures();
        }

        @Override
        public VertexFeatures vertex() {
            return new ArangoDBGraphVertexFeatures();
        }

        @Override
        public EdgeFeatures edge() {
            return new ArangoDBGraphEdgeFeatures();
        }

        @Override
        public String toString() {
            return StringFactory.featureString(this);
        }
    }

}
