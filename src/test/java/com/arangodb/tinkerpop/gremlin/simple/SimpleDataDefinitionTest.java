package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.entity.GraphEntity;
import com.arangodb.tinkerpop.gremlin.DataDefinitionTest;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig.EdgeDef;
import org.apache.commons.configuration2.Configuration;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class SimpleDataDefinitionTest extends DataDefinitionTest {

    @Override
    protected ArangoDBGraphConfig.GraphType graphType() {
        return ArangoDBGraphConfig.GraphType.SIMPLE;
    }

    @Test
    public void simpleEmptyGraph() {
        Configuration conf = confBuilder().build();
        checkDefaultSimpleGraph(conf);
    }

    @Test
    public void simpleGraph() {
        Configuration conf = confBuilder()
                .edgeDefinitions(EdgeDef.of("edge").from("vertex").to("vertex"))
                .build();
        checkDefaultSimpleGraph(conf);
    }

    @Test
    public void simpleGraphWithNonDefaultCollections() {
        Configuration conf = confBuilder()
                .graph("foo")
                .edgeDefinitions(EdgeDef.of("edges").from("vertexes").to("vertexes"))
                .build();
        GraphEntity graphInfo = graphInfo(conf);
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
    public void simpleGraphWithInvalidVertexName() {
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
    public void simpleGraphWithInvalidEdgeName() {
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
    public void existingSimpleGraph() {
        String name = "existingSimpleGraph";
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
    public void existingGraphWithMoreOrphanCollections() {
        String name = "existingSimpleGraph";
        graphInfo(confBuilder()
                .graphType(ArangoDBGraphConfig.GraphType.COMPLEX)
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
    public void existingGraphWithLessOrphanCollections() {
        String name = "existingSimpleGraph";
        graphInfo(confBuilder()
                .graphType(ArangoDBGraphConfig.GraphType.COMPLEX)
                .graph(name)
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
                .hasMessageContaining("Edge definitions do not match");
    }

    @Test
    public void existingSimpleGraphWithMoreEdgeDefinitions() {
        String name = "existingSimpleGraph";
        graphInfo(confBuilder()
                .graphType(ArangoDBGraphConfig.GraphType.COMPLEX)
                .graph(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("a"))
                .edgeDefinitions(EdgeDef.of("y").from("a").to("a"))
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("a"))
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
    public void existingSimpleGraphWithLessEdgeDefinitions() {
        String name = "existingSimpleGraph";
        graphInfo(confBuilder()
                .graphType(ArangoDBGraphConfig.GraphType.COMPLEX)
                .graph(name)
                .orphanCollections("a")
                .build());
        Configuration conf = confBuilder()
                .graph(name)
                .edgeDefinitions(EdgeDef.of("x").from("a").to("a"))
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

    private void checkDefaultSimpleGraph(Configuration conf) {
        String name = getName(conf);
        GraphEntity graphInfo = graphInfo(conf);
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
