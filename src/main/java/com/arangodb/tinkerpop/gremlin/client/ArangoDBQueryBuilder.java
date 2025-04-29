/// ///////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the TinkerPop OLTP Provider API for ArangoDB
//
// Copyright triAGENS GmbH Cologne and The University of York
//
/// ///////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.tinkerpop.gremlin.client;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.arangodb.tinkerpop.gremlin.persistence.ElementId;
import org.apache.tinkerpop.gremlin.structure.Direction;


public class ArangoDBQueryBuilder {

    private ArangoDBQueryBuilder() {
    }

    public static String readVertexNeighbors(String graphName, ElementId vertexId, Set<String> edgeCollections, Direction direction, String[] labels) {
        return oneStepTraversal(graphName, vertexId, edgeCollections, direction, labels)
                .append(" RETURN v")
                .toString();
    }

    public static String readVertexEdges(String graphName, ElementId vertexId, Set<String> edgeCollections, Direction direction, String[] labels) {
        return oneStepTraversal(graphName, vertexId, edgeCollections, direction, labels)
                .append(" RETURN e")
                .toString();
    }

    private static StringBuilder oneStepTraversal(String graphName, ElementId vertexId, Set<String> edgeCollections, Direction direction, String[] labels) {
        StringBuilder query = new StringBuilder()
                .append("FOR v, e IN 1..1 ")
                .append(toArangoDirection(direction))
                .append(" ")
                .append(quote(vertexId.toJson()))
                .append(" GRAPH ")
                .append(escape(graphName))
                .append(" OPTIONS {edgeCollections: ")
                .append(edgeCollections.stream()
                        .map(ArangoDBQueryBuilder::quote)
                        .collect(Collectors.joining(",", "[", "]")))
                .append("}");
        if (labels.length > 0) {
            query
                    .append(" FILTER e.label IN ")
                    .append(Arrays.stream(labels)
                            .map(ArangoDBQueryBuilder::quote)
                            .collect(Collectors.joining(",", "[", "]")));
        }
        return query;
    }

    public static String readDocumentsByIds(List<ElementId> ids) {
        String idsArray = ids.stream()
                .map(ElementId::toJson)
                .map(ArangoDBQueryBuilder::quote)
                .collect(Collectors.joining(",", "[", "]"));
        return String.format("FOR d IN DOCUMENT(%s) RETURN d", idsArray);
    }

    public static String readAllDocuments(Set<String> collections) {
        if (collections.isEmpty()) {
            throw new IllegalArgumentException();
        } else if (collections.size() == 1) {
            return readAllDocumentsFromSingleCollection(collections.iterator().next());
        } else {
            return readAllDocumentsFromMultipleCollections(collections);
        }
    }

    private static String readAllDocumentsFromMultipleCollections(Set<String> collections) {
        String inner = collections.stream()
                .map(ArangoDBQueryBuilder::readAllDocumentsFromSingleCollection)
                .map(it -> "(" + it + ")")
                .collect(Collectors.joining(","));
        return String.format("FOR d in UNION(%s) RETURN d", inner);
    }

    private static String readAllDocumentsFromSingleCollection(String collection) {
        return String.format("FOR x IN %s RETURN x", escape(collection));
    }

    private static String escape(String collection) {
        return String.format("`%s`", collection);
    }

    private static String quote(String id) {
        return String.format("\"%s\"", id);
    }

    private static String toArangoDirection(final Direction direction) {
        switch (direction) {
            case BOTH:
                return "ANY";
            case IN:
                return "INBOUND";
            case OUT:
                return "OUTBOUND";
        }
        throw new IllegalArgumentException("Unsupported direction: " + direction);
    }

}
