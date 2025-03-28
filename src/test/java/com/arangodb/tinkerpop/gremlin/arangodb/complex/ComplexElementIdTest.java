package com.arangodb.tinkerpop.gremlin.arangodb.complex;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assume.assumeTrue;

@SuppressWarnings("resource")
public class ComplexElementIdTest extends AbstractGremlinTest {

    protected ArangoDBGraph graph() {
        return (ArangoDBGraph) graph;
    }

    @Test
    public void id() {
        assumeTrue(!graph().isSimpleGraph());

        assertThat(graph.addVertex(T.id, "foo/a").id()).isEqualTo("foo/a");
        assertThat(graph.addVertex(T.id, "foo/b", T.label, "foo").id()).isEqualTo("foo/b");
        assertThat(graph.addVertex(T.id, "c", T.label, "foo").id()).isEqualTo("foo/c");
        assertThat(graph.addVertex(T.id, "d").id()).isEqualTo(Vertex.DEFAULT_LABEL + "/d");
        assertThat(graph.addVertex(T.label, "foo").id())
                .isInstanceOf(String.class)
                .asString()
                .startsWith("foo/");
        assertThat(graph.addVertex().id())
                .isInstanceOf(String.class)
                .asString()
                .startsWith(Vertex.DEFAULT_LABEL + "/");

        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo/bar", T.label, "baz")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mismatching label");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo/bar/baz")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bar/baz")
                .hasMessageContaining("invalid character '/'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo_bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo_bar")
                .hasMessageContaining("invalid character '_'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo/bar_baz")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bar_baz")
                .hasMessageContaining("invalid character '_'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "foo_bar/baz")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo_bar")
                .hasMessageContaining("invalid character '_'");
        assertThat(catchThrowable(() -> graph.addVertex(T.id, "vertex_foo/bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vertex_foo")
                .hasMessageContaining("invalid character '_'");
    }

    @Test
    public void label() {
        assumeTrue(!graph().isSimpleGraph());

        assertThat(graph.addVertex(T.id, "foo/a").label()).isEqualTo("foo");
        assertThat(graph.addVertex(T.id, "foo/b", T.label, "foo").label()).isEqualTo("foo");
        assertThat(graph.addVertex(T.id, "c", T.label, "foo").label()).isEqualTo("foo");
        assertThat(graph.addVertex(T.id, "d").label()).isEqualTo(Vertex.DEFAULT_LABEL);
        assertThat(graph.addVertex(T.label, "foo").label()).isEqualTo("foo");
        assertThat(graph.addVertex().label()).isEqualTo(Vertex.DEFAULT_LABEL);

        assertThat(catchThrowable(() -> graph.addVertex(T.label, "foo/bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo/bar")
                .hasMessageContaining("invalid character '/'");
        assertThat(catchThrowable(() -> graph.addVertex(T.label, "foo_bar")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foo_bar")
                .hasMessageContaining("invalid character '_'");
    }

}
