package com.arangodb.tinkerpop.gremlin;

import com.arangodb.tinkerpop.gremlin.structure.*;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.tinkerpop.gremlin.AbstractGraphProvider;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TestGraphProvider extends AbstractGraphProvider {

    private final String dbName = getClass().getSimpleName();

    protected abstract void configure(ArangoDBConfigurationBuilder builder,
                                      Class<?> test,
                                      String testMethodName,
                                      LoadGraphWith.GraphData loadGraphWith);

    @Override
    public Configuration newGraphConfiguration(final String graphName, final Class<?> test,
                                               final String testMethodName,
                                               final Map<String, Object> configurationOverrides,
                                               final LoadGraphWith.GraphData loadGraphWith) {
        System.out.println(test.getName() + "#" + testMethodName);
        ArangoDBConfigurationBuilder builder = new ArangoDBConfigurationBuilder()
                .arangoHosts("127.0.0.1:8529")
                .arangoUser("root")
                .arangoPassword("test")
                .dataBase(dbName)
                .graph(graphName);
        configure(builder, test, testMethodName, loadGraphWith);
        return builder.build();
    }

    @Override
    public void clear(Graph graph, Configuration configuration) throws Exception {
        Configuration arangoConfig = configuration.subset(ArangoDBGraph.PROPERTY_KEY_PREFIX);
        Properties arangoProperties = ConfigurationConverter.getProperties(arangoConfig);
        TestGraphClient client = new TestGraphClient(arangoProperties, dbName);
        client.clear(arangoConfig.getString(ArangoDBGraph.PROPERTY_KEY_GRAPH_NAME));
        if (graph != null) {
            graph.close();
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Set<Class> getImplementations() {
        return Stream.of(
                ArangoDBEdge.class,
                ArangoDBElement.class,
                ArangoDBGraph.class,
                ArangoDBGraphVariables.class,
                ArangoDBPersistentElement.class,
                ArangoDBProperty.class,
                ArangoDBSimpleElement.class,
                ArangoDBVertex.class,
                ArangoDBVertexProperty.class
        ).collect(Collectors.toSet());
    }

    @Override
    public Map<String, Object> getBaseConfiguration(String graphName, Class<?> test, String testMethodName,
                                                    LoadGraphWith.GraphData loadGraphWith) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object convertId(Object id, Class<? extends Element> c) {
        return id.toString();
    }

}
