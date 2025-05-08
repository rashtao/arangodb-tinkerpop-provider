package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

@Graph.OptIn(Graph.OptIn.SUITE_STRUCTURE_STANDARD)
@Graph.OptIn(Graph.OptIn.SUITE_PROCESS_STANDARD)
@Graph.OptIn("com.arangodb.tinkerpop.gremlin.complex.custom.CustomStandardSuite")
@Graph.OptIn("com.arangodb.tinkerpop.gremlin.arangodb.complex.ComplexArangoDBSuite")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.util.detached.DetachedGraphTest",
        method = "testAttachableCreateMethod",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldAddVertexWithUserSuppliedStringId",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldRemoveVertices",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldRemoveEdges",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldEvaluateConnectivityPatterns",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.util.star.StarGraphTest",
        method = "shouldAttachWithCreateMethod",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.VertexTest$AddEdgeTest",
        method = "shouldAddEdgeWithUserSuppliedStringId",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.map.MergeEdgeTest$Traversals",
        method = "*",
        reason = "tested with simple graph only")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_properties_order",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_outE_order_byXascX",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_outE_order_byXdescX",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_outE_asXheadX_path_order_byXascX_selectXheadX",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_out_properties_asXheadX_path_order_byXdescX_selectXheadX_value",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_outE_asXheadX_path_order_byXdescX_selectXheadX",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.OrderabilityTest$Traversals",
        method = "g_V_out_out_properties_asXheadX_path_order_byXascX_selectXheadX_value",
        reason = "requires numeric ids support")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.SubgraphTest$Traversals",
        method = "g_V_withSideEffectXsgX_repeatXbothEXcreatedX_subgraphXsgX_outVX_timesX5X_name_dedup",
        reason = "requires VertexProperty user supplied identifiers")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.SubgraphTest$Traversals",
        method = "g_V_withSideEffectXsgX_outEXknowsX_subgraphXsgX_name_capXsgX",
        reason = "requires VertexProperty user supplied identifiers")
public class ComplexTestGraph extends ArangoDBGraph {

    @SuppressWarnings("unused")
    public static ComplexTestGraph open(Configuration configuration) {
        return new ComplexTestGraph(configuration);
    }

    public ComplexTestGraph(Configuration configuration) {
        super(configuration);
    }
}
