package com.hrishabh.problemservice.semantic.catalog;

import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.MutatedInput;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.OperationParameter;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SemanticEntry;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SemanticOperation;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SemanticStructure;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SizeBinding;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SizeVariable;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.StructureConstraints;
import com.hrishabh.problemservice.semantic.model.BindingDimension;
import com.hrishabh.problemservice.semantic.model.ElementKind;
import com.hrishabh.problemservice.semantic.model.EntryKind;
import com.hrishabh.problemservice.semantic.model.OperationResultKind;
import com.hrishabh.problemservice.semantic.model.OperationRole;
import com.hrishabh.problemservice.semantic.model.SemanticEncoding;
import com.hrishabh.problemservice.semantic.model.SemanticFamily;
import com.hrishabh.problemservice.semantic.model.SizeVariableKind;
import com.hrishabh.problemservice.semantic.model.StructureKind;

import java.util.List;

/**
 * Approved V1 documents for the ten seeded problems. Java is the activation target.
 * These documents describe input shape only; they do not store expected complexity.
 */
public final class SemanticProfileCatalog {

    private SemanticProfileCatalog() {
    }

    public record Seed(long questionId, String profileId, String semanticVersion, SemanticProfileDocument document) {
    }

    public static List<Seed> seeds() {
        return List.of(
                seed(1, matrix()),
                seed(2, reverseKGroup()),
                seed(3, criticalConnections()),
                seed(4, codec()),
                seed(5, wordBreak()),
                seed(6, flatten()),
                seed(7, reorder()),
                seed(8, cloneGraph()),
                seed(9, sudoku()),
                seed(10, fourSum()));
    }

