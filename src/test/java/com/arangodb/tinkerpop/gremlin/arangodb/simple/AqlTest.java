package com.arangodb.tinkerpop.gremlin.arangodb.simple;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.*;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.as;
import static org.assertj.core.api.Assertions.assertThat;

public class AqlTest extends AbstractGremlinTest {

    private ArangoDBGraph graph() {
        return (ArangoDBGraph) this.graph;
    }

    private String vertexCollection() {
        return graph().vertexCollections().iterator().next();
    }

    private String edgeCollection() {
        return graph().edgeCollections().iterator().next();
    }

    @Test
    @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
    public void shouldExecuteAqlWithArgs() {
        this.graph.addVertex("name", "marko");
        String query = "FOR d IN @@vCol FILTER d.properties.name.value == @name RETURN d";
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("@vCol", vertexCollection());
        bindings.put("name", "marko");
        Iterator<String> result = this.graph().aql(query, bindings).values("name");
        assertThat(result)
                .isNotNull()
                .hasNext()
                .toIterable().first()
                .isEqualTo("marko");
    }

    @Test
    @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
    public void shouldExecuteAqlAndBackToGremlin() {
        this.graph.addVertex("name", "marko", "age", 29, "color", "red");
        this.graph.addVertex("name", "marko", "age", 30, "color", "yellow");
        String query = "FOR d IN @@vCol FILTER d.properties.name.value == @name RETURN d";
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("@vCol", vertexCollection());
        bindings.put("name", "marko");
        Traversal<?, ?> result = this.graph().aql(query, bindings).has("age", 29).values("color");
        assertThat(result)
                .isNotNull()
                .hasNext()
                .toIterable().first()
                .isEqualTo("red");
    }

    @Test
    @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
    public void shouldExecuteMultiIdWhereAql() {
        Vertex v1 = this.graph.addVertex("name", "marko", "age", 29, "color", "red");
        Vertex v2 = this.graph.addVertex("name", "marko", "age", 30, "color", "yellow");
        this.graph.addVertex("name", "marko", "age", 30, "color", "orange");
        String query = "FOR d IN @@vCol FILTER d._key IN @ids RETURN d";
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("@vCol", vertexCollection());
        bindings.put("ids", Arrays.asList(v1.id(), v2.id()));
        List<Object> result = this.graph().aql(query, bindings).id().toList();
        assertThat(result)
                .hasSize(2)
                .contains(v1.id(), v2.id());
    }

