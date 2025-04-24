package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.tinkerpop.gremlin.TestGraphProvider;

import com.arangodb.tinkerpop.gremlin.arangodb.complex.ComplexElementIdTest;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraphConfig.EdgeDef;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.algorithm.generator.CommunityGeneratorTest;
import org.apache.tinkerpop.gremlin.algorithm.generator.DistributionGeneratorTest;
import org.apache.tinkerpop.gremlin.structure.VertexTest;

public class ComplexGraphProvider extends TestGraphProvider {

    @Override
    protected void customizeBuilder(ArangoDBConfigurationBuilder builder) {
        builder
                .graphType(ArangoDBGraphConfig.GraphType.COMPLEX)
                .graphClass(ComplexTestGraph.class);
    }

    @Override
    protected void configureDataDefinitions(ArangoDBConfigurationBuilder builder, Class<?> test, String testMethodName, LoadGraphWith.GraphData loadGraphWith) {
        // add default vertex and edge cols
        builder.edgeDefinitions(EdgeDef.of("edge").from("vertex").to("vertex"));

        if (loadGraphWith != null) {
            switch (loadGraphWith) {
                case CLASSIC:
                    System.out.println("CLASSIC");
                    builder.edgeDefinitions(EdgeDef.of("knows").from("vertex").to("vertex"));
                    builder.edgeDefinitions(EdgeDef.of("created").from("vertex").to("vertex"));
                    break;
                case MODERN:
                    System.out.println("MODERN");
                    builder.orphanCollections("name");
                    builder.orphanCollections("animal");
                    builder.orphanCollections("dog");
                    builder.edgeDefinitions(EdgeDef.of("knows").from("person").to("person"));
                    builder.edgeDefinitions(EdgeDef.of("created").from("person").to("software"));
                    builder.edgeDefinitions(EdgeDef.of("createdBy").from("software").to("person"));
                    builder.edgeDefinitions(EdgeDef.of("existsWith").from("software").to("software"));
                    builder.edgeDefinitions(EdgeDef.of("codeveloper").from("person").to("person"));
                    builder.edgeDefinitions(EdgeDef.of("uses").from("person").to("software"));
                    break;
                case CREW:
                    System.out.println("CREW");
                    builder.edgeDefinitions(EdgeDef.of("uses").from("person").to("software"));
                    builder.edgeDefinitions(EdgeDef.of("develops").from("person").to("software"));
                    builder.edgeDefinitions(EdgeDef.of("traverses").from("software").to("software"));
                    break;
                case GRATEFUL:
                    System.out.println("GRATEFUL");
                    builder.edgeDefinitions(EdgeDef.of("followedBy").from("vertex").to("vertex"));
                    builder.edgeDefinitions(EdgeDef.of("sungBy").from("song").to("artist"));
                    builder.edgeDefinitions(EdgeDef.of("writtenBy").from("song").to("artist"));
                    break;
                case SINK:
                    System.out.println("SINK");
                    builder.edgeDefinitions(EdgeDef.of("self").from("loops").to("loops"));
                    builder.edgeDefinitions(EdgeDef.of("link").from("message").to("message"));
                    break;
            }
        } else {
            if (test == ComplexElementIdTest.class) {
                builder.orphanCollections("foo");
            } else if (test == CommunityGeneratorTest.DifferentDistributionsTest.class
                    || test == DistributionGeneratorTest.DifferentDistributionsTest.class) {
                builder.edgeDefinitions(EdgeDef.of("knows").from("vertex").to("vertex"));
            } else if (testMethodName.startsWith("shouldProcessVerticesEdges")
                    || testMethodName.startsWith("shouldSetValueOnEdge")
                    || testMethodName.startsWith("shouldAutotype")) {
                builder.edgeDefinitions(EdgeDef.of("knows").from("vertex").to("vertex"));
            } else if (testMethodName.startsWith("shouldIterateEdgesWithStringIdSupport")) {
                builder.edgeDefinitions(EdgeDef.of("self").from("vertex").to("vertex"));
            } else if (testMethodName.startsWith("shouldSupportUserSuppliedIds")) {
                builder.edgeDefinitions(EdgeDef.of("test").from("vertex").to("vertex"));
            } else if (testMethodName.startsWith("shouldSupportUUID")) {
                builder.edgeDefinitions(EdgeDef.of("friend").from("person").to("person"));
            } else if (testMethodName.startsWith("shouldReadWriteDetachedEdge")) {
                builder.edgeDefinitions(EdgeDef.of("friend").from("person").to("person"));
            } else if (testMethodName.startsWith("shouldReadWriteDetachedEdgeAsReference")) {
                builder.edgeDefinitions(EdgeDef.of("friend").from("person").to("person"));
            } else if (testMethodName.startsWith("shouldReadWriteEdge")) {
                builder.edgeDefinitions(EdgeDef.of("friend").from("person").to("person"));
            } else if (testMethodName.startsWith("shouldThrowOnGraphEdgeSetPropertyStandard")) {
                builder.orphanCollections("self");
                builder.edgeDefinitions(EdgeDef.of("self").from("vertex").to("vertex"));
            } else if (testMethodName.startsWith("shouldThrowOnGraphAddEdge")) {
                builder.edgeDefinitions(EdgeDef.of("self").from("vertex").to("vertex"));
            } else if (testMethodName.startsWith("shouldReadWriteVerticesNoEdgesToGryoManual") ||
                    testMethodName.startsWith("shouldReadWriteVertexWithBOTHEdges") ||
                    testMethodName.startsWith("shouldReadWriteVerticesNoEdgesToGraphSONManual") ||
                    testMethodName.startsWith("shouldReadWriteVerticesNoEdges") ||
                    testMethodName.startsWith("shouldReadWriteVertexWithINEdges") ||
                    testMethodName.startsWith("shouldReadWriteVertexMultiPropsNoEdges") ||
                    testMethodName.startsWith("shouldReadWriteDetachedVertexAsReferenceNoEdges") ||
                    testMethodName.startsWith("shouldReadWriteVertexNoEdges") ||
                    testMethodName.startsWith("shouldReadWriteVertexWithOUTEdges") ||
                    testMethodName.startsWith("shouldReadWriteDetachedVertexNoEdges")) {
                builder.edgeDefinitions(EdgeDef.of("friends").from("person").to("person"));
            } else {
                // Perhaps change for startsWith, but then it would be more verbose. Perhaps a set?
                switch (testMethodName) {
                    case "shouldGetPropertyKeysOnEdge":
                    case "shouldNotGetConcurrentModificationException":
                        builder.edgeDefinitions(EdgeDef.of("friend").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("knows").from("vertex").to("vertex"));
                        break;
                    case "shouldTraverseInOutFromVertexWithMultipleEdgeLabelFilter":
                    case "shouldTraverseInOutFromVertexWithSingleEdgeLabelFilter":
                        builder.edgeDefinitions(EdgeDef.of("hate").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("friend").from("vertex").to("vertex"));
                        break;
                    case "shouldPersistDataOnClose":
                        builder.edgeDefinitions(EdgeDef.of("collaborator").from("vertex").to("vertex"));
                        break;
                    case "shouldTestTreeConnectivity":
                        builder.edgeDefinitions(EdgeDef.of("test1").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("test2").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("test3").from("vertex").to("vertex"));
                        break;
                    case "shouldRemoveEdgesWithoutConcurrentModificationException":
                        builder.edgeDefinitions(EdgeDef.of("link").from("vertex").to("vertex"));
                        break;
                    case "shouldGetValueThatIsNotPresentOnEdge":
                    case "shouldHaveStandardStringRepresentationForEdgeProperty":
                    case "shouldHaveTruncatedStringRepresentationForEdgeProperty":
                    case "shouldValidateIdEquality":
                    case "shouldValidateEquality":
                    case "shouldHaveExceptionConsistencyWhenAssigningSameIdOnEdge":
                    case "shouldAllowNullAddEdge":
                    case "shouldAddEdgeWithUserSuppliedStringId":
                        builder.edgeDefinitions(EdgeDef.of("self").from("vertex").to("vertex"));
                        break;
                    case "shouldAllowRemovalFromEdgeWhenAlreadyRemoved":
                    case "shouldRespectWhatAreEdgesAndWhatArePropertiesInMultiProperties":
                    case "shouldProcessEdges":
                    case "shouldReturnOutThenInOnVertexIterator":
                    case "shouldReturnEmptyIteratorIfNoProperties":
                        builder.edgeDefinitions(EdgeDef.of("knows").from("vertex").to("vertex"));
                        break;
                    case "shouldNotHaveAConcurrentModificationExceptionWhenIteratingAndRemovingAddingEdges":
                        builder.edgeDefinitions(EdgeDef.of("knows").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("pets").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("walks").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("livesWith").from("vertex").to("vertex"));
                        break;
                    case "shouldHaveStandardStringRepresentation":
                        builder.edgeDefinitions(EdgeDef.of("friends").from("vertex").to("vertex"));
                        break;
                    case "shouldReadWriteSelfLoopingEdges":
                        builder.edgeDefinitions(EdgeDef.of("CONTROL").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("SELFLOOP").from("vertex").to("vertex"));
                        break;
                    case "shouldReadGraphML":
                    case "shouldReadGraphMLUnorderedElements":
                    case "shouldTransformGraphMLV2ToV3ViaXSLT":
                    case "shouldReadLegacyGraphSON":
                        builder.edgeDefinitions(EdgeDef.of("knows").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("created").from("vertex").to("vertex"));
                        break;
                    case "shouldAddVertexWithLabel":
                    case "shouldAllowNullAddVertexProperty":
                        builder.orphanCollections("person");
                        break;
                    case "shouldNotAllowSetProperty":
                    case "shouldHashAndEqualCorrectly":
                    case "shouldNotAllowRemove":
                    case "shouldNotConstructNewWithSomethingAlreadyDetached":
                    case "shouldNotConstructNewWithSomethingAlreadyReferenced":
                        builder.edgeDefinitions(EdgeDef.of("test").from("vertex").to("vertex"));
                        break;
                    case "shouldHaveExceptionConsistencyWhenUsingNullVertex":
                        builder.edgeDefinitions(EdgeDef.of("tonothing").from("vertex").to("vertex"));
                        break;
                    case "shouldHandleSelfLoops":
                        builder.edgeDefinitions(EdgeDef.of("self").from("person").to("person"));
                        break;
                    case "testAttachableCreateMethod":
                    case "shouldAttachWithCreateMethod":
                        builder.edgeDefinitions(EdgeDef.of("knows").from("person").to("person"));
                        builder.edgeDefinitions(EdgeDef.of("developedBy").from("project").to("person"));
                        break;
                    case "shouldConstructReferenceVertex":
                        builder.orphanCollections("blah");
                        break;
                    case "shouldHaveExceptionConsistencyWhenUsingSystemVertexLabel":
                    case "shouldHaveExceptionConsistencyWhenUsingEmptyVertexLabel":
                    case "shouldHaveExceptionConsistencyWhenUsingEmptyVertexLabelOnOverload":
                    case "shouldHaveExceptionConsistencyWhenUsingSystemVertexLabelOnOverload":
                        if (VertexTest.class.equals(test.getEnclosingClass())) {
                            builder.orphanCollections("foo");
                        }
                        break;
                    case "shouldHaveExceptionConsistencyWhenUsingNullVertexLabelOnOverload":
                    case "shouldHaveExceptionConsistencyWhenUsingNullVertexLabel":
                        builder.orphanCollections("foo");
                        break;
                    case "shouldReadGraphMLWithCommonVertexAndEdgePropertyNames":
                        builder.edgeDefinitions(EdgeDef.of("created").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("knows").from("vertex").to("vertex"));
                        break;
                    case "shouldCopyFromGraphAToGraphB":
                        builder.edgeDefinitions(EdgeDef.of("knows").from("person").to("person"));
                        builder.edgeDefinitions(EdgeDef.of("created").from("person").to("software"));
                        break;
                    case "shouldEvaluateConnectivityPatterns":
                        builder.edgeDefinitions(EdgeDef.of("knows").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("hates").from("vertex").to("vertex"));
                        break;
                    case "g_addV_asXfirstX_repeatXaddEXnextX_toXaddVX_inVX_timesX5X_addEXnextX_toXselectXfirstXX":
                        builder.edgeDefinitions(EdgeDef.of("next").from("vertex").to("vertex"));
                        break;
                    case "shouldGenerateDefaultIdOnAddEWithSpecifiedId":
                    case "shouldSetIdOnAddEWithNamePropertyKeySpecifiedAndNameSuppliedAsProperty":
                    case "shouldSetIdOnAddEWithIdPropertyKeySpecifiedAndNameSuppliedAsProperty":
                    case "shouldGenerateDefaultIdOnAddEWithGeneratedId":
                    case "shouldTriggerAddEdgePropertyAdded":
                    case "shouldReferencePropertyOfEdgeWhenRemoved":
                    case "shouldTriggerAddEdge":
                    case "shouldTriggerRemoveEdge":
                    case "shouldTriggerRemoveEdgeProperty":
                    case "shouldReferenceEdgeWhenRemoved":
                    case "shouldUseActualEdgeWhenAdded":
                    case "shouldDetachEdgeWhenAdded":
                    case "shouldUseActualEdgeWhenRemoved":
                    case "shouldDetachPropertyOfEdgeWhenNew":
                    case "shouldDetachPropertyOfEdgeWhenChanged":
                    case "shouldUseActualPropertyOfEdgeWhenChanged":
                    case "shouldDetachEdgeWhenRemoved":
                    case "shouldTriggerUpdateEdgePropertyAddedViaMergeE":
                    case "shouldDetachPropertyOfEdgeWhenRemoved":
                    case "shouldUseActualPropertyOfEdgeWhenRemoved":
                    case "shouldUseActualPropertyOfEdgeWhenNew":
                    case "shouldTriggerEdgePropertyChanged":
                    case "shouldTriggerAddEdgeViaMergeE":
                    case "shouldReferencePropertyOfEdgeWhenNew":
                    case "shouldReferenceEdgeWhenAdded":
                    case "shouldReferencePropertyOfEdgeWhenChanged":
                    case "shouldTriggerAddEdgeByPath":
                    case "shouldWriteToMultiplePartitions":
                    case "shouldAppendPartitionToEdge":
                    case "shouldThrowExceptionOnEInDifferentPartition":
                        builder.edgeDefinitions(EdgeDef.of("self").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("self-but-different").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("aTOa").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("aTOb").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("aTOc").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("bTOc").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("connectsTo").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("knows").from("vertex").to("vertex"));
                        builder.edgeDefinitions(EdgeDef.of("relatesTo").from("vertex").to("vertex"));
                        break;
                    case "g_io_read_withXreader_graphsonX":
                    case "g_io_read_withXreader_gryoX":
                    case "g_io_read_withXreader_graphmlX":
                    case "g_io_readXjsonX":
                    case "g_io_readXkryoX":
                    case "g_io_readXxmlX":
                        builder.edgeDefinitions(EdgeDef.of("knows").from("person").to("person"));
                        builder.edgeDefinitions(EdgeDef.of("created").from("person").to("software"));
                        break;
                    case "g_addV_propertyXlabel_personX":
                    case "g_mergeEXlabel_knows_out_marko_in_vadasX_optionXonCreate_created_YX_optionXonMatch_created_NX_exists_updated":
                    case "g_mergeEXlabel_knows_out_marko_in_vadas_weight_05X_exists":
                    case "g_V_hasXperson_name_marko_X_mergeEXlabel_knowsX_optionXonCreate_created_YX_optionXonMatch_created_NX_exists_updated":
                    case "g_mergeEXlabel_knows_out_marko_in_vadasX":
                    case "g_mergeEXlabel_knows_out_marko_in_vadasX_optionXonCreate_created_YX_optionXonMatch_created_NX_exists":
                    case "g_V_mergeEXlabel_self_weight_05X":
                    case "g_injectXlabel_knows_out_marko_in_vadasX_mergeE":
                    case "g_mergeE_with_outV_inV_options":
                        builder.edgeDefinitions(EdgeDef.of("knows").from("person").to("person"));
                        builder.edgeDefinitions(EdgeDef.of("self").from("person").to("person"));
                        break;
                    case "g_V_hasXname_regexXTinkerXX":
                    case "g_V_hasXname_regexXTinkerUnicodeXX":
                        builder.orphanCollections("software");
                        break;
                    case "shouldDetachVertexWhenAdded":
                    case "shouldReferenceVertexWhenAdded":
                    case "shouldUseActualVertexWhenAdded":
                        builder.orphanCollections("thing");
                        break;
                    case "shouldAppendPartitionToAllVertexProperties":
                        builder.edgeDefinitions(EdgeDef.of("edge").from("person").to("person"));
                        break;
                    case "shouldPartitionWithAbstractLambdaChildTraversal":
                        builder.edgeDefinitions(EdgeDef.of("self").from("testV").to("testV"));
                        break;
                }
            }
        }
    }
}
