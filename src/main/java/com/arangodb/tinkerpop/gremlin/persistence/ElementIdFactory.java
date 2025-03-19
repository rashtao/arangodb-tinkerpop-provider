package com.arangodb.tinkerpop.gremlin.persistence;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.arangodb.tinkerpop.gremlin.structure.ArangoDBElement.Exceptions.unsupportedIdType;

public class ElementIdFactory {
    private final String prefix;
    private final boolean simpleGraph;
    private final String defaultVertexLabel;
    private final String defaultEdgeLabel;

    public ElementIdFactory(String prefix, boolean simpleGraph, String defaultVertexLabel, String defaultEdgeLabel) {
        this.prefix = prefix;
        this.simpleGraph = simpleGraph;
        this.defaultVertexLabel = defaultVertexLabel;
        this.defaultEdgeLabel = defaultEdgeLabel;
    }

    private String extractKey(final String id) {
        String[] parts = id.split("/");
        return parts[parts.length - 1];
    }

    private String extractCollection(final String id) {
        String[] parts = id.replaceFirst("^" + prefix + "_", "").split("/");
        return parts.length == 2 ? parts[0] : null;
    }

    private String inferCollection(final String collection, final String label, final String defaultLabel) {
        if (simpleGraph) {
            return defaultLabel;
        }
        if (collection != null) {
            if (label != null && !label.equals(collection)) {
                throw new IllegalArgumentException("Mismatching label: [" + label + "] and collection: [" + collection + "]");
            }
            return collection;
        }
        if (label != null) {
            return label;
        }
        return defaultLabel;
    }

    public ElementId createVertexId(Graph.Features.ElementFeatures features, String label, Object[] keyValues) {
        return createId(features, label, defaultVertexLabel, keyValues);
    }

    public ElementId createEdgeId(Graph.Features.ElementFeatures features, String label, Object[] keyValues) {
        return createId(features, label, defaultEdgeLabel, keyValues);
    }

    public ElementId parseVertexId(Object id) {
        if (id instanceof String) {
            return parseWithDefaultLabel((String) id, defaultVertexLabel);
        } else if (id instanceof Element) {
            return parseVertexId(((Element) id).id());
        } else {
            throw unsupportedIdType(id);
        }
    }

    public List<ElementId> parseVertexIds(Object[] ids) {
        return Arrays.stream(ids)
                .map(this::parseVertexId)
                .collect(Collectors.toList());
    }

    public ElementId parseEdgeId(Object id) {
        if (id instanceof String) {
            return parseWithDefaultLabel((String) id, defaultEdgeLabel);
        } else if (id instanceof Element) {
            return parseEdgeId(((Element) id).id());
        } else {
            throw unsupportedIdType(id);
        }
    }

    public List<ElementId> parseEdgeIds(Object[] ids) {
        return Arrays.stream(ids)
                .map(this::parseEdgeId)
                .collect(Collectors.toList());
    }

    public ElementId parse(String id) {
        String collection = extractCollection(id);
        String key = extractKey(id);
        return of(prefix, collection, key);
    }

    private ElementId parseWithDefaultLabel(String id, String defaultLabel) {
        String collection = inferCollection(extractCollection(id), null, defaultLabel);
        String key = extractKey(id);
        return of(prefix, collection, key);
    }

    private ElementId createId(Graph.Features.ElementFeatures features, String label, String defaultLabel, Object[] keyValues) {
        Optional<Object> optionalId = ElementHelper.getIdValue(keyValues);
        if (!optionalId.isPresent()) {
            return of(prefix, inferCollection(null, label, defaultLabel), null);
        }
        String id = optionalId
                .filter(features::willAllowId)
                .map(Object::toString)
                .orElseThrow(Vertex.Exceptions::userSuppliedIdsOfThisTypeNotSupported);
        return of(prefix, inferCollection(extractCollection(id), label, defaultLabel), extractKey(id));
    }

    private ElementId of(String prefix, String collection, String key) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(collection);
        ElementId.validateIdParts(prefix, collection, key);
        return simpleGraph ?
                new SimpleId(prefix, collection, key) :
                new ArangoId(prefix, collection, key);
    }

}
