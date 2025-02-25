/// ///////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the TinkerPop OLTP Provider API for ArangoDB
//
// Copyright triAGENS GmbH Cologne and The University of York
//
/// ///////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.tinkerpop.gremlin.structure;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.tinkerpop.gremlin.client.ArangoDBGraphException;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;

import static com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil.*;


/**
 * The ArangoDB vertex class.
 *
 * @author Achim Brandt (http://www.triagens.de)
 * @author Johannes Gocke (http://www.triagens.de)
 * @author Guido Schwab (http://www.triagens.de)
 * @author Horacio Hoyos Rodriguez (https://www.york.ac.uk)
 */

public class ArangoDBVertex extends ArangoDBElement<ArangoDBVertexData> implements Vertex {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoDBVertex.class);

    public ArangoDBVertex(ArangoDBGraph graph, ArangoDBVertexData data) {
        super(graph, data);
    }

    public ArangoDBVertex(final String id, final String label, ArangoDBGraph graph) {
        this(graph, new ArangoDBVertexData(extractLabel(id, label).orElse(DEFAULT_LABEL), extractKey(id)));
    }

    @Override
    public <V> VertexProperty<V> property(
            final Cardinality cardinality,
            final String key,
            final V value,
            final Object... keyValues
    ) {
        if (removed) throw elementAlreadyRemoved(Vertex.class, id());
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        ElementHelper.validateProperty(key, value);

        Optional<VertexProperty<V>> optionalVertexProperty = ElementHelper.stageVertexProperty(this, cardinality, key, value, keyValues);
        if (optionalVertexProperty.isPresent()) return optionalVertexProperty.get();

        String idValue = ElementHelper.getIdValue(keyValues)
                .map(it -> {
                    if (!graph.features().vertex().properties().willAllowId(it)) {
                        throw VertexProperty.Exceptions.userSuppliedIdsOfThisTypeNotSupported();
                    }
                    return it.toString();
                })
                .orElseGet(() -> UUID.randomUUID().toString());

        ArangoDBVertexPropertyData prop = new ArangoDBVertexPropertyData(idValue, value);
        List<ArangoDBVertexPropertyData> list = data.getProperties().getOrDefault(key, new ArrayList<>());
        list.add(prop);
        data.getProperties().put(key, list);

        ArangoDBVertexProperty<V> vertexProperty = new ArangoDBVertexProperty<>(key, prop, this);
        ElementHelper.attachProperties(vertexProperty, keyValues);
        update();
        return vertexProperty;
    }

    // TODO: review
    @Override
    public Edge addEdge(String label, Vertex vertex, Object... keyValues) {
        if (null == vertex) throw Graph.Exceptions.argumentCanNotBeNull("vertex");
        if (this.removed || ((ArangoDBVertex) vertex).removed) throw elementAlreadyRemoved(Vertex.class, id());

        LOGGER.trace("addEdge in collection {} to vertex {}", label, vertex.id());
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        ElementHelper.validateLabel(label);

        if (!graph.edgeCollections().contains(label)) {
            throw new IllegalArgumentException(String.format("Edge label (%s)not in graph (%s) edge collections.", label, graph.name()));
        }

        Object id;
        ArangoDBEdge edge;
        if (ElementHelper.getIdValue(keyValues).isPresent()) {
            id = ElementHelper.getIdValue(keyValues).get();
            if (graph.features().edge().willAllowId(id)) {
                if (id.toString().contains("/")) {
                    String fullId = id.toString();
                    String[] parts = fullId.split("/");
                    // The collection name is the last part of the full name
                    String[] collectionParts = parts[0].split("_");
                    String collectionName = collectionParts[collectionParts.length - 1];
                    if (collectionName.contains(label)) {
                        id = parts[1];

                    }
                }
                Matcher m = ArangoDBUtil.DOCUMENT_KEY.matcher((String) id);
                if (m.matches()) {
                    edge = new ArangoDBEdge(id.toString(), label, this.id(), (String) vertex.id(), graph);
                } else {
                    throw new ArangoDBGraphException(String.format("Given id (%s) has unsupported characters.", id));
                }
            } else {
                throw Vertex.Exceptions.userSuppliedIdsOfThisTypeNotSupported();
            }
        } else {
            edge = new ArangoDBEdge(null, label, this.id(), (String) vertex.id(), graph);
        }
        edge.insert();
        ElementHelper.attachProperties(edge, keyValues);
        return edge;
    }

    @Override
    public void remove() {
        if (removed) return;
        LOGGER.trace("removing {} from graph {}.", id(), graph.name());
        edges(Direction.BOTH).forEachRemaining(Edge::remove);
        graph.getClient().deleteVertex(data);
        this.removed = true;
    }

    @Override
    public String toString() {
        return StringFactory.vertexString(this);
    }

    @Override
    public Iterator<Edge> edges(Direction direction, String... edgeLabels) {
        List<String> edgeCollections = getQueryEdgeCollections(edgeLabels);
        // If edgeLabels was not empty but all were discarded, this means that we should
        // return an empty iterator, i.e. no edges for that edgeLabels exist.
        if (edgeCollections.isEmpty()) {
            return Collections.emptyIterator();
        }
        return IteratorUtils.map(graph.getClient().getVertexEdges(id(), edgeCollections, direction), it -> new ArangoDBEdge(graph, it));
    }


    @Override
    public Iterator<Vertex> vertices(Direction direction, String... edgeLabels) {
        List<String> edgeCollections = getQueryEdgeCollections(edgeLabels);
        // If edgeLabels was not empty but all were discarded, this means that we should
        // return an empty iterator, i.e. no edges for that edgeLabels exist.
        if (edgeCollections.isEmpty()) {
            return Collections.emptyIterator();
        }
        return IteratorUtils.map(graph.getClient().getVertexNeighbors(id(), edgeCollections, direction), it -> new ArangoDBVertex(graph, it));
    }


    @SuppressWarnings("unchecked")
    @Override
    public <V> Iterator<VertexProperty<V>> properties(String... propertyKeys) {
        if (this.removed) return Collections.emptyIterator();
        return allProperties()
                .filter(it -> ElementHelper.keyExists(it.key(), propertyKeys))
                .map(it -> (VertexProperty<V>) it)
                .iterator();
    }

    private Stream<ArangoDBVertexProperty<?>> allProperties() {
        return data.getProperties().entrySet().stream()
                .flatMap(x -> x.getValue().stream()
                        .map(y -> new ArangoDBVertexProperty<>(x.getKey(), y, this))
                );
    }

    public void insert() {
        if (removed) throw elementAlreadyRemoved(Vertex.class, id());
        graph.getClient().insertVertex(data);
    }


    public void update() {
        if (removed) throw elementAlreadyRemoved(Vertex.class, id());
        graph.getClient().updateVertex(data);
    }

    public void removeProperty(ArangoDBVertexPropertyData prop) {
        if (removed) throw elementAlreadyRemoved(Vertex.class, id());
        Map<String, List<ArangoDBVertexPropertyData>> props = data.getProperties();
        for (Map.Entry<String, List<ArangoDBVertexPropertyData>> p : props.entrySet()) {
            List<ArangoDBVertexPropertyData> pVal = p.getValue();
            if (pVal.remove(prop)) {
                if (pVal.isEmpty()) props.remove(p.getKey());
                break;
            }
        }
        update();
    }

    /**
     * Query will raise an exception if the edge_collection name is not in the graph, so we need to filter out
     * edgeLabels not in the graph.
     *
     * @param edgeLabels
     * @return
     */
    private List<String> getQueryEdgeCollections(String... edgeLabels) {
        List<String> vertexCollections;
        if (edgeLabels.length == 0) {
            vertexCollections = graph.edgeCollections().stream().map(graph::getPrefixedCollectioName).collect(Collectors.toList());
        } else {
            vertexCollections = Arrays.stream(edgeLabels)
                    .filter(el -> graph.edgeCollections().contains(el))
                    .map(graph::getPrefixedCollectioName)
                    .collect(Collectors.toList());

        }
        return vertexCollections;
    }

}

