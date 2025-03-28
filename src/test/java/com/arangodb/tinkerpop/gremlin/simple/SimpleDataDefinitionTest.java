package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.entity.GraphEntity;
import com.arangodb.tinkerpop.gremlin.DataDefinitionTest;
import org.apache.commons.configuration2.Configuration;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class SimpleDataDefinitionTest extends DataDefinitionTest {

    @Override
    protected boolean isSimple() {
        return true;
    }

    @Test
    public void simpleEmptyGraph() {
        Configuration conf = confBuilder().build();
        checkDefaultSimpleGraph(conf);
    }

    @Test
    public void simpleGraph() {
        Configuration conf = confBuilder()
                .withVertexCollection("vertex")
                .withEdgeCollection("edge")
                .configureEdge("edge", "vertex", "vertex")
                .build();
        checkDefaultSimpleGraph(conf);
    }

    @Test
    public void simpleGraphWithPrefixedCollections() {
        Configuration conf = confBuilder()
                .graph("foo")
                .withVertexCollection("foo_vertex")
                .withEdgeCollection("foo_edge")
                .configureEdge("foo_edge", "foo_vertex", "foo_vertex")
                .build();
        Throwable thrown = catchThrowable(() -> checkDefaultSimpleGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot contain '_'");
    }

    @Test
    public void simpleGraphWithNonDefaultCollections() {
        Configuration conf = confBuilder()
                .graph("foo")
                .withVertexCollection("vertexes")
                .withEdgeCollection("edges")
                .configureEdge("edges", "vertexes", "vertexes")
                .build();
        GraphEntity graphInfo = createGraph(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo("foo_edges");
                    assertThat(ed.getFrom())
                            .hasSize(1)
                            .contains("foo_vertexes");
                    assertThat(ed.getTo())
                            .hasSize(1)
                            .contains("foo_vertexes");
                });
    }

    @Test
    public void simpleGraphWithoutEdgeDefinition() {
        Configuration conf = confBuilder()
                .graph("foo")
                .withVertexCollection("vertex")
                .withEdgeCollection("edge")
                .build();
        Throwable thrown = catchThrowable(() -> checkDefaultSimpleGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing definition for edge");
    }

    @Test
    public void simpleGraphWithWrongEdgeDefinition() {
        Configuration conf = confBuilder()
                .graph("foo")
                .withVertexCollection("vertex")
                .withEdgeCollection("edge")
                .configureEdge("edges", "vertexes", "vertexes")
                .build();
        Throwable thrown = catchThrowable(() -> checkDefaultSimpleGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing definition for edge");
    }

    @Test
    public void existingSimpleGraph() {
        String name = "existingSimpleGraph";
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", "v", "v")
                .build();
        createGraph(conf);
        GraphEntity graphInfo = createGraph(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo(name + "_e");
                    assertThat(ed.getFrom())
                            .hasSize(1)
                            .contains(name + "_v");
                    assertThat(ed.getTo())
                            .hasSize(1)
                            .contains(name + "_v");
                });
    }

    @Test
    public void existingSimpleGraphWithMoreOrphanCollections() {
        String name = "existingSimpleGraph";
        createGraph(confBuilder()
                .simpleGraph(false)
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", "v", "v")
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", "v", "v")
                .build();
        Throwable thrown = catchThrowable(() -> createGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Orphan collections do not match");
    }

    @Test
    public void existingSimpleGraphWithLessOrphanCollections() {
        String name = "existingSimpleGraph";
        createGraph(confBuilder()
                .simpleGraph(false)
                .graph(name)
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", "v", "v")
                .build();
        Throwable thrown = catchThrowable(() -> createGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Edge definitions do not match");
    }

    @Test
    public void existingSimpleGraphWithMoreEdgeDefinitions() {
        String name = "existingSimpleGraph";
        createGraph(confBuilder()
                .simpleGraph(false)
                .graph(name)
                .withVertexCollection("a")
                .withEdgeCollection("x")
                .withEdgeCollection("y")
                .configureEdge("x", "a", "a")
                .configureEdge("y", "a", "a")
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withEdgeCollection("x")
                .configureEdge("x", "a", "a")
                .build();
        Throwable thrown = catchThrowable(() -> createGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Edge definitions do not match");
    }

    @Test
    public void existingSimpleGraphWithLessEdgeDefinitions() {
        String name = "existingSimpleGraph";
        createGraph(confBuilder()
                .simpleGraph(false)
                .graph(name)
                .withVertexCollection("a")
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withEdgeCollection("x")
                .configureEdge("x", "a", "a")
                .build();
        Throwable thrown = catchThrowable(() -> createGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Orphan collections do not match");
    }

    private void checkDefaultSimpleGraph(Configuration conf) {
        String name = getName(conf);
        GraphEntity graphInfo = createGraph(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo(name + "_edge");
                    assertThat(ed.getFrom())
                            .hasSize(1)
                            .contains(name + "_vertex");
                    assertThat(ed.getTo())
                            .hasSize(1)
                            .contains(name + "_vertex");
                });
    }

}
