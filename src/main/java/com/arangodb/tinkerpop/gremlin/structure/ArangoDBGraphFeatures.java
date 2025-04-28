package com.arangodb.tinkerpop.gremlin.structure;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

public class ArangoDBGraphFeatures implements Graph.Features {

    public static class ArangoDBGraphGraphFeatures implements GraphFeatures {

        @Override
        public boolean supportsComputer() {
            return false;
        }

        @Override
        public boolean supportsThreadedTransactions() {
            return false;
        }

        @Override
        public boolean supportsTransactions() {
            return false;
        }
    }

    public static class ArangoDBGraphElementFeatures implements ElementFeatures {

        @Override
        public boolean supportsAnyIds() {
            return false;
        }

        @Override
        public boolean supportsCustomIds() {
            return false;
        }

        @Override
        public boolean supportsNumericIds() {
            return false;
        }

        @Override
        public boolean supportsUuidIds() {
            return false;
        }
    }

    public static class ArangoDBGraphVertexFeatures extends ArangoDBGraphElementFeatures implements VertexFeatures {

        @Override
        public VertexPropertyFeatures properties() {
            return new ArangoDBGraphVertexPropertyFeatures();
        }
    }

    public static class ArangoDBGraphEdgeFeatures extends ArangoDBGraphElementFeatures implements EdgeFeatures {
    }

    public static class ArangoDBGraphVertexPropertyFeatures implements VertexPropertyFeatures {

        @Override
        public boolean supportsAnyIds() {
            return false;
        }

        @Override
        public boolean supportsCustomIds() {
            return false;
        }

        @Override
        public boolean supportsNumericIds() {
            return false;
        }

        @Override
        public boolean supportsUuidIds() {
            return false;
        }
    }

    @Override
    public GraphFeatures graph() {
        return new ArangoDBGraphGraphFeatures();
    }

    @Override
    public VertexFeatures vertex() {
        return new ArangoDBGraphVertexFeatures();
    }

    @Override
    public EdgeFeatures edge() {
        return new ArangoDBGraphEdgeFeatures();
    }

    @Override
    public String toString() {
        return StringFactory.featureString(this);
    }
}
