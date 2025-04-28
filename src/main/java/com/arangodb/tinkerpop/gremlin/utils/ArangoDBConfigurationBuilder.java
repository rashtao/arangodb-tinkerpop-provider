/// ///////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the TinkerPop OLTP Provider API for ArangoDB
//
// Copyright triAGENS GmbH Cologne and The University of York
//
/// ///////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.tinkerpop.gremlin.utils;

import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import org.apache.commons.configuration2.BaseConfiguration;

import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import static com.arangodb.config.ArangoConfigProperties.*;
import static com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig.*;


public class ArangoDBConfigurationBuilder {

    private final BaseConfiguration config = new BaseConfiguration();

    public ArangoDBConfigurationBuilder() {
        config.setProperty(Graph.GRAPH, ArangoDBGraph.class.getCanonicalName());
    }

    private ArangoDBConfigurationBuilder setProperty(final String key, final String value) {
        config.setProperty(KEY_PREFIX + "." + key, value);
        return this;
    }

    private ArangoDBConfigurationBuilder addProperty(final String key, final String value) {
        config.addProperty(KEY_PREFIX + "." + key, value);
        return this;
    }

    /**
     * Set driver property.
     * Properties from {@link ArangoConfigProperties} can be set using this method.
     *
     * @param key   property key
     * @param value property value
     * @return this
     */
    public ArangoDBConfigurationBuilder setDriverProperty(final String key, final String value) {
        config.setProperty(KEY_PREFIX + "." + KEY_DRIVER_PREFIX + "." + key, value);
        return this;
    }

    /**
     * Add driver property.
     * If it already exists then the value stated here will be added to the configuration entry.
     * Properties from {@link ArangoConfigProperties} can be set using this method.
     *
     * @param key   property key
     * @param value property value
     * @return this
     */
    public ArangoDBConfigurationBuilder addDriverProperty(final String key, final String value) {
        config.addProperty(KEY_PREFIX + "." + KEY_DRIVER_PREFIX + "." + key, value);
        return this;
    }

    /**
     * Name of the database to use.
     *
     * @param name the db name
     * @return this
     */
    public ArangoDBConfigurationBuilder database(String name) {
        return setProperty(KEY_DB_NAME, name);
    }

    /**
     * Name of the graph to use.
     *
     * @param name the graph name
     * @return this
     */
    public ArangoDBConfigurationBuilder graph(String name) {
        return setProperty(KEY_GRAPH_NAME, name);
    }

    /**
     * Set the graph type.
     *
     * @param graphType the graph type
     * @return this
     */
    public ArangoDBConfigurationBuilder graphType(ArangoDBGraphConfig.GraphType graphType) {
        return setProperty(KEY_GRAPH_TYPE, graphType.toString());
    }

    /**
     * Specify which graph to instantiate.
     *
     * @param graphClass the graph class
     * @return this
     */
    public ArangoDBConfigurationBuilder graphClass(Class<? extends ArangoDBGraph> graphClass) {
        config.setProperty(Graph.GRAPH, graphClass.getCanonicalName());
        return this;
    }

    /**
     * Add orphan collections.
     *
     * @param collections the orphan collections names
     * @return this
     */
    public ArangoDBConfigurationBuilder orphanCollections(String... collections) {
        for (String collection : collections) {
            addProperty(KEY_GRAPH_ORPHAN_COLLECTIONS, collection);
        }
        return this;
    }

    /**
     * Add edge definitions.
     *
     * @param configs the edge definitions
     * @return this
     */
    public ArangoDBConfigurationBuilder edgeDefinitions(EdgeDef... configs) {
        for (EdgeDef config : configs) {
            addProperty(KEY_GRAPH_EDGE_DEFINITIONS, config.toString());
        }
        return this;
    }

    /**
     * Add hosts.
     *
     * @param hosts the hosts in the form of host:port
     * @return this
     */
    public ArangoDBConfigurationBuilder hosts(String... hosts) {
        for (String host : hosts) {
            addDriverProperty(KEY_HOSTS, host);
        }
        return this;
    }

    /**
     * Set the db user.
     *
     * @param user the user
     * @return this
     */
    public ArangoDBConfigurationBuilder user(String user) {
        return setDriverProperty(KEY_USER, user);
    }

    /**
     * Set the db password.
     *
     * @param password the password
     * @return this
     */
    public ArangoDBConfigurationBuilder password(String password) {
        return setDriverProperty(KEY_PASSWORD, password);
    }

    /**
     * Build the configuration.
     *
     * @return a configuration that can be used to instantiate a new {@link ArangoDBGraph}.
     * @see ArangoDBGraph#open(org.apache.commons.configuration2.Configuration)
     */
    public Configuration build() {
        return config;
    }

}
