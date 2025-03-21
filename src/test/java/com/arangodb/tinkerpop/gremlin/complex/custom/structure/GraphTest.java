/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.arangodb.tinkerpop.gremlin.complex.custom.structure;

import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.ExceptionCoverage;
import org.apache.tinkerpop.gremlin.FeatureRequirement;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@ExceptionCoverage(exceptionClass = Graph.Exceptions.class, methods = {
        "vertexWithIdAlreadyExists",
        "elementNotFound",
        "idArgsMustBeEitherIdOrElement"
})
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class GraphTest extends AbstractGremlinTest {

    @Test
    @FeatureRequirement(featureClass = Graph.Features.VertexFeatures.class, feature = Graph.Features.VertexFeatures.FEATURE_ADD_VERTICES)
    @FeatureRequirement(featureClass = Graph.Features.VertexFeatures.class, feature = Graph.Features.VertexFeatures.FEATURE_USER_SUPPLIED_IDS)
    @FeatureRequirement(featureClass = Graph.Features.VertexFeatures.class, feature = Graph.Features.VertexFeatures.FEATURE_STRING_IDS)
    public void shouldAddVertexWithUserSuppliedStringId() {
        graph.addVertex(T.id, "1000");
        graph.addVertex();
        tryCommit(graph, graph -> {
            final Vertex v = graph.vertices("1000").next();
            assertEquals("vertex/1000", v.id());
        });
    }

    /**
     * Create a small {@link Graph} and ensure that counts of edges per vertex are correct.
     */
    @Test
    @FeatureRequirement(featureClass = Graph.Features.EdgeFeatures.class, feature = Graph.Features.EdgeFeatures.FEATURE_ADD_EDGES)
    @FeatureRequirement(featureClass = Graph.Features.VertexFeatures.class, feature = Graph.Features.VertexFeatures.FEATURE_ADD_VERTICES)
    public void shouldEvaluateConnectivityPatterns() {
        Vertex a;
        Vertex b;
        final Vertex c;
        final Vertex d;
        if (graph.features().vertex().supportsUserSuppliedIds()) {
            a = graph.addVertex(T.id, graphProvider.convertId("1", Vertex.class));
            b = graph.addVertex(T.id, graphProvider.convertId("2", Vertex.class));
            c = graph.addVertex(T.id, graphProvider.convertId("3", Vertex.class));
            d = graph.addVertex(T.id, graphProvider.convertId("4", Vertex.class));
        } else {
            a = graph.addVertex();
            b = graph.addVertex();
            c = graph.addVertex();
            d = graph.addVertex();
        }

        tryCommit(graph, getAssertVertexEdgeCounts(4, 0));

        final Edge e = a.addEdge(graphProvider.convertLabel("knows"), b);
        final Edge f = b.addEdge(graphProvider.convertLabel("knows"), c);
        final Edge g = c.addEdge(graphProvider.convertLabel("knows"), d);
        final Edge h = d.addEdge(graphProvider.convertLabel("knows"), a);

        tryCommit(graph, getAssertVertexEdgeCounts(4, 4));

        graph.vertices().forEachRemaining(v -> {
            assertEquals(1l, IteratorUtils.count(v.edges(Direction.OUT)));
            assertEquals(1l, IteratorUtils.count(v.edges(Direction.IN)));
        });

        graph.edges().forEachRemaining(x -> {
            assertEquals(graphProvider.convertLabel("knows"), x.label());
        });

        if (graph.features().vertex().supportsUserSuppliedIds()) {
            Vertex va = graph.vertices(graphProvider.convertId("1", Vertex.class)).next();
            Vertex vb = graph.vertices(graphProvider.convertId("2", Vertex.class)).next();
            Vertex vc = graph.vertices(graphProvider.convertId("3", Vertex.class)).next();
            Vertex vd = graph.vertices(graphProvider.convertId("4", Vertex.class)).next();

            assertEquals(a, va);
            assertEquals(b, vb);
            assertEquals(c, vc);
            assertEquals(d, vd);

            assertEquals(1l, IteratorUtils.count(va.edges(Direction.IN)));
            assertEquals(1l, IteratorUtils.count(va.edges(Direction.OUT)));
            assertEquals(1l, IteratorUtils.count(vb.edges(Direction.IN)));
            assertEquals(1l, IteratorUtils.count(vb.edges(Direction.OUT)));
            assertEquals(1l, IteratorUtils.count(vc.edges(Direction.IN)));
            assertEquals(1l, IteratorUtils.count(vc.edges(Direction.OUT)));
            assertEquals(1l, IteratorUtils.count(vd.edges(Direction.IN)));
            assertEquals(1l, IteratorUtils.count(vd.edges(Direction.OUT)));

            final Edge i = a.addEdge(graphProvider.convertLabel("hates"), b);

            a = graph.vertices(a.id()).next();
            b = graph.vertices(b.id()).next();

            va = graph.vertices(graphProvider.convertId("1", Vertex.class)).next();
            vb = graph.vertices(graphProvider.convertId("2", Vertex.class)).next();
            vc = graph.vertices(graphProvider.convertId("3", Vertex.class)).next();
            vd = graph.vertices(graphProvider.convertId("4", Vertex.class)).next();

            assertEquals(1l, IteratorUtils.count(va.edges(Direction.IN)));
            assertEquals(2l, IteratorUtils.count(va.edges(Direction.OUT)));
            assertEquals(2l, IteratorUtils.count(vb.edges(Direction.IN)));
            assertEquals(1l, IteratorUtils.count(vb.edges(Direction.OUT)));
            assertEquals(1l, IteratorUtils.count(vc.edges(Direction.IN)));
            assertEquals(1l, IteratorUtils.count(vc.edges(Direction.OUT)));
            assertEquals(1l, IteratorUtils.count(vd.edges(Direction.IN)));
            assertEquals(1l, IteratorUtils.count(vd.edges(Direction.OUT)));

            for (Edge x : IteratorUtils.list(a.edges(Direction.OUT))) {
                assertTrue(x.label().equals(graphProvider.convertLabel("knows")) || x.label().equals(graphProvider.convertLabel("hates")));
            }

            assertEquals(graphProvider.convertLabel("hates"), i.label());
            assertEquals(graphProvider.convertId("vertex/2", Vertex.class).toString(), i.inVertex().id().toString());
            assertEquals(graphProvider.convertId("vertex/1", Vertex.class).toString(), i.outVertex().id().toString());
        }

        final Set<Object> vertexIds = new HashSet<>();
        vertexIds.add(a.id());
        vertexIds.add(a.id());
        vertexIds.add(b.id());
        vertexIds.add(b.id());
        vertexIds.add(c.id());
        vertexIds.add(d.id());
        vertexIds.add(d.id());
        vertexIds.add(d.id());
        assertEquals(4, vertexIds.size());
    }

}
