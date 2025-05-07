package com.arangodb.tinkerpop.gremlin.arangodb.simple;

import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DataTypesTest extends AbstractGremlinTest {

    private final List<?> data = Arrays.asList(
            true,
            11,
            5_000_000_000L,
            12.12d,
            "hello",
            null
    );

    private final List<?> unsupportedData = Arrays.asList(
            Collections.singletonMap("hello", "world"),
            Collections.singletonList("hello"),
            Collections.singleton("hello"),
            new Date(),
            new java.sql.Date(new Date().getTime()),
            new Date[]{new Date()},
            UUID.randomUUID(),
            BigInteger.TEN,
            BigDecimal.ONE,
            Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            1.23f,
            (byte) 0x22,
            (short) 11,
            Void.class
    );

    @Test
    public void variables() {
        data.forEach(this::testVariables);
        unsupportedData.forEach(this::testUnsupportedVariables);
    }

    private void testVariables(Object value) {
        if (value == null) return;
        graph.variables().set("value", value);
        Optional<Object> got = graph.variables().get("value");
        assertThat(got).isPresent().get().isEqualTo(value);
    }

    private void testUnsupportedVariables(Object value) {
        Throwable thrown = catchThrowable(() -> graph.variables().set("value", value));
        assertThat(thrown)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Graph variable value [%s] is of type %s is not supported", value, value.getClass());
    }

    @Test
    public void vertexProperties() {
        data.forEach(this::testVertexProperties);
        unsupportedData.forEach(this::testUnsupportedVertexProperties);
        unsupportedData.forEach(this::testUnsupportedMetaProperties);
    }

    private void testVertexProperties(Object value) {
        Vertex v = graph.addVertex();
        v
                .property("value", value)  // set vertex property value
                .property("meta", value);  // set meta property value
        VertexProperty<Object> p = graph.vertices(v.id()).next().property("value");
        assertThat(p.value()).isEqualTo(value);
        Property<Object> meta = p.property("meta");
        assertThat(meta.isPresent()).isTrue();
        assertThat(meta.value()).isEqualTo(value);
    }

    private void testUnsupportedVertexProperties(Object value) {
        Throwable thrown = catchThrowable(() -> graph.addVertex("value", value));
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Property value [%s] is of type %s is not supported", value, value.getClass());
    }

    private void testUnsupportedMetaProperties(Object value) {
        VertexProperty<String> p = graph.addVertex().property("value", "ok");
        Throwable thrown = catchThrowable(() -> p.property("value", value));
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Property value [%s] is of type %s is not supported", value, value.getClass());
    }

    @Test
    public void edgeProperties() {
        data.forEach(this::testEdgeProperties);
        unsupportedData.forEach(this::testUnsupportedEdgeProperties);
    }

    private void testEdgeProperties(Object value) {
        Vertex a = graph.addVertex();
        Vertex b = graph.addVertex();
        Edge e = a.addEdge("edge", b, "value", value);
        Property<Object> p = graph.edges(e.id()).next().properties("value").next();
        assertThat(p.isPresent()).isTrue();
        assertThat(p.value()).isEqualTo(value);
    }

    private void testUnsupportedEdgeProperties(Object value) {
        Vertex a = graph.addVertex();
        Vertex b = graph.addVertex();
        Throwable thrown = catchThrowable(() -> a.addEdge("edge", b, "value", value));
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Property value [%s] is of type %s is not supported", value, value.getClass());
    }
}
