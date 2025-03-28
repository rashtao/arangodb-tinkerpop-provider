package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.entity.GraphEntity;
import com.arangodb.tinkerpop.gremlin.DataDefinitionTest;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.util.CollectionUtil;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ComplexDataDefinitionTest extends DataDefinitionTest {

    @Override
    protected boolean isSimple() {
        return false;
    }

    @Test
    public void complexEmptyGraph() {
        Configuration conf = confBuilder().build();
        GraphEntity graphInfo = createGraph(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions()).isEmpty();
    }

    @Test
    public void complexGraphWithoutEdges() {
        Configuration conf = confBuilder()
                .graph("foo")
                .withVertexCollection("v")
                .build();
        GraphEntity graphInfo = createGraph(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections())
                .hasSize(1)
                .contains("foo_v");
        assertThat(graphInfo.getEdgeDefinitions()).isEmpty();
    }

    @Test
    public void complexGraph() {
        String name = "complexGraph";
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", "v", "v")
                .build();
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
    public void complexGraphWithOrphanCollections() {
        String name = "complexGraph";
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", "v", "v")
                .build();
        GraphEntity graphInfo = createGraph(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections())
                .hasSize(1)
                .contains(name + "_a");
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
    public void complexGraphWithManyEdgesCollections() {
        String name = "complexGraph";
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", CollectionUtil.asSet("v", "a"), CollectionUtil.asSet("a", "v"))
                .build();
        GraphEntity graphInfo = createGraph(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo(name + "_e");
                    assertThat(ed.getFrom())
                            .hasSize(2)
                            .contains(name + "_a")
                            .contains(name + "_v");
                    assertThat(ed.getTo())
                            .hasSize(2)
                            .contains(name + "_a")
                            .contains(name + "_v");
                });
    }

    @Test
    public void complexGraphWithManyEdgesCollectionsWithSameName() {
        String name = "complexGraph";
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", "v", "v")
                .configureEdge("e", "a", "a")
                .build();
        GraphEntity graphInfo = createGraph(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo(name + "_e");
                    assertThat(ed.getFrom())
                            .hasSize(2)
                            .contains(name + "_a")
                            .contains(name + "_v");
                    assertThat(ed.getTo())
                            .hasSize(2)
                            .contains(name + "_a")
                            .contains(name + "_v");
                });
    }

    @Test
    public void existingComplexGraphWithManyEdgesCollectionsWithSameNameInDifferentOrder() {
        String name = "complexGraph";
        createGraph(confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", "a", "a")
                .configureEdge("e", "v", "v")
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", "v", "v")
                .configureEdge("e", "a", "a")
                .build();
        GraphEntity graphInfo = createGraph(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions())
                .hasSize(1)
                .allSatisfy(ed -> {
                    assertThat(ed.getCollection()).isEqualTo(name + "_e");
                    assertThat(ed.getFrom())
                            .hasSize(2)
                            .contains(name + "_a")
                            .contains(name + "_v");
                    assertThat(ed.getTo())
                            .hasSize(2)
                            .contains(name + "_a")
                            .contains(name + "_v");
                });
    }

    @Test
    public void complexGraphWithInvalidName() {
        Configuration conf = confBuilder()
                .graph("foo_bar")
                .withVertexCollection("vertex")
                .withEdgeCollection("edge")
                .configureEdge("edge", "vertex", "vertex")
                .build();
        Throwable thrown = catchThrowable(() -> createGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot contain '_'");
    }

    @Test
    public void complexGraphWithInvalidVertexName() {
        Configuration conf = confBuilder()
                .graph("foo")
                .withVertexCollection("foo_vertex")
                .withEdgeCollection("edge")
                .configureEdge("edge", "foo_vertex", "foo_vertex")
                .build();
        Throwable thrown = catchThrowable(() -> createGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot contain '_'");
    }

    @Test
    public void complexGraphWithInvalidEdgeName() {
        Configuration conf = confBuilder()
                .graph("foo")
                .withVertexCollection("vertex")
                .withEdgeCollection("foo_edge")
                .configureEdge("foo_edge", "vertex", "vertex")
                .build();
        Throwable thrown = catchThrowable(() -> createGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot contain '_'");
    }

    @Test
    public void complexGraphWithoutEdgeDefinition() {
        Configuration conf = confBuilder()
                .graph("foo")
                .withVertexCollection("vertex")
                .withEdgeCollection("edge")
                .build();
        Throwable thrown = catchThrowable(() -> createGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing definition for edge");
    }

    @Test
    public void complexGraphWithWrongEdgeDefinition() {
        Configuration conf = confBuilder()
                .graph("foo")
                .withVertexCollection("vertex")
                .withEdgeCollection("edge")
                .configureEdge("edges", "vertexes", "vertexes")
                .build();
        Throwable thrown = catchThrowable(() -> createGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing definition for edge");
    }

    @Test
    public void existingComplexGraph() {
        String name = "existingComplexGraph";
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
    public void existingComplexGraphWithMoreOrphanCollections() {
        String name = "existingComplexGraph";
        createGraph(confBuilder()
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
    public void existingComplexGraphWithLessOrphanCollections() {
        String name = "existingComplexGraph";
        createGraph(confBuilder()
                .graph(name)
                .withVertexCollection("v")
                .withEdgeCollection("e")
                .configureEdge("e", "v", "v")
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("a")
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
    public void existingComplexGraphWithMoreEdgeDefinitions() {
        String name = "existingComplexGraph";
        createGraph(confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("b")
                .withEdgeCollection("x")
                .withEdgeCollection("y")
                .configureEdge("x", "a", "b")
                .configureEdge("y", "b", "a")
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("b")
                .withEdgeCollection("x")
                .configureEdge("x", "a", "b")
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
    public void existingComplexGraphWithLessEdgeDefinitions() {
        String name = "existingComplexGraph";
        createGraph(confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("b")
                .withEdgeCollection("x")
                .configureEdge("x", "a", "b")
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("b")
                .withEdgeCollection("x")
                .withEdgeCollection("y")
                .configureEdge("x", "a", "b")
                .configureEdge("y", "b", "a")
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
    public void existingComplexGraphWithMismatchingEdgeDefinitions() {
        String name = "existingComplexGraph";
        createGraph(confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("b")
                .withEdgeCollection("x")
                .configureEdge("x", "a", "b")
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .withVertexCollection("a")
                .withVertexCollection("b")
                .withEdgeCollection("x")
                .configureEdge("x", "b", "a")
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

}
