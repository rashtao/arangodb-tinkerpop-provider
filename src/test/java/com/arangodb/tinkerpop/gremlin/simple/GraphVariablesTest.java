package com.arangodb.tinkerpop.gremlin.simple;

import com.arangodb.tinkerpop.gremlin.AbstractTest;
import com.arangodb.tinkerpop.gremlin.PackageVersion;
import com.arangodb.tinkerpop.gremlin.persistence.VariablesData;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphVariables;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SuppressWarnings("resource")
public class GraphVariablesTest extends AbstractTest {

    private Configuration conf;
    private String name;

    @Before
    public void before() {
        conf = confBuilder().build();
        name = getName(conf);
        client.clear(name);
    }

    @Test
    public void shouldSaveVersion() {
        assertThat(client.variablesCollection().documentExists(name)).isFalse();
        ArangoDBGraph g = createGraph(conf);
        assertThat(((ArangoDBGraphVariables) g.variables()).getVersion()).isEqualTo(PackageVersion.VERSION);
        VariablesData vData = client.variablesCollection().getDocument(name, VariablesData.class);
        assertThat(vData.getVersion()).isEqualTo(PackageVersion.VERSION);
    }

    @Test
    public void shouldUpdateVersion() {
        VariablesData vData0 = new VariablesData(name, "0.0.1");
        client.variablesCollection().insertDocument(vData0);
        assertThat(client.variablesCollection().documentExists(name)).isTrue();
        ArangoDBGraph g = createGraph(conf);
        assertThat(((ArangoDBGraphVariables) g.variables()).getVersion()).isEqualTo(PackageVersion.VERSION);
        VariablesData vData = client.variablesCollection().getDocument(name, VariablesData.class);
        assertThat(vData.getVersion()).isEqualTo(PackageVersion.VERSION);
    }

    @Test
    public void shouldThrowOnMoreRecentVersions() {
        VariablesData vData0 = new VariablesData(name, "999.999.999");
        client.variablesCollection().insertDocument(vData0);
        assertThat(client.variablesCollection().documentExists(name)).isTrue();
        Throwable thrown = catchThrowable(() -> createGraph(conf));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .cause()
                .isInstanceOf(InvocationTargetException.class);
        assertThat(((InvocationTargetException) thrown.getCause()).getTargetException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Existing graph has more recent version [999.999.999] than library version");
    }

}
