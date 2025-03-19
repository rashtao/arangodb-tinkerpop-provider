package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

@Graph.OptIn(Graph.OptIn.SUITE_STRUCTURE_STANDARD)
@Graph.OptIn(Graph.OptIn.SUITE_PROCESS_STANDARD)
@Graph.OptIn("com.arangodb.tinkerpop.gremlin.simple.SimpleCustomStandardSuite")
@Graph.OptOut(
        test = "org.apache.tinkerpop.gremlin.process.traversal.step.map.MergeEdgeTest$Traversals",
        method = "*",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.simple.custom.process.traversal.step.map.MergeEdgeTest")
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
        method = "*",
        reason = "replaced by com.arangodb.tinkerpop.gremlin.simple.custom.process.traversal.step.OrderabilityTest")
public class SimpleTestGraph extends ArangoDBGraph {

    @SuppressWarnings("unused")
    public static SimpleTestGraph open(Configuration configuration) {
        return new SimpleTestGraph(configuration);
    }

    public SimpleTestGraph(Configuration configuration) {
        super(configuration);
    }

}
