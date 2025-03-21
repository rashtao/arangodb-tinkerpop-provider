/// ///////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the TinkerPop OLTP Provider API for ArangoDB
//
// Copyright triAGENS GmbH Cologne and The University of York
//
/// ///////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.tinkerpop.gremlin.structure;

import java.util.*;
import java.util.stream.Collectors;

import com.arangodb.entity.EdgeDefinition;
import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import com.arangodb.tinkerpop.gremlin.persistence.ElementIdFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoGraph;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.tinkerpop.gremlin.client.ArangoDBGraphClient;
import com.arangodb.tinkerpop.gremlin.client.ArangoDBGraphException;
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
 * In order to allow multiple graphs in the same database, vertex and edge collections can be prefixed with the
 * graph name in order to avoid collection clashes. To enable this function the graph.shouldPrefixCollectionNames
 * property should be set to <code>true</code>. If you have an existing graph/collections and want to reuse those,
 * the flag should be set to <code>false</code>. The default value is <code>true</code>.
 * <p>
 * The list of allowed settings is:
 * <ul>
 *   <li>  graph.db 								// The name of the database
 *   <li>  graph.name 								// The name of the graph
 *   <li>  graph.vertex 							// The name of a vertices collection
 *   <li>  graph.edge 								// The name of an edges collection
 *   <li>  graph.relation 							// The allowed from/to relations for edges
 *   <li>  graph.shouldPrefixCollectionNames 		// Boolean flag, true if Vertex and Edge collections will be prefixed with graph name
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

    public static class ArangoDBGraphFeatures implements Features {

        protected static class ArangoDBGraphGraphFeatures implements GraphFeatures {

            protected ArangoDBGraphGraphFeatures() {
            }

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

            @Override
            public VariableFeatures variables() {
                return new ArangoDBGraphVariables.ArangoDBGraphVariableFeatures();
            }
        }

        protected static class ArangoDBGraphElementFeatures implements ElementFeatures {

            protected ArangoDBGraphElementFeatures() {
            }

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

            protected ArangoDBGraphVertexFeatures() {
            }

            @Override
            public VertexPropertyFeatures properties() {
                return new ArangoDBGraphVertexPropertyFeatures();
            }
        }

        public static class ArangoDBGraphEdgeFeatures extends ArangoDBGraphElementFeatures implements EdgeFeatures {

            protected ArangoDBGraphEdgeFeatures() {
            }

            @Override
            public EdgePropertyFeatures properties() {
                return new ArangoDBGraphEdgePropertyFeatures();
            }
        }

        protected static class ArangoDBGraphVertexPropertyFeatures implements VertexPropertyFeatures {

            protected ArangoDBGraphVertexPropertyFeatures() {
            }

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

        protected static class ArangoDBGraphEdgePropertyFeatures implements EdgePropertyFeatures {

            protected ArangoDBGraphEdgePropertyFeatures() {
            }

        }

        @Override
        public EdgeFeatures edge() {
            return new ArangoDBGraphEdgeFeatures();
        }

        @Override
        public GraphFeatures graph() {
            return new ArangoDBGraphGraphFeatures();
        }

        @Override
        public String toString() {
            return StringFactory.featureString(this);
        }

        @Override
        public VertexFeatures vertex() {
            return new ArangoDBGraphVertexFeatures();
        }
    }

    /**
     * The Logger.
     */

    private static final Logger logger = LoggerFactory.getLogger(ArangoDBGraph.class);

    /**
     * The properties name CONFIG_CONF.
     */

    public static final String PROPERTY_KEY_PREFIX = "gremlin.arangodb.conf";

    /**
     * The properties name  CONFIG_DB.
     */

    public static final String PROPERTY_KEY_DB_NAME = "graph.db";

    /**
     * The properties name  CONFIG_NAME.
     */

    public static final String PROPERTY_KEY_GRAPH_NAME = "graph.name";

    /**
     * The properties name CONFIG_VERTICES.
     */

    public static final String PROPERTY_KEY_VERTICES = "graph.vertex";

    /**
     * The properties name CONFIG_EDGES.
     */

    public static final String PROPERTY_KEY_EDGES = "graph.edge";

    /**
     * The properties name CONFIG_RELATIONS.
     */

    public static final String PROPERTY_KEY_RELATIONS = "graph.relation";

    /**
     * The properties name CONFIG_SHOULD_PREFIX_COLLECTION_NAMES
     **/

    public static final String PROPERTY_KEY_SHOULD_PREFIX_COLLECTION_NAMES = "graph.shouldPrefixCollectionNames";

    /**
     * The properties name SIMPLE_GRAPH
     **/

    public static final String SIMPLE_GRAPH = "graph.simple";

    /**
     * The Constant DEFAULT_VERTEX_COLLECTION.
     */

    public static final String DEFAULT_VERTEX_COLLECTION = "vertex";

    /**
     * The Constant DEFAULT_VERTEX_COLLECTION.
     */

    public static final String DEFAULT_EDGE_COLLECTION = "edge";

    /**
     * The Constant GRAPH_VARIABLES_COLLECTION.
     */

    public static final String GRAPH_VARIABLES_COLLECTION = "TINKERPOP-GRAPH-VARIABLES";

    /**
     * The Constant ELEMENT_PROPERTIES_COLLECTION.
     */

    public static final String ELEMENT_PROPERTIES_COLLECTION = "ELEMENT-PROPERTIES";

    /**
     * The Constant ELEMENT_PROPERTIES_EDGE_COLLECTION.
     */

    public static final String ELEMENT_PROPERTIES_EDGE_COLLECTION = "ELEMENT-HAS-PROPERTIES";

    public static Set<String> GRAPH_COLLECTIONS = new HashSet<>(Arrays.asList(ELEMENT_PROPERTIES_EDGE_COLLECTION, ELEMENT_PROPERTIES_COLLECTION));

    /**
     * The features.
     */

    private final Features FEATURES = new ArangoDBGraphFeatures();

    /**
     * A ArangoDBGraphClient to handle the connection to the Database.
     */

    private final ArangoDBGraphClient client;

    private final ElementIdFactory idFactory;

    /**
     * The name.
     */

    private String name;

    /**
     * The vertex collections.
     */

    private final List<String> vertexCollections;

    public final List<String> prefixedVertexCollections;

    /**
     * The edge collections.
     */

    public final List<String> edgeCollections;

    public final List<String> prefixedEdgeCollections;

    /**
     * The relations.
     */

    private final List<String> relations;

    /**
     * The configuration.
     */

    private final Configuration configuration;


    /**
     * If collection names should be prefixed with graph name
     */
    private final boolean shouldPrefixCollectionNames;

    public final boolean simpleGraph;

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
     * @param configuration the Apache Commons configuration
     */

    public ArangoDBGraph(Configuration configuration) {

        logger.info("Creating new ArangoDB Graph from configuration");
        Configuration arangoConfig = configuration.subset(PROPERTY_KEY_PREFIX);
        name = arangoConfig.getString(PROPERTY_KEY_GRAPH_NAME);
        shouldPrefixCollectionNames = arangoConfig.getBoolean(PROPERTY_KEY_SHOULD_PREFIX_COLLECTION_NAMES, true);
        vertexCollections = arangoConfig.getList(PROPERTY_KEY_VERTICES).stream()
                .map(String.class::cast)
                .collect(Collectors.toList());
        if (vertexCollections.isEmpty()) {
            logger.warn("Empty vertexes collection(s), the default 'vertex' collection will be used.");
            vertexCollections.add(DEFAULT_VERTEX_COLLECTION);
        }
        prefixedVertexCollections = vertexCollections.stream()
                .map(this::getPrefixedCollectionName)
                .collect(Collectors.toList());
        edgeCollections = arangoConfig.getList(PROPERTY_KEY_EDGES).stream()
                .map(String.class::cast)
                .collect(Collectors.toList());
        if (edgeCollections.isEmpty()) {
            logger.warn("Empty edges collection(s), the default 'edge' collection will be used.");
            edgeCollections.add(DEFAULT_EDGE_COLLECTION);
        }
        prefixedEdgeCollections = edgeCollections.stream()
                .map(this::getPrefixedCollectionName)
                .collect(Collectors.toList());
        relations = arangoConfig.getList(PROPERTY_KEY_RELATIONS).stream()
                .map(String.class::cast)
                .collect(Collectors.toList());
        checkValues(arangoConfig.getString(PROPERTY_KEY_DB_NAME), name, vertexCollections, edgeCollections, relations);
        simpleGraph = arangoConfig.getBoolean(SIMPLE_GRAPH, false);

        Properties arangoProperties = ConfigurationConverter.getProperties(arangoConfig);
        client = new ArangoDBGraphClient(this, arangoProperties, arangoConfig.getString(PROPERTY_KEY_DB_NAME));
        idFactory = new ElementIdFactory(name, simpleGraph, Vertex.DEFAULT_LABEL, Edge.DEFAULT_LABEL);

        final List<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        final List<String> prefVCols;
        final List<String> prefECols;
        if (simpleGraph) {
            prefVCols = Collections.singletonList(getPrefixedCollectionName("vertex"));
            prefECols = Collections.singletonList(getPrefixedCollectionName("edge"));
            edgeDefinitions.addAll(ArangoDBUtil.createDefaultEdgeDefinitions(prefVCols, prefECols));
        } else {
            // FIXME Cant be in orphan collections because it will be deleted with graph?
            // options.orphanCollections(GRAPH_VARIABLES_COLLECTION);
            prefVCols = vertexCollections.stream().map(this::getPrefixedCollectionName).collect(Collectors.toList());
            prefECols = edgeCollections.stream().map(this::getPrefixedCollectionName).collect(Collectors.toList());
            if (relations.isEmpty()) {
                logger.info("No relations, creating default ones.");
                edgeDefinitions.addAll(ArangoDBUtil.createDefaultEdgeDefinitions(prefVCols, prefECols));
            } else {
                for (String value : relations) {
                    EdgeDefinition ed = ArangoDBUtil.relationPropertyToEdgeDefinition(this, value);
                    edgeDefinitions.add(ed);
                }
            }
            edgeDefinitions.add(ArangoDBUtil.createPropertyEdgeDefinitions(this, prefVCols, prefECols));
        }

        ArangoGraph graph = client.getArangoGraph();
        GraphCreateOptions options = new GraphCreateOptions();
        if (graph.exists()) {
            ArangoDBUtil.checkGraphForErrors(prefVCols, prefECols, edgeDefinitions, graph, options);
            ArangoDBGraphVariables variables = null;
            try {
                variables = client.getGraphVariables();
            } catch (NullPointerException ex) {
                logger.warn("Existing graph missing Graph Variables collection ({}), will attempt to create one.", GRAPH_VARIABLES_COLLECTION);
            }
            if (variables == null) {
                variables = new ArangoDBGraphVariables(name, GRAPH_VARIABLES_COLLECTION, this);
                try {
                    client.insertGraphVariables(variables);
                } catch (ArangoDBGraphException ex) {
                    throw new ArangoDBGraphException(
                            String.format(
                                    "Unable to add graph variables collection (%s) to existing graph. %s",
                                    ex.getMessage(),
                                    GRAPH_VARIABLES_COLLECTION)
                            , ex);
                }
            }
        } else {
            graph = client.createGraph(name, edgeDefinitions, options);
            this.name = graph.name();
            ArangoDBGraphVariables variables = new ArangoDBGraphVariables(name, GRAPH_VARIABLES_COLLECTION, this);
            client.insertGraphVariables(variables);
        }
        this.configuration = configuration;
    }

    @Override
    public Vertex addVertex(Object... keyValues) {
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        String label = ElementHelper.getLabelValue(keyValues).orElse(null);
        ElementId id = idFactory.createVertexId(features().vertex(), label, keyValues);
        ArangoDBVertex vertex = ArangoDBVertex.of(label, id, this);
        if (!prefixedVertexCollections.contains(vertex.collection())) {
            throw new IllegalArgumentException(String.format("Vertex collection (%s) not in graph (%s).", vertex.collection(), name));
        }

        // TODO: optmize writing only once
        vertex.doInsert();
        ElementHelper.attachProperties(vertex, keyValues);
        return vertex;
    }

    /**
     * Check that the configuration values are sound.
     *
     * @param db        the db
     * @param name      the name
     * @param vertices  the vertices
     * @param edges     the edges
     * @param relations the relations
     */

    private void checkValues(
            String db,
            String name,
            List<String> vertices,
            List<String> edges,
            List<String> relations) {

        if (StringUtils.isBlank(db)) {
            throw new ArangoDBGraphException("The db name can not be empty/null. Check that your configuration file " +
                    "has a 'graph.db' setting.");
        }
        if (StringUtils.isBlank(name)) {
            throw new ArangoDBGraphException("The graph name can not be empty/null. Check that your configuration file " +
                    "has a 'graph.name' name setting.");
        }
        if ((vertices.size() > 1) && (edges.size() > 1) && CollectionUtils.isEmpty(relations)) {
            throw new ArangoDBGraphException("If more than one vertex/edge collection is provided, relations must be defined");
        }
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
        return configuration;
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

    /**
     * Returns the identifier of the graph.
     *
     * @return the identifier of the graph
     */

    public String getId() {
        ArangoGraph graph = client.getArangoGraph();
        return graph.getInfo().getName();
    }

    /**
     * The graph name
     *
     * @return the name
     */

    public String name() {
        return this.name;
    }

    @Override
    public Transaction tx() {
        throw Graph.Exceptions.transactionsNotSupported();
    }

    @Override
    public Variables variables() {
        ArangoDBGraphVariables v = client.getGraphVariables();
        if (v != null) {
            v.graph(this);
            return v;
        } else {
            throw new ArangoDBGraphException("Existing graph does not have a Variables collection");
        }
    }

    /**
     * Return the collection name correctly prefixed according to the shouldPrefixCollectionNames flag
     *
     * @param collectionName the collection name
     * @return the Collection name prefixed
     */
    public String getPrefixedCollectionName(String collectionName) {
        if (!shouldPrefixCollectionNames) {
            return collectionName;
        }

        if (collectionName.startsWith(name + "_")) {
            return collectionName;
        }
        return name + "_" + collectionName;
    }

    @Override
    public String toString() {
        String vertices = vertexCollections.stream()
                .map(vc -> String.format("\"%s\"", vc))
                .collect(Collectors.joining(", ", "{", "}"));
        String edges = edgeCollections.stream()
                .map(vc -> String.format("\"%s\"", vc))
                .collect(Collectors.joining(", ", "{", "}"));
        String relations = relations().stream()
                .map(vc -> String.format("\"%s\"", vc))
                .collect(Collectors.joining(", ", "{", "}"));
        String internal = "{"
                + "\"name\":\"" + name() + "\","
                + "\"vertices\":" + vertices + ","
                + "\"edges\":" + edges + ","
                + "\"relations\":" + relations
                + "}";
        return StringFactory.graphString(this, internal);
    }

    /**
     * The graph relations.
     *
     * @return the collection of relations
     */
    private Collection<String> relations() {
        return relations;
    }

}
