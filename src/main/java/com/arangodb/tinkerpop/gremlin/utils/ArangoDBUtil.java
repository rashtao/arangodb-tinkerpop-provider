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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.arangodb.tinkerpop.gremlin.client.ArangoDBQueryBuilder;
import com.arangodb.tinkerpop.gremlin.structure.*;
import com.arangodb.tinkerpop.gremlin.PackageVersion;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods for creating properties and for normalising property and
 * collections names (to satisfy Arango DB naming conventions.
 *
 * @author Achim Brandt (http://www.triagens.de)
 * @author Johannes Gocke (http://www.triagens.de)
 * @author Horacio Hoyos Rodriguez (https://www.york.ac.uk)
 */
//FIXME We should add more util methods to validate attribute names, e.g. scape ".".
public class ArangoDBUtil {

    /**
     * The Logger.
     */

    private static final Logger logger = LoggerFactory.getLogger(ArangoDBUtil.class);

    /**
     * Utiliy mapper for conversions.
     **/

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Instantiates a new ArangoDB Util.
     */
    private ArangoDBUtil() {
        // this is a helper class
    }

    /**
     * Gets a collection that is unique for the given graph.
     *
     * @param graphName                 the graph name
     * @param collectionName            the collection name
     * @param shouldPrefixWithGraphName flag to indicate if the name should be prefixed
     * @return the unique collection name
     */
    @Deprecated
    public static String getCollectioName(String graphName, String collectionName, Boolean shouldPrefixWithGraphName) {
        if (shouldPrefixWithGraphName) {
            return String.format("%s_%s", graphName, collectionName);
        } else {
            return collectionName;
        }
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

    /**
     * Gets the correct primitive.
     *
     * @param value      the value
     * @param valueClass the exoected class of the value
     * @param <V>        the value type
     * @return the        correct Java primitive
     */

    @SuppressWarnings("unchecked")
    public static <V> Object getCorretctPrimitive(V value, String valueClass) {

        switch (valueClass) {
            case "java.lang.Float": {
                if (value instanceof Double) {
                    return ((Double) value).floatValue();
                } else if (value instanceof Long) {
                    return ((Long) value).floatValue();
                } else if (value instanceof Integer) {
                    return ((Integer) value).floatValue();
                } else {
                    logger.debug("Add conversion for {} to {}", value.getClass().getName(), valueClass);
                }
                break;
            }
            case "java.lang.Double": {
                if (value instanceof Double) {
                    return value;
                } else if (value instanceof Long) {
                    return ((Long) value).doubleValue();
                } else if (value instanceof Integer) {
                    return ((Integer) value).doubleValue();
                } else {
                    logger.debug("Add conversion for {} to {}", value.getClass().getName(), valueClass);
                }
                break;
            }
            case "java.lang.Long": {
                if (value instanceof Long) {
                    return value;
                } else if (value instanceof Double) {
                    return ((Double) value).longValue();
                } else if (value instanceof Integer) {
                    return ((Integer) value).longValue();
                } else {
                    logger.debug("Add conversion for {} to {}", value.getClass().getName(), valueClass);
                }
                break;
            }
            case "java.lang.Integer": {
                if (value instanceof Long) {
                    return ((Long) value).intValue();
                }
                break;
            }
            case "java.lang.String":
            case "java.lang.Boolean":
            case "":
                return value;
            case "java.util.HashMap":
                //logger.debug(((Map<?,?>)value).keySet().stream().map(Object::getClass).collect(Collectors.toList()));
                //logger.debug("Add conversion for map values to " + valueClass);
                // Maps are handled by ArangoOK, but we have an extra field, remove it
                Map<String, ?> valueMap = (Map<String, ?>) value;
                for (String key : valueMap.keySet()) {
                    if (key.startsWith("_")) {
                        valueMap.remove(key);
                    }
                    // We might need to check individual values...
                }
                break;
            case "java.util.ArrayList":
                // Should we save the type per item?
                List<Object> list = new ArrayList<>();
                ((ArrayList<?>) value).forEach(e -> list.add(getCorretctPrimitive(e, "")));
                return list;
            case "boolean[]":
                if (value instanceof List) {
                    List<Object> barray = (List<Object>) value;
                    boolean[] br = new boolean[barray.size()];
                    IntStream.range(0, barray.size())
                            .forEach(i -> br[i] = (boolean) barray.get(i));
                    return br;
                } else {
                    return value;
                }
            case "double[]":
                if (value instanceof List) {
                    List<Object> darray = (List<Object>) value;
                    double[] dr = new double[darray.size()];
                    IntStream.range(0, darray.size())
                            .forEach(i -> dr[i] = (double) getCorretctPrimitive(darray.get(i), "java.lang.Double"));
                    return dr;
                } else {
                    return value;
                }
            case "float[]":
                if (value instanceof List) {
                    List<Object> farray = (List<Object>) value;
                    float[] fr = new float[farray.size()];
                    IntStream.range(0, farray.size())
                            .forEach(i -> fr[i] = (float) getCorretctPrimitive(farray.get(i), "java.lang.Float"));
                    return fr;
                } else {
                    return value;
                }
            case "int[]":
                if (value instanceof List) {
                    List<Object> iarray = (List<Object>) value;
                    int[] ir = new int[iarray.size()];
                    IntStream.range(0, iarray.size())
                            .forEach(i -> ir[i] = (int) getCorretctPrimitive(iarray.get(i), "java.lang.Integer"));
                    return ir;
                } else {
                    return value;
                }
            case "long[]":
                if (value instanceof List) {
                    List<Object> larray = (List<Object>) value;
                    long[] lr = new long[larray.size()];
                    IntStream.range(0, larray.size())
                            .forEach(i -> lr[i] = (long) getCorretctPrimitive(larray.get(i), "java.lang.Long"));
                    return lr;
                } else {
                    return value;
                }
            case "java.lang.String[]":
                if (value instanceof List) {
                    List<Object> sarray = (List<Object>) value;
                    String[] sr = new String[sarray.size()];
                    IntStream.range(0, sarray.size())
                            .forEach(i -> sr[i] = (String) sarray.get(i));
                    return sr;
                } else {
                    return value;
                }
            default:
                Object result;
                try {
                    result = mapper.convertValue(value, Class.forName(valueClass));
                    return result;
                } catch (IllegalArgumentException | ClassNotFoundException e1) {
                    logger.warn("Type not deserializable", e1);
                }
                logger.debug("Add conversion for {} to {}", value.getClass().getName(), valueClass);
        }
        return value;
    }

    /**
     * Translate a Gremlin direction to Arango direction
     *
     * @param direction the direction to translate
     * @return the ArangoDBQueryBuilder.Direction that represents the gremlin direction
     */
    public static ArangoDBQueryBuilder.Direction getArangoDirectionFromGremlinDirection(final Direction direction) {
        switch (direction) {
            case BOTH:
                return ArangoDBQueryBuilder.Direction.ALL;
            case IN:
                return ArangoDBQueryBuilder.Direction.IN;
            case OUT:
                return ArangoDBQueryBuilder.Direction.OUT;
        }
        throw new IllegalArgumentException("Unsupported direction: " + direction);
    }

}
