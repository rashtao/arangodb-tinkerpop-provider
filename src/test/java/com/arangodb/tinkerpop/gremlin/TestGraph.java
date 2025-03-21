package com.arangodb.tinkerpop.gremlin;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

@Graph.OptIn(Graph.OptIn.SUITE_STRUCTURE_STANDARD)
@Graph.OptIn(Graph.OptIn.SUITE_PROCESS_STANDARD)
@Graph.OptIn("com.arangodb.tinkerpop.gremlin.complex.custom.CustomStandardSuite")
@Graph.OptIn("com.arangodb.tinkerpop.gremlin.simple.custom.CustomStandardSuite")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.util.detached.DetachedGraphTest",
        method = "testAttachableCreateMethod",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.custom.structure.util.detached.DetachedGraphTest#testAttachableCreateMethod()")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldAddVertexWithUserSuppliedStringId",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.custom.structure.GraphTest#shouldAddVertexWithUserSuppliedStringId()")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldRemoveVertices",
        reason = "FIXME: DE-998")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldRemoveEdges",
        reason = "FIXME: DE-998")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.GraphTest",
        method = "shouldEvaluateConnectivityPatterns",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.custom.structure.GraphTest#shouldEvaluateConnectivityPatterns()")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.util.star.StarGraphTest",
        method = "shouldAttachWithCreateMethod",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.custom.structure.util.star.StarGraphTest.shouldAttachWithCreateMethod")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.util.star.StarGraphTest",
        method = "shouldCopyFromGraphAToGraphB",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.custom.structure.util.star.StarGraphTest.shouldCopyFromGraphAToGraphB")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.structure.VertexTest$AddEdgeTest",
        method = "shouldAddEdgeWithUserSuppliedStringId",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.custom.structure.VertexTest.AddEdgeTest#shouldAddEdgeWithUserSuppliedStringId()")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.map.MergeEdgeTest$Traversals",
        method = "*",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.custom.process.traversal.step.map.MergeEdgeTest")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.map.MergeVertexTest$Traversals",
        method = "g_withSideEffectXc_label_person_name_markoX_withSideEffectXm_age_19X_mergeVXselectXcXX_optionXonMatch_selectXmXX_option",
        reason = "FIXME: DE-995")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.map.MergeVertexTest$Traversals",
        method = "g_mergeVXlabel_person_name_markoX_optionXonMatch_age_19X_option",
        reason = "FIXME: DE-995")
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
public class TestGraph extends ArangoDBGraph {

    @SuppressWarnings("unused")
    public static TestGraph open(Configuration configuration) {
        return new TestGraph(configuration);
    }

    public TestGraph(Configuration configuration) {
        super(configuration);
    }
}
