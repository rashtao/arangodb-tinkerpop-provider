package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.tinkerpop.gremlin.TestGraphProvider;

import com.arangodb.tinkerpop.gremlin.arangodb.complex.ComplexElementIdTest;
import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.algorithm.generator.CommunityGeneratorTest;
import org.apache.tinkerpop.gremlin.algorithm.generator.DistributionGeneratorTest;
import org.apache.tinkerpop.gremlin.structure.VertexTest;

public class ComplexGraphProvider extends TestGraphProvider {

    @Override
    protected void customizeBuilder(ArangoDBConfigurationBuilder builder) {
        builder
                .simpleGraph(false)
                .graphClass(ComplexTestGraph.class);
    }

    @Override
    protected void configureDataDefinitions(ArangoDBConfigurationBuilder builder, Class<?> test, String testMethodName, LoadGraphWith.GraphData loadGraphWith) {
        // add default vertex and edge cols
        builder.withVertexCollection("vertex");
        builder.withEdgeCollection("edge");
        builder.configureEdge("edge", "vertex", "vertex");

        if (loadGraphWith != null) {
            switch (loadGraphWith) {
                case CLASSIC:
                    System.out.println("CLASSIC");
                    builder.withEdgeCollection("knows");
                    builder.withEdgeCollection("created");
                    builder.configureEdge("knows", "vertex", "vertex");
                    builder.configureEdge("created", "vertex", "vertex");
                    break;
                case MODERN:
                    System.out.println("MODERN");
                    builder.withVertexCollection("name");
                    builder.withVertexCollection("animal");
                    builder.withVertexCollection("dog");
                    builder.withVertexCollection("software");
                    builder.withVertexCollection("person");
                    builder.withEdgeCollection("knows");
                    builder.withEdgeCollection("created");
                    builder.withEdgeCollection("createdBy");
                    builder.withEdgeCollection("existsWith");
                    builder.withEdgeCollection("codeveloper");
                    builder.withEdgeCollection("uses");
                    builder.configureEdge("knows", "person", "person");
                    builder.configureEdge("created", "person", "software");
                    builder.configureEdge("createdBy", "software", "person");
                    builder.configureEdge("existsWith", "software", "software");
                    builder.configureEdge("codeveloper", "person", "person");
                    builder.configureEdge("uses", "person", "software");
                    break;
                case CREW:
                    System.out.println("CREW");
                    builder.withVertexCollection("software");
                    builder.withVertexCollection("person");
                    builder.withEdgeCollection("uses");
                    builder.withEdgeCollection("develops");
                    builder.withEdgeCollection("traverses");
                    builder.configureEdge("uses", "person", "software");
                    builder.configureEdge("develops", "person", "software");
                    builder.configureEdge("traverses", "software", "software");
                    break;
                case GRATEFUL:
                    System.out.println("GRATEFUL");
                    builder.withVertexCollection("song");
                    builder.withVertexCollection("artist");
                    builder.withEdgeCollection("followedBy");
                    builder.withEdgeCollection("sungBy");
                    builder.withEdgeCollection("writtenBy");
                    builder.configureEdge("followedBy", "vertex", "vertex");
                    builder.configureEdge("sungBy", "song", "artist");
                    builder.configureEdge("writtenBy", "song", "artist");
                    break;
                case SINK:
                    System.out.println("SINK");
                    builder.withVertexCollection("loops");
                    builder.withVertexCollection("message");
                    builder.withEdgeCollection("link");
                    builder.withEdgeCollection("self");
                    builder.configureEdge("self", "loops", "loops");
                    builder.configureEdge("link", "message", "message");
                    break;
            }
        } else {
            if (test == ComplexElementIdTest.class) {
                builder.withVertexCollection("foo");
            } else if (test == CommunityGeneratorTest.DifferentDistributionsTest.class
                    || test == DistributionGeneratorTest.DifferentDistributionsTest.class) {
                builder.withEdgeCollection("knows");
                builder.configureEdge("knows", "vertex", "vertex");
            } else if (testMethodName.startsWith("shouldProcessVerticesEdges")
                    || testMethodName.startsWith("shouldSetValueOnEdge")
                    || testMethodName.startsWith("shouldAutotype")) {
                builder.withEdgeCollection("knows");
                builder.configureEdge("knows", "vertex", "vertex");
            } else if (testMethodName.startsWith("shouldIterateEdgesWithStringIdSupport")) {
                builder.withEdgeCollection("self");
                builder.configureEdge("self", "vertex", "vertex");
            } else if (testMethodName.startsWith("shouldSupportUserSuppliedIds")) {
                builder.withEdgeCollection("test");
                builder.configureEdge("test", "vertex", "vertex");
            } else if (testMethodName.startsWith("shouldSupportUUID")) {
                builder.withVertexCollection("person");
                builder.withEdgeCollection("friend");
                builder.configureEdge("friend", "person", "person");
            } else if (testMethodName.startsWith("shouldReadWriteDetachedEdge")) {
                builder.withVertexCollection("person");
                builder.withEdgeCollection("friend");
                builder.configureEdge("friend", "person", "person");
            } else if (testMethodName.startsWith("shouldReadWriteDetachedEdgeAsReference")) {
                builder.withVertexCollection("person");
                builder.withEdgeCollection("friend");
                builder.configureEdge("friend", "person", "person");
            } else if (testMethodName.startsWith("shouldReadWriteEdge")) {
                builder.withVertexCollection("person");
                builder.withEdgeCollection("friend");
                builder.configureEdge("friend", "person", "person");
            } else if (testMethodName.startsWith("shouldThrowOnGraphEdgeSetPropertyStandard")) {
                builder.withEdgeCollection("self");
                builder.configureEdge("self", "vertex", "vertex");
            } else if (testMethodName.startsWith("shouldThrowOnGraphAddEdge")) {
                builder.withEdgeCollection("self");
                builder.configureEdge("self", "vertex", "vertex");
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
                builder.withVertexCollection("person");
                builder.withEdgeCollection("friends");
                builder.configureEdge("friends", "person", "person");
            } else {
                // Perhaps change for startsWith, but then it would be more verbose. Perhaps a set?
                switch (testMethodName) {
                    case "shouldGetPropertyKeysOnEdge":
                    case "shouldNotGetConcurrentModificationException":
                        builder.withEdgeCollection("friend");
                        builder.withEdgeCollection("knows");
                        builder.configureEdge("friend", "vertex", "vertex");
                        builder.configureEdge("knows", "vertex", "vertex");
                        break;
                    case "shouldTraverseInOutFromVertexWithMultipleEdgeLabelFilter":
                    case "shouldTraverseInOutFromVertexWithSingleEdgeLabelFilter":
                        builder.withEdgeCollection("hate");
                        builder.withEdgeCollection("friend");
                        builder.configureEdge("hate", "vertex", "vertex");
                        builder.configureEdge("friend", "vertex", "vertex");
                        break;
                    case "shouldPersistDataOnClose":
                        builder.withEdgeCollection("collaborator");
                        builder.configureEdge("collaborator", "vertex", "vertex");
                        break;
                    case "shouldTestTreeConnectivity":
                        builder.withEdgeCollection("test1");
                        builder.withEdgeCollection("test2");
                        builder.withEdgeCollection("test3");
                        builder.configureEdge("test1", "vertex", "vertex");
                        builder.configureEdge("test2", "vertex", "vertex");
                        builder.configureEdge("test3", "vertex", "vertex");
                        break;
                    case "shouldRemoveEdgesWithoutConcurrentModificationException":
                        builder.withEdgeCollection("link");
                        builder.configureEdge("link", "vertex", "vertex");
                        break;
                    case "shouldGetValueThatIsNotPresentOnEdge":
                    case "shouldHaveStandardStringRepresentationForEdgeProperty":
                    case "shouldHaveTruncatedStringRepresentationForEdgeProperty":
                    case "shouldValidateIdEquality":
                    case "shouldValidateEquality":
                    case "shouldHaveExceptionConsistencyWhenAssigningSameIdOnEdge":
                    case "shouldAllowNullAddEdge":
                    case "shouldAddEdgeWithUserSuppliedStringId":
                        builder.withEdgeCollection("self");
                        builder.configureEdge("self", "vertex", "vertex");
                        break;
                    case "shouldAllowRemovalFromEdgeWhenAlreadyRemoved":
                    case "shouldRespectWhatAreEdgesAndWhatArePropertiesInMultiProperties":
                    case "shouldProcessEdges":
                    case "shouldReturnOutThenInOnVertexIterator":
                    case "shouldReturnEmptyIteratorIfNoProperties":
                        builder.withEdgeCollection("knows");
                        builder.configureEdge("knows", "vertex", "vertex");
                        break;
                    case "shouldNotHaveAConcurrentModificationExceptionWhenIteratingAndRemovingAddingEdges":
                        builder.withEdgeCollection("knows");
                        builder.withEdgeCollection("pets");
                        builder.withEdgeCollection("walks");
                        builder.withEdgeCollection("livesWith");
                        builder.configureEdge("knows", "vertex", "vertex");
                        builder.configureEdge("pets", "vertex", "vertex");
                        builder.configureEdge("walks", "vertex", "vertex");
                        builder.configureEdge("livesWith", "vertex", "vertex");
                        break;
                    case "shouldHaveStandardStringRepresentation":
                        builder.withEdgeCollection("friends");
                        builder.configureEdge("friends", "vertex", "vertex");
                        break;
                    case "shouldReadWriteSelfLoopingEdges":
                        builder.withEdgeCollection("CONTROL");
                        builder.withEdgeCollection("SELFLOOP");
                        builder.configureEdge("CONTROL", "vertex", "vertex");
                        builder.configureEdge("SELFLOOP", "vertex", "vertex");
                        break;
                    case "shouldReadGraphML":
                    case "shouldReadGraphMLUnorderedElements":
                    case "shouldTransformGraphMLV2ToV3ViaXSLT":
                    case "shouldReadLegacyGraphSON":
                        builder.withEdgeCollection("knows");
                        builder.withEdgeCollection("created");
                        builder.configureEdge("knows", "vertex", "vertex");
                        builder.configureEdge("created", "vertex", "vertex");
                        break;
                    case "shouldAddVertexWithLabel":
                    case "shouldAllowNullAddVertexProperty":
                        builder.withVertexCollection("person");
                        break;
                    case "shouldNotAllowSetProperty":
                    case "shouldHashAndEqualCorrectly":
                    case "shouldNotAllowRemove":
                    case "shouldNotConstructNewWithSomethingAlreadyDetached":
                    case "shouldNotConstructNewWithSomethingAlreadyReferenced":
                        builder.withEdgeCollection("test");
                        builder.configureEdge("test", "vertex", "vertex");
                        break;
                    case "shouldHaveExceptionConsistencyWhenUsingNullVertex":
                        builder.withEdgeCollection("tonothing");
                        builder.configureEdge("tonothing", "vertex", "vertex");
                        break;
                    case "shouldHandleSelfLoops":
                        builder.withVertexCollection("person");
                        builder.withEdgeCollection("self");
                        builder.configureEdge("self", "person", "person");
                        break;
                    case "testAttachableCreateMethod":
                    case "shouldAttachWithCreateMethod":
                        builder.withVertexCollection("person");
                        builder.withVertexCollection("project");
                        builder.withEdgeCollection("knows");
                        builder.withEdgeCollection("developedBy");
                        builder.configureEdge("knows", "person", "person");
                        builder.configureEdge("developedBy", "project", "person");
                        break;
                    case "shouldConstructReferenceVertex":
                        builder.withVertexCollection("blah");
                        break;
                    case "shouldHaveExceptionConsistencyWhenUsingSystemVertexLabel":
                    case "shouldHaveExceptionConsistencyWhenUsingEmptyVertexLabel":
                    case "shouldHaveExceptionConsistencyWhenUsingEmptyVertexLabelOnOverload":
                    case "shouldHaveExceptionConsistencyWhenUsingSystemVertexLabelOnOverload":
                        if (VertexTest.class.equals(test.getEnclosingClass())) {
                            builder.withVertexCollection("foo");
                        }
                        break;
                    case "shouldHaveExceptionConsistencyWhenUsingNullVertexLabelOnOverload":
                    case "shouldHaveExceptionConsistencyWhenUsingNullVertexLabel":
                        builder.withVertexCollection("foo");
                        break;
                    case "shouldReadGraphMLWithCommonVertexAndEdgePropertyNames":
                        builder.withEdgeCollection("created");
                        builder.withEdgeCollection("knows");
                        builder.configureEdge("created", "vertex", "vertex");
                        builder.configureEdge("knows", "vertex", "vertex");
                        break;
                    case "shouldCopyFromGraphAToGraphB":
                        builder.withVertexCollection("person");
                        builder.withVertexCollection("software");
                        builder.withEdgeCollection("knows");
                        builder.withEdgeCollection("created");
                        builder.configureEdge("knows", "person", "person");
                        builder.configureEdge("created", "person", "software");
                        break;
                    case "shouldEvaluateConnectivityPatterns":
                        builder.withEdgeCollection("knows");
                        builder.withEdgeCollection("hates");
                        builder.configureEdge("knows", "vertex", "vertex");
                        builder.configureEdge("hates", "vertex", "vertex");
                        break;
                    case "g_addV_asXfirstX_repeatXaddEXnextX_toXaddVX_inVX_timesX5X_addEXnextX_toXselectXfirstXX":
                        builder.withEdgeCollection("next");
                        builder.configureEdge("next", "vertex", "vertex");
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
                        builder.withEdgeCollection("self");
                        builder.withEdgeCollection("self-but-different");
                        builder.withEdgeCollection("aTOa");
                        builder.withEdgeCollection("aTOb");
                        builder.withEdgeCollection("aTOc");
                        builder.withEdgeCollection("bTOc");
                        builder.withEdgeCollection("connectsTo");
                        builder.withEdgeCollection("knows");
                        builder.withEdgeCollection("relatesTo");
                        builder.configureEdge("self", "vertex", "vertex");
                        builder.configureEdge("self-but-different", "vertex", "vertex");
                        builder.configureEdge("aTOa", "vertex", "vertex");
                        builder.configureEdge("aTOb", "vertex", "vertex");
                        builder.configureEdge("aTOc", "vertex", "vertex");
                        builder.configureEdge("bTOc", "vertex", "vertex");
                        builder.configureEdge("connectsTo", "vertex", "vertex");
                        builder.configureEdge("knows", "vertex", "vertex");
                        builder.configureEdge("relatesTo", "vertex", "vertex");
                        break;
                    case "g_io_read_withXreader_graphsonX":
                    case "g_io_read_withXreader_gryoX":
                    case "g_io_read_withXreader_graphmlX":
                    case "g_io_readXjsonX":
                    case "g_io_readXkryoX":
                    case "g_io_readXxmlX":
                        builder.withVertexCollection("person");
                        builder.withVertexCollection("software");
                        builder.withEdgeCollection("knows");
                        builder.withEdgeCollection("created");
                        builder.configureEdge("knows", "person", "person");
                        builder.configureEdge("created", "person", "software");
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
                        builder.withVertexCollection("person");
                        builder.withEdgeCollection("knows");
                        builder.withEdgeCollection("self");
                        builder.configureEdge("knows", "person", "person");
                        builder.configureEdge("self", "person", "person");
                        break;
                    case "g_V_hasXname_regexXTinkerXX":
                    case "g_V_hasXname_regexXTinkerUnicodeXX":
                        builder.withVertexCollection("software");
                        break;
                    case "shouldDetachVertexWhenAdded":
                    case "shouldReferenceVertexWhenAdded":
                    case "shouldUseActualVertexWhenAdded":
                        builder.withVertexCollection("thing");
                        break;
                    case "shouldAppendPartitionToAllVertexProperties":
                        builder.withVertexCollection("person");
                        builder.configureEdge("edge", "person", "person");
                        break;
                    case "shouldPartitionWithAbstractLambdaChildTraversal":
                        builder.withVertexCollection("testV");
                        builder.withEdgeCollection("self");
                        builder.configureEdge("self", "testV", "testV");
                        break;
                }
            }
        }
    }
}
