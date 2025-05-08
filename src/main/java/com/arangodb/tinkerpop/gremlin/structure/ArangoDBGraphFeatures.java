package com.arangodb.tinkerpop.gremlin.structure;

import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

public class ArangoDBGraphFeatures implements Graph.Features {

    public static class ArangoDBGraphGraphFeatures implements GraphFeatures {
        @Override
        public boolean supportsComputer() {
            return false;
        }

        @Override
        public boolean supportsTransactions() {
            return false;
        }

        @Override
        public boolean supportsThreadedTransactions() {
            return false;
        }

        @Override
        public boolean supportsOrderabilitySemantics() {
            return false;
        }

        public VariableFeatures variables() {
            return new ArangoGraphVariableFeatures();
        }
    }

    public static class ArangoGraphVariableFeatures extends ArangoDBGraphDataTypeFeatures implements VariableFeatures {
    }

    public static class ArangoDBGraphDataTypeFeatures implements DataTypeFeatures {
        @Override
        public boolean supportsByteValues() {
            return false;
        }

        @Override
        public boolean supportsFloatValues() {
            return false;
        }

        @Override
        public boolean supportsMapValues() {
            return false;
        }

        @Override
        public boolean supportsMixedListValues() {
            return false;
        }

        @Override
        public boolean supportsByteArrayValues() {
            return false;
        }

        @Override
        public boolean supportsFloatArrayValues() {
            return false;
        }

        @Override
        public boolean supportsSerializableValues() {
            return false;
        }

        @Override
        public boolean supportsUniformListValues() {
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

    public static class ArangoDBGraphPropertyFeatures extends ArangoDBGraphDataTypeFeatures implements PropertyFeatures {
    }

    public static class ArangoDBGraphVertexPropertyFeatures extends ArangoDBGraphPropertyFeatures implements VertexPropertyFeatures {
        @Override
        public boolean supportsUserSuppliedIds() {
            return false;
        }

        @Override
        public boolean supportsAnyIds() {
            return false;
        }
    }

    public static class ArangoDBGraphVertexFeatures extends ArangoDBGraphElementFeatures implements VertexFeatures {
        @Override
        public VertexPropertyFeatures properties() {
            return new ArangoDBGraphVertexPropertyFeatures();
        }

        @Override
        public boolean supportsMultiProperties() {
            return false;
        }

        @Override
        public VertexProperty.Cardinality getCardinality(final String key) {
            return VertexProperty.Cardinality.single;
        }
    }

    public static class ArangoDBGraphEdgePropertyFeatures extends ArangoDBGraphPropertyFeatures implements EdgePropertyFeatures {
    }

    public static class ArangoDBGraphEdgeFeatures extends ArangoDBGraphElementFeatures implements EdgeFeatures {
        public EdgePropertyFeatures properties() {
            return new ArangoDBGraphEdgePropertyFeatures();
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
