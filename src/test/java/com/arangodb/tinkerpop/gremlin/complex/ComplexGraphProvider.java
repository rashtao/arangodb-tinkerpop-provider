package com.arangodb.tinkerpop.gremlin.complex;

import com.arangodb.tinkerpop.gremlin.TestGraphProvider;

import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.structure.VertexTest;

public class ComplexGraphProvider extends TestGraphProvider {
    @Override
    protected void configure(ArangoDBConfigurationBuilder builder, Class<?> test, String testMethodName, LoadGraphWith.GraphData loadGraphWith) {
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
                    builder.withVertexCollection("vertex");
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
                    builder.withVertexCollection("vertex");
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
            if (testMethodName.startsWith("shouldProcessVerticesEdges")
                    || testMethodName.startsWith("shouldGenerate")
                    || testMethodName.startsWith("shouldSetValueOnEdge")
                    || testMethodName.startsWith("shouldAutotype")) {
                builder.withEdgeCollection("knows");
            } else if (testMethodName.startsWith("shouldIterateEdgesWithStringIdSupport")) {
                builder.withEdgeCollection("self");
            } else if (testMethodName.startsWith("shouldSupportUserSuppliedIds")) {
                builder.withEdgeCollection("test");
            } else if (testMethodName.startsWith("shouldSupportUUID")) {
                builder.withVertexCollection("person");
                builder.withEdgeCollection("friend");
            } else if (testMethodName.startsWith("shouldReadWriteDetachedEdge")) {
                builder.withVertexCollection("person");
                builder.withEdgeCollection("friend");
            } else if (testMethodName.startsWith("shouldReadWriteDetachedEdgeAsReference")) {
                builder.withVertexCollection("person");
                builder.withEdgeCollection("friend");
            } else if (testMethodName.startsWith("shouldReadWriteEdge")) {
                builder.withVertexCollection("person");
                builder.withEdgeCollection("friend");
            } else if (testMethodName.startsWith("shouldThrowOnGraphEdgeSetPropertyStandard")) {
                builder.withEdgeCollection("self");
            } else if (testMethodName.startsWith("shouldThrowOnGraphAddEdge")) {
                builder.withEdgeCollection("self");
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
                builder.withVertexCollection("vertex");
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
                        break;
                    case "shouldTraverseInOutFromVertexWithMultipleEdgeLabelFilter":
                    case "shouldTraverseInOutFromVertexWithSingleEdgeLabelFilter":
                        builder.withEdgeCollection("hate");
                        builder.withEdgeCollection("friend");
                        break;
                    case "shouldPersistDataOnClose":
                        builder.withEdgeCollection("collaborator");
                        break;
                    case "shouldTestTreeConnectivity":
                        builder.withEdgeCollection("test1");
                        builder.withEdgeCollection("test2");
                        builder.withEdgeCollection("test3");
                        break;
                    case "shouldRemoveEdgesWithoutConcurrentModificationException":
                        builder.withEdgeCollection("link");
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
                        break;
                    case "shouldAllowRemovalFromEdgeWhenAlreadyRemoved":
                    case "shouldRespectWhatAreEdgesAndWhatArePropertiesInMultiProperties":
                    case "shouldProcessEdges":
                    case "shouldReturnOutThenInOnVertexIterator":
                    case "shouldReturnEmptyIteratorIfNoProperties":
                        builder.withEdgeCollection("knows");
                        break;
                    case "shouldNotHaveAConcurrentModificationExceptionWhenIteratingAndRemovingAddingEdges":
                        builder.withEdgeCollection("knows");
                        builder.withEdgeCollection("pets");
                        builder.withEdgeCollection("walks");
                        builder.withEdgeCollection("livesWith");
                        break;
                    case "shouldHaveStandardStringRepresentation":
                        builder.withEdgeCollection("friends");
                        break;
                    case "shouldReadWriteSelfLoopingEdges":
                        builder.withEdgeCollection("CONTROL");
                        builder.withEdgeCollection("SELFLOOP");
                        break;
                    case "shouldReadGraphML":
                    case "shouldReadGraphMLUnorderedElements":
                    case "shouldTransformGraphMLV2ToV3ViaXSLT":
                    case "shouldReadLegacyGraphSON":
                        builder.withEdgeCollection("knows");
                        builder.withEdgeCollection("created");
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
                        break;
                    case "shouldHaveExceptionConsistencyWhenUsingNullVertex":
                        builder.withEdgeCollection("tonothing");
                        break;
                    case "shouldHandleSelfLoops":
                        builder.withVertexCollection("person");
                        builder.withEdgeCollection("self");
                        break;
                    case "testAttachableCreateMethod":
                    case "shouldAttachWithCreateMethod":
                        builder.withVertexCollection("vertex");
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
                        break;
                    case "shouldCopyFromGraphAToGraphB":
                        builder.withVertexCollection("vertex");
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
                        break;
                    case "g_addV_asXfirstX_repeatXaddEXnextX_toXaddVX_inVX_timesX5X_addEXnextX_toXselectXfirstXX":
                        builder.withEdgeCollection("next");
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
                        builder.withVertexCollection("vertex");
                        builder.configureEdge("edge", "person", "person");
                        break;
                    case "shouldPartitionWithAbstractLambdaChildTraversal":
                        builder.withVertexCollection("testV");
                        builder.withEdgeCollection("self");
                        break;
                }
            }
        }
    }
}
