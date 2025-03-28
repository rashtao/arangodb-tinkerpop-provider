package com.arangodb.tinkerpop.gremlin;

import com.arangodb.entity.GraphEntity;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.junit.After;
import org.junit.Before;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DataDefinitionTest {
    private TestGraphClient client;
    private final Set<ArangoDBGraph> graphs = ConcurrentHashMap.newKeySet();
    private final String dbName = getClass().getSimpleName();

    @Before
    public void initDB() {
        client = new TestGraphClient(ConfigurationConverter.getProperties(confBuilder().build().subset(ArangoDBGraph.PROPERTY_KEY_PREFIX)), dbName);
    }

    @After
    public void clear() {
        for (ArangoDBGraph graph : graphs) {
            graph.close();
            client.clear(graph.name());
        }
    }

    protected abstract boolean isSimple();

    protected ArangoDBConfigurationBuilder confBuilder() {
        return new ArangoDBConfigurationBuilder()
                .arangoHosts("127.0.0.1:8529")
                .arangoUser("root")
                .arangoPassword("test")
                .dataBase(dbName)
                .simpleGraph(isSimple());
    }

    protected GraphEntity createGraph(Configuration conf) {
        ArangoDBGraph g = (ArangoDBGraph) GraphFactory.open(conf);
        graphs.add(g);
        return g.getClient().getArangoGraph().getInfo();
    }

    protected String getName(Configuration conf) {
        return conf.subset(ArangoDBGraph.PROPERTY_KEY_PREFIX).getString(ArangoDBGraph.PROPERTY_KEY_GRAPH_NAME);
    }

}
