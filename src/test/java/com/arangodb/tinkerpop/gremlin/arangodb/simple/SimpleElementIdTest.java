package com.arangodb.tinkerpop.gremlin.arangodb.simple;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assume.assumeTrue;

@SuppressWarnings("resource")
public class SimpleElementIdTest extends AbstractGremlinTest {

    protected ArangoDBGraph graph() {
        return (ArangoDBGraph) graph;
    }

    @Test
    public void id() {
        assumeTrue(graph().isSimpleGraph());

        assertThat(graph.addVertex(T.id, "a").id()).isEqualTo("a");
        assertThat(graph.addVertex(T.id, "b", T.label, "bar").id()).isEqualTo("b");
        assertThat(graph.addVertex().id())
                .isInstanceOf(String.class)
                .asString()
                .doesNotContain("/");

        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo/bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo/bar")
                .hasMessageContaining("invalid character '/'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo_bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo_bar")
                .hasMessageContaining("invalid character '_'");
    }

    @Test
    public void label() {
        assumeTrue(graph().isSimpleGraph());

        assertThat(graph.addVertex(T.label, "foo").label()).isEqualTo("foo");
        assertThat(graph.addVertex(T.id, "a", T.label, "bar").label()).isEqualTo("bar");
        assertThat(graph.addVertex(T.id, "b").label()).isEqualTo(Vertex.DEFAULT_LABEL);
        assertThat(graph.addVertex().label()).isEqualTo(Vertex.DEFAULT_LABEL);
    }
}
