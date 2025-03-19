package com.arangodb.tinkerpop.gremlin.persistence;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;

import java.util.Arrays;
import java.util.List;
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
        if (id == null) {
            return null;
        }
        int separator = id.indexOf('/');
        if (separator > 0) {
            return id.substring(separator + 1);
        } else {
            return id;
        }
    }

    private String extractCollection(final String id) {
        if (id == null) {
            return null;
        }
        int separator = id.indexOf('/');
        if (separator > 0) {
            return id.substring(0, separator);
        } else {
            return null;
        }
    }

    private String extractLabel(final String collection, final String label) {
        if (collection != null) {
            if (label != null && !label.equals(collection)) {
                throw new IllegalArgumentException("Mismatching label: [" + label + "] and collection: [" + collection + "]");
            }
            return collection;
        } else {
            return label;
        }
    }

    private ElementId createId(Graph.Features.ElementFeatures features, String label, String defaultLabel, Object[] keyValues) {
        Optional<Object> optionalId = ElementHelper.getIdValue(keyValues);
        if (!optionalId.isPresent()) {
            return ArangoId.of(prefix, Optional.ofNullable(label).orElse(defaultLabel), null);
        }
        String id = optionalId
                .filter(features::willAllowId)
                .map(Object::toString)
                .orElseThrow(Vertex.Exceptions::userSuppliedIdsOfThisTypeNotSupported);
        String l = Optional.ofNullable(extractLabel(extractCollection(id), label)).orElse(defaultLabel);
        ElementHelper.validateLabel(l);
        return ArangoId.of(prefix, l, extractKey(id));
    }

    public ElementId createVertexId(Graph.Features.ElementFeatures features, String label, Object[] keyValues) {
        return createId(features, label, defaultVertexLabel, keyValues);
    }

    public ElementId createEdgeId(Graph.Features.ElementFeatures features, String label, Object[] keyValues) {
        return createId(features, label, defaultEdgeLabel, keyValues);
    }

    public ElementId parseVertexId(Object id) {
        if (id instanceof String) {
            return ArangoId.parseWithDefaultLabel(prefix, defaultVertexLabel, (String) id);
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
            return ArangoId.parseWithDefaultLabel(prefix, defaultEdgeLabel, (String) id);
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

    public ElementId parse(String fullName) {
        return ArangoId.parse(prefix, fullName);
    }

}
