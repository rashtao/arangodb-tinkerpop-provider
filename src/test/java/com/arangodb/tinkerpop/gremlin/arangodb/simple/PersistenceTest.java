package com.arangodb.tinkerpop.gremlin.arangodb.simple;

import com.arangodb.ArangoCollection;
import com.arangodb.tinkerpop.gremlin.PackageVersion;
import com.arangodb.tinkerpop.gremlin.TestGraphClient;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph.GRAPH_VARIABLES_COLLECTION;
import static org.assertj.core.api.Assertions.assertThat;

public class PersistenceTest extends AbstractGremlinTest {

    private TestGraphClient client() {
        return new TestGraphClient(graph.configuration());
    }

    private String graphName() {
        return ((ArangoDBGraph) graph).name();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void variables() {
        graph.variables().set("key", "value");
        ArangoCollection col = client().variablesCollection();
        Map<String, Object> doc = (Map<String, Object>) col.getDocument(graphName(), Map.class);
        assertThat(doc)
                .hasSize(5)
                .containsEntry("_id", GRAPH_VARIABLES_COLLECTION + "/" + graphName())
                .containsEntry("_key", graphName())
                .containsKey("_rev")
                .containsEntry("version", PackageVersion.VERSION)
                .containsKey("properties");
        assertThat((Map<String, Object>) doc.get("properties"))
                .hasSize(1)
                .containsEntry("key", "value");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void vertices() {
        Vertex v = graph.addVertex(
                T.id, "foo",
                T.label, "bar"
        );
        v
                .property("key", "value")
                .property("meta", "metaValue");
        String colName = graphName() + "_" + Vertex.DEFAULT_LABEL;
        ArangoCollection col = client().database().collection(colName);
        Map<String, Object> doc = (Map<String, Object>) col.getDocument((String) v.id(), Map.class);
        assertThat(doc)
                .hasSize(5)
                .containsEntry("_key", "foo")
                .containsEntry("_id", colName + "/foo")
                .containsKey("_rev")
                .containsEntry("label", "bar")
                .containsKey("properties");

        Map<String, Object> vertexProperties = (Map<String, Object>) doc.get("properties");
        assertThat(vertexProperties)
                .hasSize(1)
                .containsKey("key");

        List<Object> vertexProperty = (List<Object>) vertexProperties.get("key");
        assertThat(vertexProperty).hasSize(1);
        Map<String, Object> vertexPropertyValue = (Map<String, Object>) vertexProperty.get(0);
        assertThat(vertexPropertyValue)
                .hasSize(3)
                .containsKey("id")
                .containsEntry("value", "value")
                .containsKey("properties");

        Map<String, Object> metaProperties = (Map<String, Object>) vertexPropertyValue.get("properties");
        assertThat(metaProperties)
                .hasSize(1)
                .containsEntry("meta", "metaValue");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void edges() {
        Vertex a = graph.addVertex(T.id, "a");
        Vertex b = graph.addVertex(T.id, "b");
        Edge e = a.addEdge("foo", b, T.id, "e", "key", "value");

        String vertexColName = graphName() + "_" + Vertex.DEFAULT_LABEL;
        String edgeColName = graphName() + "_" + Edge.DEFAULT_LABEL;
        ArangoCollection col = client().database().collection(edgeColName);
        Map<String, Object> doc = (Map<String, Object>) col.getDocument((String) e.id(), Map.class);
        System.out.println(doc);
        assertThat(doc)
                .hasSize(7)
                .containsEntry("_key", "e")
                .containsEntry("_id", edgeColName + "/e")
                .containsKey("_rev")
                .containsEntry("_from", vertexColName + "/a")
                .containsEntry("_to", vertexColName + "/b")
                .containsEntry("label", "foo")
                .containsKey("properties");

        Map<String, Object> props = (Map<String, Object>) doc.get("properties");
        assertThat(props)
                .hasSize(1)
                .containsEntry("key", "value");
    }
}
