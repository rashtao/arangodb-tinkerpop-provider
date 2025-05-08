package com.arangodb.tinkerpop.gremlin.persistence;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig.GraphType;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class ElementIdFactory {
    private final String prefix;
    private final GraphType graphType;

    public ElementIdFactory(ArangoDBGraphConfig config) {
        this.prefix = config.graphName;
        this.graphType = config.graphType;
    }

    private String extractKey(final String id) {
        String[] parts = id.split("/");
        return parts[parts.length - 1];
    }

    private String extractCollection(final String id) {
        String[] parts = id.replaceFirst("^" + prefix + "_", "").split("/");
        if (parts.length > 2) {
            throw new IllegalArgumentException(String.format("key (%s) contains invalid character '/'", id));
        }
        return parts.length == 2 ? parts[0] : null;
    }

    private String inferCollection(final String collection, final String label, final String defaultLabel) {
        if (graphType == GraphType.SIMPLE) {
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

    public ElementId createVertexId(String label, Object[] keyValues) {
        return createId(label, Vertex.DEFAULT_LABEL, keyValues);
    }

    public ElementId createEdgeId(String label, Object[] keyValues) {
        return createId(label, Edge.DEFAULT_LABEL, keyValues);
    }

    public ElementId parseVertexId(Object id) {
        if (id instanceof String) {
            return parseWithDefaultLabel((String) id, Vertex.DEFAULT_LABEL);
        } else if (id instanceof Element) {
            return parseVertexId(((Element) id).id());
        } else {
            throw Vertex.Exceptions.userSuppliedIdsOfThisTypeNotSupported();
        }
    }

    public List<ElementId> parseVertexIds(Object[] ids) {
        return Arrays.stream(ids)
                .map(this::parseVertexId)
                .collect(Collectors.toList());
    }

    public ElementId parseEdgeId(Object id) {
        if (id instanceof String) {
            return parseWithDefaultLabel((String) id, Edge.DEFAULT_LABEL);
        } else if (id instanceof Element) {
            return parseEdgeId(((Element) id).id());
        } else {
            throw Edge.Exceptions.userSuppliedIdsOfThisTypeNotSupported();
        }
    }

    public List<ElementId> parseEdgeIds(Object[] ids) {
        return Arrays.stream(ids)
                .map(this::parseEdgeId)
                .collect(Collectors.toList());
    }

    public ElementId parseId(String id) {
        String collection = extractCollection(id);
        String key = extractKey(id);
        return of(prefix, collection, key);
    }

    private ElementId parseWithDefaultLabel(String id, String defaultLabel) {
        String collection = inferCollection(extractCollection(id), null, defaultLabel);
        String key = extractKey(id);
        return of(prefix, collection, key);
    }

    private ElementId createId(String label, String defaultLabel, Object[] keyValues) {
        Optional<Object> optionalId = ElementHelper.getIdValue(keyValues);
        if (!optionalId.isPresent()) {
            return of(prefix, inferCollection(null, label, defaultLabel), null);
        }
        String id = optionalId
                .filter(String.class::isInstance)
                .map(Object::toString)
                .orElseThrow(Vertex.Exceptions::userSuppliedIdsOfThisTypeNotSupported);
        validateId(id);
        return of(prefix, inferCollection(extractCollection(id), label, defaultLabel), extractKey(id));
    }

    private void validateId(String id) {
        switch (graphType) {
            case SIMPLE:
                if (id.contains("_")) {
                    throw new IllegalArgumentException(String.format("id (%s) contains invalid character '_'", id));
                }
                if (id.contains("/")) {
                    throw new IllegalArgumentException(String.format("id (%s) contains invalid character '/'", id));
                }
                break;
            case COMPLEX:
                if (id.replaceFirst("^" + prefix + "_", "").contains("_")) {
                    throw new IllegalArgumentException(String.format("id (%s) contains invalid character '_'", id));
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private ElementId of(String prefix, String collection, String key) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(collection);
        ElementId.validateIdParts(prefix, collection, key);
        return graphType == GraphType.SIMPLE ?
                new SimpleId(prefix, collection, key) :
                new ArangoId(prefix, collection, key);
    }

}
