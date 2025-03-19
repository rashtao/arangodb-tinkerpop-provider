package com.arangodb.tinkerpop.gremlin.persistence;


import com.arangodb.shaded.fasterxml.jackson.core.JsonGenerator;
import com.arangodb.shaded.fasterxml.jackson.core.JsonParser;
import com.arangodb.shaded.fasterxml.jackson.databind.DeserializationContext;
import com.arangodb.shaded.fasterxml.jackson.databind.JsonDeserializer;
import com.arangodb.shaded.fasterxml.jackson.databind.JsonSerializer;
import com.arangodb.shaded.fasterxml.jackson.databind.SerializerProvider;
import com.arangodb.shaded.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.arangodb.shaded.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@JsonSerialize(using = ArangoId.Serializer.class)
@JsonDeserialize(using = ArangoId.Deserializer.class)
class ArangoId implements ElementId {
    private final String prefix;
    private final String label;
    private final String key;

    public static ArangoId of(String prefix, String label, String key) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(label);
        validateIdParts(prefix, label, key);
        return new ArangoId(prefix, label, key);
    }

    public static ArangoId parse(String prefix, String fullName) {
        String[] parts = fullName.replaceFirst("^" + prefix + "_", "").split("/");
        String label = parts[0];
        String key = parts[1];
        return ArangoId.of(prefix, label, key);
    }

    public static ArangoId parseWithDefaultLabel(String prefix, String defaultLabel, String fullName) {
        String[] parts = fullName.replaceFirst("^" + prefix + "_", "").split("/");
        String label = parts.length == 2 ? parts[0] : defaultLabel;
        String key = parts[parts.length - 1];
        return ArangoId.of(prefix, label, key);
    }

    public static ArangoId parse(String fullName) {
        String[] parts = fullName.split("_");
        String prefix = parts[0];
        parts = parts[1].split("/");
        String collection = parts[0];
        String key = parts[1];
        return ArangoId.of(prefix, collection, key);
    }

    private static void validateIdParts(String... names) {
        for (String name : names) {
            if (name == null)
                continue;
            if (name.contains("_")) {
                throw new IllegalArgumentException(String.format("key (%s) contains invalid character '_'", name));
            }
            if (name.contains("/")) {
                throw new IllegalArgumentException(String.format("key (%s) contains invalid character '/'", name));
            }
        }
    }

    private ArangoId(String prefix, String label, String key) {
        this.prefix = prefix;
        this.label = label.replaceFirst("^" + prefix + "_", "");
        this.key = key;
    }

    @Override
    public ArangoId withKey(String newKey) {
        return ArangoId.of(prefix, label, newKey);
    }

    @Override
    public String getCollection() {
        return prefix + "_" + label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getId() {
        return Optional.ofNullable(key)
                .map(it -> label + '/' + it)
                .orElse(null);
    }

    @Override
    public String toString() {
        return prefix + "_" + label + "/" + key;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArangoId)) return false;
        ArangoId arangoId = (ArangoId) o;
        return Objects.equals(prefix, arangoId.prefix) && Objects.equals(label, arangoId.label) && Objects.equals(key, arangoId.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, label, key);
    }

    static class Serializer extends JsonSerializer<ArangoId> {
        @Override
        public void serialize(ArangoId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString());
        }
    }

    static class Deserializer extends JsonDeserializer<ArangoId> {
        @Override
        public ArangoId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return ArangoId.parse(p.getValueAsString());
        }
    }
}
