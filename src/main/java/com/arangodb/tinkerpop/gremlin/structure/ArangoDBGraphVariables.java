/// ///////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the TinkerPop OLTP Provider API for ArangoDB
//
// Copyright triAGENS GmbH Cologne and The University of York
//
/// ///////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.tinkerpop.gremlin.structure;


import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.arangodb.tinkerpop.gremlin.persistence.VariablesData;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBUtil;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphVariableHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;


public class ArangoDBGraphVariables implements Graph.Variables {

    private final ArangoDBGraph graph;
    private final VariablesData data;

    ArangoDBGraphVariables(ArangoDBGraph graph, VariablesData data) {
        this.graph = graph;
        this.data = data;
    }

    public String getVersion() {
        return data.getVersion();
    }

    void updateVersion(String version) {
        data.setVersion(version);
        update();
    }

    @Override
    public Set<String> keys() {
        return data.entries()
                .map(Map.Entry::getKey)
                .filter(it -> !Graph.Hidden.isHidden(it))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Optional<R> get(String key) {
        return data.entries()
                .filter(e -> key.equals(e.getKey()))
                .map(Map.Entry::getValue)
                .map(it -> (R) it)
                .findFirst();
    }

    @Override
    public void set(String key, Object value) {
        GraphVariableHelper.validateVariable(key, value);
        ArangoDBUtil.validateVariableValue(value);
        data.add(key, value);
        update();
    }

    @Override
    public void remove(String key) {
        data.remove(key);
        update();
    }

    private void update() {
        graph.getClient().updateGraphVariables(data);
    }

    @Override
    public String toString() {
        return StringFactory.graphVariablesString(this);
    }

}
