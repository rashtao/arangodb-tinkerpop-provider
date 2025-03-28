package com.arangodb.tinkerpop.gremlin.persistence;

import com.arangodb.serde.jackson.Key;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class VariablesData extends SimplePropertyData {

    private final String key;
    private final String version;

    @JsonCreator
    public VariablesData(@Key String key, @JsonProperty("version") String version) {
        this.key = key;
        this.version = version;
    }

    @Key
    public String getKey() {
        return key;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "VariablesData{" +
                "key='" + key + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VariablesData)) return false;
        if (!super.equals(o)) return false;
        VariablesData that = (VariablesData) o;
        return Objects.equals(key, that.key) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, version);
    }
}