    public static Seed seedFor(long questionId) {
        return seeds().stream()
                .filter(seed -> seed.questionId() == questionId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no semantic seed for question " + questionId));
    }

    private static Seed seed(long questionId, SemanticProfileDocument document) {
        return new Seed(questionId, "spp-q" + questionId + "-v1", "v1", document);
    }

    private static SemanticProfileDocument matrix() {
        return function(
                List.of(SemanticFamily.MATRIX),
                List.of(
                        dimension("n", SizeVariableKind.DIMENSION, 0, BindingDimension.ROWS),
                        dimension("m", SizeVariableKind.DIMENSION, 0, BindingDimension.COLUMNS)),
                List.of(structure(0, StructureKind.MATRIX, ElementKind.INT, SemanticEncoding.JSON_NESTED_ARRAY,
                        new StructureConstraints(true, null, null, null, null, null, null))),
                List.of());
    }

    private static SemanticProfileDocument reverseKGroup() {
        return function(
                List.of(SemanticFamily.LINKED_LIST),
                List.of(
                        dimension("n", SizeVariableKind.COUNT, 0, BindingDimension.NODE_COUNT),
                        dimension("k", SizeVariableKind.COUNT, 1, BindingDimension.SCALAR)),
                List.of(structure(0, StructureKind.LIST_NODE, null, SemanticEncoding.JSON_ARRAY,
                        new StructureConstraints(null, true, null, null, null, null, null))),
                List.of());
    }

    private static SemanticProfileDocument criticalConnections() {
        return function(
                List.of(SemanticFamily.GRAPH),
                List.of(
                        dimension("v", SizeVariableKind.COUNT, 0, BindingDimension.SCALAR),
                        dimension("e", SizeVariableKind.COUNT, 1, BindingDimension.EDGE_COUNT)),
                List.of(new SemanticStructure(
                        new SizeBinding(1, null, null, null),
                        StructureKind.EDGE_LIST,
                        ElementKind.INT,
                        SemanticEncoding.EDGE_PAIRS,
                        new StructureConstraints(null, null, true, true, null, null, null))),
                List.of());
    }

    private static SemanticProfileDocument codec() {
        SemanticEntry entry = new SemanticEntry(
                EntryKind.DESIGN_CLASS,
                "Codec",
                SemanticEncoding.OPERATION_TRACE,
                List.of(
                        new SemanticOperation("Codec", OperationRole.CONSTRUCTOR, List.of(), null),
                        new SemanticOperation(
                                "serialize",
                                OperationRole.METHOD,
                                List.of(new OperationParameter(StructureKind.TREE_NODE, SemanticEncoding.LEVEL_ORDER)),
                                OperationResultKind.STRING),
                        new SemanticOperation(
                                "deserialize",
                                OperationRole.METHOD,
                                List.of(new OperationParameter(StructureKind.STRING, null)),
                                OperationResultKind.TREE_NODE)));
        return new SemanticProfileDocument(
                SemanticProfileDocument.SCHEMA_V1,
                List.of(SemanticFamily.BINARY_TREE, SemanticFamily.DESIGN_CLASS, SemanticFamily.MULTI_ENTRY),
                entry,
                List.of(
                        new SizeVariable("n", SizeVariableKind.COUNT,
                                new SizeBinding(null, "serialize", 0, BindingDimension.NODE_COUNT)),
                        new SizeVariable("s", SizeVariableKind.LENGTH,
                                new SizeBinding(null, "deserialize", 0, BindingDimension.LENGTH))),
                List.of(),
                List.of());
    }

    private static SemanticProfileDocument wordBreak() {
        return function(
                List.of(SemanticFamily.STRING, SemanticFamily.ARRAY),
                List.of(
                        dimension("n", SizeVariableKind.LENGTH, 0, BindingDimension.LENGTH),
                        dimension("d", SizeVariableKind.COUNT, 1, BindingDimension.LENGTH),
                        dimension("L", SizeVariableKind.AGGREGATE_LENGTH, 1, BindingDimension.LENGTH)),
                List.of(
                        structure(0, StructureKind.STRING, ElementKind.CHAR, null, null),
                        structure(1, StructureKind.ARRAY, ElementKind.STRING, SemanticEncoding.JSON_ARRAY, null)),
                List.of());
    }

    private static SemanticProfileDocument flatten() {
        return function(
                List.of(SemanticFamily.BINARY_TREE, SemanticFamily.MUTATED_INPUT),
                List.of(dimension("n", SizeVariableKind.COUNT, 0, BindingDimension.NODE_COUNT)),
                List.of(structure(0, StructureKind.TREE_NODE, null, SemanticEncoding.LEVEL_ORDER, null)),
                List.of(new MutatedInput(0, StructureKind.TREE_NODE)));
    }

    private static SemanticProfileDocument reorder() {
        return function(
                List.of(SemanticFamily.LINKED_LIST, SemanticFamily.MUTATED_INPUT),
                List.of(dimension("n", SizeVariableKind.COUNT, 0, BindingDimension.NODE_COUNT)),
                List.of(structure(0, StructureKind.LIST_NODE, null, SemanticEncoding.JSON_ARRAY,
                        new StructureConstraints(null, true, null, null, null, null, null))),
                List.of(new MutatedInput(0, StructureKind.LIST_NODE)));
    }

    private static SemanticProfileDocument cloneGraph() {
        return function(
                List.of(SemanticFamily.GRAPH),
                List.of(
                        dimension("v", SizeVariableKind.COUNT, 0, BindingDimension.NODE_COUNT),
                        dimension("e", SizeVariableKind.COUNT, 0, BindingDimension.EDGE_COUNT)),
                List.of(structure(0, StructureKind.GRAPH_NODE, null, SemanticEncoding.ADJACENCY_LIST,
                        new StructureConstraints(null, null, null, null, true, null, null))),
                List.of());
    }

    private static SemanticProfileDocument sudoku() {
        return function(
                List.of(SemanticFamily.MATRIX, SemanticFamily.MUTATED_INPUT),
                List.of(
                        dimension("r", SizeVariableKind.DIMENSION, 0, BindingDimension.ROWS),
                        dimension("c", SizeVariableKind.DIMENSION, 0, BindingDimension.COLUMNS)),
                List.of(structure(0, StructureKind.CHAR_MATRIX, ElementKind.CHAR, SemanticEncoding.JSON_NESTED_ARRAY,
                        new StructureConstraints(true, null, null, null, null, 9, 9))),
                List.of(new MutatedInput(0, StructureKind.CHAR_MATRIX)));
    }

    private static SemanticProfileDocument fourSum() {
        return function(
                List.of(SemanticFamily.ARRAY),
                List.of(dimension("n", SizeVariableKind.LENGTH, 0, BindingDimension.LENGTH)),
                List.of(structure(0, StructureKind.ARRAY, ElementKind.INT, SemanticEncoding.JSON_ARRAY, null)),
                List.of());
    }

    private static SemanticProfileDocument function(
            List<SemanticFamily> families,
            List<SizeVariable> variables,
            List<SemanticStructure> structures,
            List<MutatedInput> mutated) {
        return new SemanticProfileDocument(
                SemanticProfileDocument.SCHEMA_V1,
                families,
                new SemanticEntry(EntryKind.FUNCTION, null, null, null),
                variables,
                structures,
                mutated);
    }

    private static SizeVariable dimension(String name, SizeVariableKind kind, int parameterIndex, BindingDimension dimension) {
        return new SizeVariable(name, kind, new SizeBinding(parameterIndex, null, null, dimension));
    }

    private static SemanticStructure structure(
            int parameterIndex,
            StructureKind kind,
            ElementKind elementKind,
            SemanticEncoding encoding,
            StructureConstraints constraints) {
        return new SemanticStructure(new SizeBinding(parameterIndex, null, null, null), kind, elementKind, encoding, constraints);
    }
}
