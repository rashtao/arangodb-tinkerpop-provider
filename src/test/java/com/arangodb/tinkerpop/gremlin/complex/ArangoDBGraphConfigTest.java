package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.config.HostDescription;
import com.arangodb.tinkerpop.gremlin.AbstractTest;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("resource")
public class ArangoDBGraphConfigTest extends AbstractTest {

    @Test
    public void loadConfigFromFile() {
        client.clear("g");
        ArangoDBGraph g = createGraph("src/test/resources/test.yaml");
        ArangoDBGraphConfig conf = new ArangoDBGraphConfig(g.configuration());
        assertThat(conf.dbName).isEqualTo(dbName);
        assertThat(conf.graphName).isEqualTo("g");
        assertThat(conf.prefix).isEqualTo("g_");
        assertThat(conf.graphType).isEqualTo(ArangoDBGraphConfig.GraphType.COMPLEX);
        assertThat(conf.orphanCollections).containsExactlyInAnyOrder("g_x", "g_y", "g_z");
        assertThat(conf.edgeDefinitions).hasSize(2)
                .anySatisfy(e -> {
                    assertThat(e.getCollection()).isEqualTo("g_e1");
                    assertThat(e.getFrom()).containsExactlyInAnyOrder("g_a");
                    assertThat(e.getTo()).containsExactlyInAnyOrder("g_b", "g_c");
                })
                .anySatisfy(e -> {
                    assertThat(e.getCollection()).isEqualTo("g_e2");
                    assertThat(e.getFrom()).containsExactlyInAnyOrder("g_a", "g_b");
                    assertThat(e.getTo()).containsExactlyInAnyOrder("g_c", "g_d");
                });
        assertThat(conf.vertices).containsExactlyInAnyOrder("g_a", "g_b", "g_c", "g_d", "g_x", "g_y", "g_z");
        assertThat(conf.edges).containsExactlyInAnyOrder("g_e1", "g_e2");
        assertThat(conf.driverConfig.getHosts()).isPresent()
                .get(as(InstanceOfAssertFactories.list(HostDescription.class)))
                .hasSize(1)
                .anySatisfy(host -> {
                    assertThat(host.getHost()).isEqualTo("127.0.0.1");
                    assertThat(host.getPort()).isEqualTo(8529);
                });
        assertThat(conf.driverConfig.getPassword()).isPresent().get().isEqualTo("test");
    }
}
