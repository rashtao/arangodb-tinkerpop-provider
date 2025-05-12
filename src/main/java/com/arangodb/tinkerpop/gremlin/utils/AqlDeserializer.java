package com.arangodb.tinkerpop.gremlin.utils;

import com.arangodb.tinkerpop.gremlin.persistence.EdgeData;
import com.arangodb.tinkerpop.gremlin.persistence.VertexData;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBEdge;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBVertex;
import com.arangodb.util.RawBytes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class AqlDeserializer {
    private final ArangoDBGraph graph;
    private final ObjectMapper mapper;

    public AqlDeserializer(ArangoDBGraph graph, ObjectMapper mapper) {
        this.graph = graph;
        this.mapper = mapper;
    }

    public Object deserialize(RawBytes raw) {
        try {
            return deserialize(mapper.readTree(raw.get()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Object deserialize(JsonNode node) throws IOException {
        if (isEdge(node)) {
            EdgeData data = mapper.readerFor(EdgeData.class).readValue(node);
            return new ArangoDBEdge(graph, data);
        } else if (isVertex(node)) {
            VertexData data = mapper.readerFor(VertexData.class).readValue(node);
            return new ArangoDBVertex(graph, data);
        } else if (node.isArray()) {
            ArrayList<Object> out = new ArrayList<>();
            for (JsonNode e : IteratorUtils.list(node.elements())) {
                out.add(deserialize(e));
            }
            return out;
        } else if (node.isObject()) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> f : IteratorUtils.list(node.fields())) {
                out.put(f.getKey(), deserialize(f.getValue()));
            }
            return out;
        } else {
            return mapper.readerFor(Object.class).readValue(node);
        }
    }

    private boolean isVertex(JsonNode node) {
        return node.has("_key")
                && node.has("_id")
                && node.has("_rev")
                && node.has("label");
    }

    private boolean isEdge(JsonNode node) {
        return node.has("_key")
                && node.has("_id")
                && node.has("_rev")
                && node.has("label")
                && node.has("_from")
                && node.has("_to");
    }
}
