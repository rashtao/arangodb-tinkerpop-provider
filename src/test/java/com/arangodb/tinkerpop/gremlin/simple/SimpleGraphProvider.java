package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.tinkerpop.gremlin.TestGraphProvider;
import com.arangodb.tinkerpop.gremlin.complex.ComplexTestGraph;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.Map;

public class SimpleGraphProvider extends TestGraphProvider {

    @Override
    public Configuration newGraphConfiguration(final String graphName, final Class<?> test,
                                               final String testMethodName,
                                               final Map<String, Object> configurationOverrides,
                                               final LoadGraphWith.GraphData loadGraphWith) {
        Configuration conf = super.newGraphConfiguration(graphName, test, testMethodName, configurationOverrides, loadGraphWith);
        // FIXME: add config method
        conf.setProperty(Graph.GRAPH, SimpleTestGraph.class.getName());
        return conf;
    }

    @Override
    protected void configure(ArangoDBConfigurationBuilder builder, Class<?> test, String testMethodName, LoadGraphWith.GraphData loadGraphWith) {
        builder.simpleGraph(true);
    }

}