    @Test
    @LoadGraphWith(LoadGraphWith.GraphData.GRATEFUL)
    @SuppressWarnings({"ConcatenationWithEmptyString", "resource"})
    public void compareAqlAndGremlin() {
        Set<?> sungAndWrittenBySame = g.V().match(
                        as("a").in("sungBy").as("b"),
                        as("a").in("writtenBy").as("b"))
                .select("a", "b").by("name")
                .toSet();

        Set<?> followEachOther = g.V().match(
                        as("a").out("followedBy").as("b"),
                        as("b").out("followedBy").as("a"))
                .select("a", "b").by("name")
                .toSet();

        Set<?> sameFollowersAndFollowingsCountGreaterThan10 = g.V()
                .match(
                        as("a").out("followedBy").count().is(P.gt(10)).as("b"),
                        as("a").in("followedBy").count().is(P.gt(10)).as("b")
                )
                .select("a").by("name")
                .toSet();

        List<Pair<Set<?>, Set<?>>> data = Arrays.asList(
                Pair.of(
                        sungAndWrittenBySame,
                        graph().aql("" +
                                        "FOR start IN standard_vertex" +
                                        "    FOR b, e1 IN 1..1 INBOUND start GRAPH standard" +
                                        "        FILTER e1.label == 'sungBy'" +
                                        "        FOR a, e2 IN 1..1 OUTBOUND b GRAPH standard" +
                                        "            FILTER e2.label == 'writtenBy'" +
                                        "            FILTER a == start" +
                                        "            RETURN {a, b}")
                                .select("a", "b").by("name")
                                .toSet()
                ),
                Pair.of(
                        sungAndWrittenBySame,
                        graph().aql("" +
                                        "FOR e1 IN standard_edge" +
                                        "    FILTER e1.label == \"sungBy\"" +
                                        "    FOR e2 IN standard_edge" +
                                        "        FILTER e2.label == \"writtenBy\"" +
                                        "        FILTER e1._from == e2._from" +
                                        "        FILTER e1._to == e2._to" +
                                        "        RETURN {a: DOCUMENT(e1._to), b: DOCUMENT(e1._from)}")
                                .select("a", "b").by("name")
                                .toSet()
                ),
                Pair.of(
                        followEachOther,
                        graph().aql("" +
                                        "FOR start IN standard_vertex" +
                                        "    FOR a, e, p IN 2..2 OUTBOUND start GRAPH standard" +
                                        "        FILTER p.edges[0].label == 'followedBy'" +
                                        "        FILTER p.edges[1].label == 'followedBy'" +
                                        "        FILTER a == start" +
                                        "        RETURN {a: p.vertices[0], b: p.vertices[1]}")
                                .select("a", "b").by("name")
                                .toSet()
                ),
                Pair.of(
                        followEachOther,
                        graph().aql("" +
                                        "FOR e1 IN standard_edge" +
                                        "    FILTER e1.label == 'followedBy'" +
                                        "    FOR e2 IN standard_edge" +
                                        "        FILTER e2.label == 'followedBy'" +
                                        "        FILTER e1._to == e2._from" +
                                        "        FILTER e1._from == e2._to" +
                                        "        RETURN {a: DOCUMENT(e1._from), b: DOCUMENT(e1._to)}")
                                .select("a", "b").by("name")
                                .toSet()
                ),
                Pair.of(
                        sameFollowersAndFollowingsCountGreaterThan10,
                        graph().aql("" +
                                        "FOR start IN standard_vertex" +
                                        "  LET outCount = (" +
                                        "    FOR a, e IN 1..1 OUTBOUND start GRAPH standard" +
                                        "      FILTER e.label == 'followedBy'" +
                                        "      COLLECT WITH COUNT INTO c" +
                                        "      RETURN c" +
                                        "  )[0]" +
                                        "  FILTER outCount > 10" +
                                        "  LET inCount = (" +
                                        "    FOR a, e IN 1..1 INBOUND start GRAPH standard" +
                                        "      FILTER e.label == 'followedBy'" +
                                        "      COLLECT WITH COUNT INTO c" +
                                        "      RETURN c" +
                                        "  )[0]" +
                                        "  FILTER inCount > 10" +
                                        "  FILTER inCount == outCount" +
                                        "  RETURN {a: start}")
                                .select("a").by("name")
                                .toSet()
                ),
                Pair.of(
                        sameFollowersAndFollowingsCountGreaterThan10,
                        graph().aql("" +
                                        "FOR start IN standard_vertex" +
                                        "  LET outCount = (" +
                                        "    FOR e IN standard_edge" +
                                        "      FILTER e.label == 'followedBy'" +
                                        "      FILTER e._from == start._id" +
                                        "      COLLECT WITH COUNT INTO c" +
                                        "      RETURN c" +
                                        "  )[0]" +
                                        "  FILTER outCount > 10" +
                                        "  LET inCount = (" +
                                        "    FOR e IN standard_edge" +
                                        "      FILTER e.label == 'followedBy'" +
                                        "      FILTER e._to == start._id      " +
                                        "      COLLECT WITH COUNT INTO c" +
                                        "      RETURN c" +
                                        "  )[0]" +
                                        "  FILTER inCount > 10" +
                                        "  FILTER inCount == outCount" +
                                        "  RETURN {a: start}")
                                .select("a").by("name")
                                .toSet()
                )
        );

        for (Pair<Set<?>, Set<?>> p : data) {
            assertThat(p.getLeft()).isEqualTo(p.getRight());
        }
    }
}