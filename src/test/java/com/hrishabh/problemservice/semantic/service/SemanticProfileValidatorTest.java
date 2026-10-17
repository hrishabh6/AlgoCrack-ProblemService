package com.hrishabh.problemservice.semantic.service;

import com.hrishabh.problemservice.models.Language;
import com.hrishabh.problemservice.models.QuestionMetadata;
import com.hrishabh.problemservice.semantic.catalog.SemanticProfileCatalog;
import com.hrishabh.problemservice.semantic.catalog.SemanticProfileCatalog.Seed;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SizeBinding;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SizeVariable;
import com.hrishabh.problemservice.semantic.model.BindingDimension;
import com.hrishabh.problemservice.semantic.model.SemanticActiveSlot;
import com.hrishabh.problemservice.semantic.model.SemanticProfileStatus;
import com.hrishabh.problemservice.semantic.model.SizeVariableKind;
import com.hrishabh.problemservice.semantic.model.StructureKind;
import com.hrishabh.problemservice.semantic.support.SemanticProfileCodec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticProfileValidatorTest {

    @Test
    void allTenBackfilledProfilesMatchJavaMetadata() {
        assertTrue(SemanticProfileValidator.validateLanguage(seed(1), java(1)).isEmpty());
        assertTrue(SemanticProfileValidator.validateLanguage(seed(2), java(2)).isEmpty());
        assertTrue(SemanticProfileValidator.validateLanguage(seed(3), java(3)).isEmpty());
        assertTrue(SemanticProfileValidator.validateLanguage(seed(4), java(4)).isEmpty());
        assertTrue(SemanticProfileValidator.validateLanguage(seed(5), java(5)).isEmpty());
        assertTrue(SemanticProfileValidator.validateLanguage(seed(6), java(6)).isEmpty());
        assertTrue(SemanticProfileValidator.validateLanguage(seed(7), java(7)).isEmpty());
        assertTrue(SemanticProfileValidator.validateLanguage(seed(8), java(8)).isEmpty());
        assertTrue(SemanticProfileValidator.validateLanguage(seed(9), java(9)).isEmpty());
        assertTrue(SemanticProfileValidator.validateLanguage(seed(10), java(10)).isEmpty());
    }

    @Test
    void unknownSchemaVersionAndEnumFailClosed() {
        SemanticProfileDocument broken = new SemanticProfileDocument(
                "semantic-v2", seed(1).families(), seed(1).entry(), seed(1).sizeVariables(),
                seed(1).structures(), seed(1).mutatedInputs());
        assertFalse(SemanticProfileValidator.validateDocument(broken).isEmpty());
        assertThrows(Exception.class, () -> SemanticProfileCodec.parse(
                SemanticProfileCodec.canonicalJson(seed(1)).replace("MATRIX", "TRIE")));
        assertThrows(Exception.class, () -> SemanticProfileCodec.parse(
                SemanticProfileCodec.canonicalJson(seed(1)).replaceFirst("\\{", "{\"expectedTime\":\"O(n)\",")));
    }

    @Test
    void duplicateVariableNamesAndUnorderedParametersFailClosed() {
        SemanticProfileDocument document = seed(10);
        SemanticProfileDocument duplicated = new SemanticProfileDocument(
                document.schemaVersion(), document.families(), document.entry(),
                List.of(document.sizeVariables().get(0), document.sizeVariables().get(0)),
                document.structures(), document.mutatedInputs());
        assertTrue(SemanticProfileValidator.validateDocument(duplicated).stream()
                .anyMatch(reason -> reason.contains("duplicate")));

        QuestionMetadata unordered = java(10);
        unordered.setParamNames(List.of("nums"));
        assertTrue(SemanticProfileValidator.validateLanguage(document, unordered).stream()
                .anyMatch(reason -> reason.contains("not deterministic")));
    }

    @Test
    void parameterIndexAndStructureMustMatchJavaTypes() {
        SemanticProfileDocument shifted = retarget(seed(10), 5);
        assertTrue(SemanticProfileValidator.validateLanguage(shifted, java(10)).stream()
                .anyMatch(reason -> reason.contains("out of range")));

        SemanticProfileDocument matrixOnArray = new SemanticProfileDocument(
                seed(10).schemaVersion(),
                seed(1).families(),
                seed(10).entry(),
                seed(1).sizeVariables(),
                seed(1).structures(),
                seed(10).mutatedInputs());
        assertTrue(SemanticProfileValidator.validateLanguage(matrixOnArray, java(10)).stream()
                .anyMatch(reason -> reason.contains("incompatible")));
    }

    @Test
    void pythonIncompatibilityDoesNotBlockJavaActivation() {
        QuestionMetadata python = QuestionMetadata.builder()
                .functionName("reverseKGroup")
                .returnType("ListNode")
                .paramTypes(List.of("List", "int"))
                .paramNames(List.of("head", "k"))
                .language(Language.PYTHON)
                .build();
        List<String> pythonFailures = SemanticProfileValidator.validateLanguage(seed(2), python);
        List<String> javaFailures = SemanticProfileValidator.validateLanguage(seed(2), java(2));
        assertFalse(pythonFailures.isEmpty());
        assertTrue(javaFailures.isEmpty());
        assertTrue(SemanticProfileValidator.javaActivationAllowed(javaFailures));
        assertFalse(SemanticProfileValidator.blocksJavaActivation(Language.PYTHON, pythonFailures));
        assertTrue(SemanticProfileValidator.blocksJavaActivation(Language.JAVA, pythonFailures));
    }

    @Test
    void onlyActiveStatusOccupiesTheUniquenessSlot() {
        assertEquals(1, SemanticActiveSlot.occupiedSlot(SemanticProfileStatus.ACTIVE));
        assertEquals(null, SemanticActiveSlot.occupiedSlot(SemanticProfileStatus.DRAFT));
        assertEquals(null, SemanticActiveSlot.occupiedSlot(SemanticProfileStatus.RETIRED));
    }

    @Test
    void benchmarkVariablesForQuestions1And3And10Agree() {
        assertTrue(SemanticProfileValidator.validateBenchmarkAgreement(seed(1), List.of("heightMap"), List.of(
                new SemanticProfileValidator.BenchmarkVariable("n", "heightMap", "rows"),
                new SemanticProfileValidator.BenchmarkVariable("m", "heightMap", "cols"))).isEmpty());
        assertTrue(SemanticProfileValidator.validateBenchmarkAgreement(seed(3), List.of("n", "connections"), List.of(
                new SemanticProfileValidator.BenchmarkVariable("v", "n", null),
                new SemanticProfileValidator.BenchmarkVariable("e", "connections", "edgeCount"))).isEmpty());
        assertTrue(SemanticProfileValidator.validateBenchmarkAgreement(seed(10), List.of("nums", "target"), List.of(
                new SemanticProfileValidator.BenchmarkVariable("n", "nums", "length"))).isEmpty());
        assertFalse(SemanticProfileValidator.validateBenchmarkAgreement(seed(10), List.of("nums", "target"), List.of(
                new SemanticProfileValidator.BenchmarkVariable("n", "target", "length"))).isEmpty());
    }

    @Test
    void codecOperationsUseSeparateSizeBindings() {
        SemanticProfileDocument codec = seed(4);
        SizeVariable serialize = variable(codec, "n");
        SizeVariable deserialize = variable(codec, "s");
        assertEquals("serialize", serialize.binding().operationName());
        assertEquals(BindingDimension.NODE_COUNT, serialize.binding().dimension());
        assertEquals("deserialize", deserialize.binding().operationName());
        assertEquals(BindingDimension.LENGTH, deserialize.binding().dimension());
        assertEquals(StructureKind.STRING, codec.entry().operations().get(2).parameters().get(0).kind());
        SizeVariable reusedTree = new SizeVariable("s", SizeVariableKind.COUNT,
                new SizeBinding(null, "deserialize", 0, BindingDimension.NODE_COUNT));
        SemanticProfileDocument wrong = new SemanticProfileDocument(
                codec.schemaVersion(), codec.families(), codec.entry(), List.of(serialize, reusedTree),
                codec.structures(), codec.mutatedInputs());
        assertFalse(SemanticProfileValidator.validateDocument(wrong).isEmpty());
    }

    @Test
    void mutationMustAgreeWithMetadataTarget() {
        assertTrue(SemanticProfileValidator.validateLanguage(seed(6), java(6)).isEmpty());
        QuestionMetadata wrongTarget = java(6);
        wrongTarget.setMutationTarget("1");
        assertFalse(SemanticProfileValidator.validateLanguage(seed(6), wrongTarget).isEmpty());
        QuestionMetadata unexpected = java(2);
        unexpected.setMutationTarget("0");
        assertFalse(SemanticProfileValidator.validateLanguage(seed(2), unexpected).isEmpty());
    }

    @Test
    void sudokuIsAFixedCharMatrixWithoutSearchComplexity() {
        SemanticProfileDocument sudoku = seed(9);
        assertEquals(StructureKind.CHAR_MATRIX, sudoku.structures().get(0).kind());
        assertEquals(9, sudoku.structures().get(0).constraints().fixedRows());
        assertEquals(9, sudoku.structures().get(0).constraints().fixedColumns());
        assertEquals(1, sudoku.mutatedInputs().size());
        assertTrue(sudoku.sizeVariables().stream().noneMatch(variable ->
                variable.name().toLowerCase().contains("backtrack") || variable.name().toLowerCase().contains("search")));
        assertTrue(SemanticProfileValidator.validateLanguage(sudoku, java(9)).isEmpty());
    }

    @Test
    void canonicalHashIsStable() throws Exception {
        String first = SemanticProfileCodec.profileHash(seed(1));
        String second = SemanticProfileCodec.profileHash(SemanticProfileCodec.parse(
                SemanticProfileCodec.canonicalJson(seed(1))));
        assertEquals(64, first.length());
        assertEquals(first, second);
    }

    private static SemanticProfileDocument seed(long questionId) {
        return SemanticProfileCatalog.seedFor(questionId).document();
    }

    private static SizeVariable variable(SemanticProfileDocument document, String name) {
        return document.sizeVariables().stream().filter(variable -> variable.name().equals(name)).findFirst().orElseThrow();
    }

    private static SemanticProfileDocument retarget(SemanticProfileDocument document, int parameterIndex) {
        SizeVariable variable = document.sizeVariables().get(0);
        return new SemanticProfileDocument(
                document.schemaVersion(),
                document.families(),
                document.entry(),
                List.of(new SizeVariable(variable.name(), variable.kind(),
                        new SizeBinding(parameterIndex, null, null, variable.binding().dimension()))),
                List.of(new SemanticProfileDocument.SemanticStructure(
                        new SizeBinding(parameterIndex, null, null, null),
                        document.structures().get(0).kind(),
                        document.structures().get(0).elementKind(),
                        document.structures().get(0).encoding(),
                        document.structures().get(0).constraints())),
                document.mutatedInputs());
    }

    private static QuestionMetadata java(long questionId) {
        return switch ((int) questionId) {
            case 1 -> meta("trapRainWater", "int", List.of("int[][]"), List.of("heightMap"), null, null);
            case 2 -> meta("reverseKGroup", "ListNode", List.of("ListNode", "int"), List.of("head", "k"), null, null);
            case 3 -> meta("criticalConnections", "List<List<Integer>>",
                    List.of("int", "List<List<Integer>>"), List.of("n", "connections"), null, null);
            case 4 -> meta("Codec", "String", List.of(), List.of(), null, "DESIGN_CLASS");
            case 5 -> meta("wordBreak", "List<String>", List.of("String", "List<String>"), List.of("s", "wordDict"), null, null);
            case 6 -> meta("flatten", "void", List.of("TreeNode"), List.of("root"), "0", null);
            case 7 -> meta("reorderList", "void", List.of("ListNode"), List.of("head"), "0", null);
            case 8 -> meta("cloneGraph", "Node", List.of("Node"), List.of("node"), null, null);
            case 9 -> meta("solveSudoku", "void", List.of("char[][]"), List.of("board"), "0", null);
            case 10 -> meta("fourSum", "List<List<Integer>>", List.of("int[]", "int"), List.of("nums", "target"), null, null);
            default -> throw new IllegalArgumentException(String.valueOf(questionId));
        };
    }

    private static QuestionMetadata meta(
            String function, String returns, List<String> types, List<String> names, String mutation, String questionType) {
        return QuestionMetadata.builder()
                .functionName(function)
                .returnType(returns)
                .paramTypes(types)
                .paramNames(names)
                .language(Language.JAVA)
                .mutationTarget(mutation)
                .questionType(questionType)
                .build();
    }
}
