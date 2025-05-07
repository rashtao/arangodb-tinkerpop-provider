/// ///////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the TinkerPop OLTP Provider API for ArangoDB
//
// Copyright triAGENS GmbH Cologne and The University of York
//
/// ///////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.tinkerpop.gremlin.utils;

import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.PackageVersion;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;

public class ArangoDBUtil {

    private ArangoDBUtil() {
    }

    public static void checkExistingGraph(GraphEntity info, ArangoDBGraphConfig config) {
        // check orphanCollections
        if (!CollectionUtils.isEqualCollection(info.getOrphanCollections(), config.orphanCollections)) {
            throw new IllegalStateException("Orphan collections do not match. From DB: "
                    + info.getOrphanCollections() + ", From config: " + config.orphanCollections);
        }

        // check edgeDefinitions
        Set<ArangoDBGraphConfig.EdgeDef> dbDefs = info.getEdgeDefinitions().stream()
                .map(ArangoDBGraphConfig.EdgeDef::of)
                .collect(Collectors.toSet());
        if (!CollectionUtils.isEqualCollection(dbDefs, config.edgeDefinitions, new EdgeDefEquator())) {
            throw new IllegalStateException("Edge definitions do not match. From DB: "
                    + dbDefs + ", From config: " + config.edgeDefinitions);
        }
    }

    private static class EdgeDefEquator implements Equator<ArangoDBGraphConfig.EdgeDef> {
        @Override
        public boolean equate(ArangoDBGraphConfig.EdgeDef a, ArangoDBGraphConfig.EdgeDef b) {
            return a.getCollection().equals(b.getCollection()) &&
                    CollectionUtils.isEqualCollection(a.getFrom(), b.getFrom()) &&
                    CollectionUtils.isEqualCollection(a.getTo(), b.getTo());
        }

        @Override
        public int hash(ArangoDBGraphConfig.EdgeDef o) {
            return Objects.hash(o.getCollection(), new HashSet<>(o.getFrom()), new HashSet<>(o.getTo()));
        }
    }

    public static String toString(Collection<EdgeDefinition> edgeDefinitions) {
        return "[" +
                edgeDefinitions.stream()
                        .map(ArangoDBUtil::toString)
                        .collect(Collectors.joining(",")) +
                "]";
    }

    public static String toString(EdgeDefinition edgeDefinition) {
        return "{" +
                "from:" +
                String.join(",", edgeDefinition.getFrom()) +
                "," +
                "to:" +
                String.join(",", edgeDefinition.getTo()) +
                "}";
    }

    public static void validatePropertyValue(Object value) {
        if (!supportsDataType(value)) {
            throw Property.Exceptions.dataTypeOfPropertyValueNotSupported(value);
        }
    }

    public static void validateVariableValue(Object value) {
        if (!supportsDataType(value)) {
            throw Graph.Variables.Exceptions.dataTypeOfVariableValueNotSupported(value);
        }
    }

    private static boolean supportsDataType(Object value) {
        return value == null ||
                value instanceof Boolean || value instanceof boolean[] ||
                value instanceof Double || value instanceof double[] ||
                value instanceof Integer || value instanceof int[] ||
                value instanceof Long || value instanceof long[] ||
                value instanceof String || value instanceof String[];
    }

    public static void checkVersion(String version) {
        if (new VersionComparator().compare(version, PackageVersion.VERSION) > 0) {
            throw new IllegalStateException("Existing graph has more recent version [" + version +
                    "] than library version [" + PackageVersion.VERSION + "].");
        }
    }

    private static class VersionComparator implements Comparator<String> {
        @Override
        public int compare(String a, String b) {
            Objects.requireNonNull(a);
            Objects.requireNonNull(b);
            Pattern versionPattern = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*).*");

            Matcher ma = versionPattern.matcher(a);
            if (!ma.matches()) {
                throw new IllegalArgumentException("Invalid version: " + a);
            }
            int aMajor = Integer.parseInt(ma.group(1));
            int aMinor = Integer.parseInt(ma.group(2));
            int aPatch = Integer.parseInt(ma.group(3));

            Matcher mb = versionPattern.matcher(b);
            if (!mb.matches()) {
                throw new IllegalArgumentException("Invalid version: " + b);
            }
            int bMajor = Integer.parseInt(mb.group(1));
            int bMinor = Integer.parseInt(mb.group(2));
            int bPatch = Integer.parseInt(mb.group(3));

            if (aMajor < bMajor) {
                return -1;
            }
            if (aMajor > bMajor) {
                return 1;
            }

            if (aMinor < bMinor) {
                return -1;
            }
            if (aMinor > bMinor) {
                return 1;
            }

            return Integer.compare(aPatch, bPatch);
        }
    }

}
