package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.entity.GraphEntity;
import com.arangodb.tinkerpop.gremlin.DataDefinitionTest;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig.EdgeDef;
import org.apache.commons.configuration2.Configuration;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ComplexDataDefinitionTest extends DataDefinitionTest {

    @Override
    protected ArangoDBGraphConfig.GraphType graphType() {
        return ArangoDBGraphConfig.GraphType.COMPLEX;
    }

    @Test
    public void complexEmptyGraph() {
        Configuration conf = confBuilder().build();
        GraphEntity graphInfo = graphInfo(conf);
        assertThat(graphInfo).isNotNull();
        assertThat(graphInfo.getOrphanCollections()).isEmpty();
        assertThat(graphInfo.getEdgeDefinitions()).isEmpty();
    }

    @Test
    public void complexGraphWithoutEdges() {
        Configuration conf = confBuilder()
                .graph("foo")
                .orphanCollections("v")
                .build();
        GraphEntity graphInfo = graphInfo(conf);
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
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
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
                .orphanCollections("a")
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
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
                .edgeDefinitions(EdgeDef.of("e").from("v", "a").to("a", "v"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
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
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .edgeDefinitions(EdgeDef.of("e").from("a").to("a"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
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
        graphInfo(confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("e").from("a").to("a"))
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .edgeDefinitions(EdgeDef.of("e").from("a").to("a"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
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
                .edgeDefinitions(EdgeDef.of("edge").from("vertex").to("vertex"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
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
                .edgeDefinitions(EdgeDef.of("edge").from("foo_ver_tex").to("foo_vertex"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
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
                .edgeDefinitions(EdgeDef.of("foo_ed_ge").from("vertex").to("vertex"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot contain '_'");
    }

    @Test
    public void existingComplexGraph() {
        String name = "existingComplexGraph";
        Configuration conf = confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build();
        graphInfo(conf);
        GraphEntity graphInfo = graphInfo(conf);
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
        graphInfo(confBuilder()
                .graph(name)
                .orphanCollections("a")
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
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
        graphInfo(confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .orphanCollections("a")
                .edgeDefinitions(EdgeDef.of("e").from("v").to("v"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
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
        graphInfo(confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("b"))
                .edgeDefinitions(EdgeDef.of("y").from("b").to("a"))
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("b"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
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
        graphInfo(confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("b"))
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("b"))
                .edgeDefinitions(EdgeDef.of("y").from("b").to("a"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
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
        graphInfo(confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("b"))
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("x").from("b").to("a"))
                .build();
        Throwable thrown = catchThrowable(() -> graphInfo(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Edge definitions do not match");
    }

}
