package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

@Graph.OptIn(Graph.OptIn.SUITE_STRUCTURE_STANDARD)
@Graph.OptIn(Graph.OptIn.SUITE_PROCESS_STANDARD)
public class SimpleTestGraph extends ArangoDBGraph {

    @SuppressWarnings("unused")
    public static SimpleTestGraph open(Configuration configuration) {
        return new SimpleTestGraph(configuration);
    }

    public SimpleTestGraph(Configuration configuration) {
        super(configuration);
    }

}
