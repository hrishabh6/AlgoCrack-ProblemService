package com.hrishabh.problemservice.semantic.service;

import com.hrishabh.problemservice.models.Language;
import com.hrishabh.problemservice.models.QuestionMetadata;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.MutatedInput;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.OperationParameter;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SemanticEntry;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SemanticOperation;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SemanticStructure;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SizeBinding;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument.SizeVariable;
import com.hrishabh.problemservice.semantic.model.BindingDimension;
import com.hrishabh.problemservice.semantic.model.EntryKind;
import com.hrishabh.problemservice.semantic.model.OperationRole;
import com.hrishabh.problemservice.semantic.model.SemanticEncoding;
import com.hrishabh.problemservice.semantic.model.SemanticFamily;
import com.hrishabh.problemservice.semantic.model.SizeVariableKind;
import com.hrishabh.problemservice.semantic.model.StructureKind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fail-closed checks for a V1 semantic document against one language's execution metadata.
 * Java incompatibility blocks activation. Other languages are reported and do not.
 */
public final class SemanticProfileValidator {

    private SemanticProfileValidator() {
    }

    public record BenchmarkVariable(String name, String parameter, String dimension) {
    }

    public static List<String> validateDocument(SemanticProfileDocument document) {
        List<String> failures = new ArrayList<>();
        if (document == null) {
            failures.add("profile document is missing");
            return failures;
        }
        if (!SemanticProfileDocument.SCHEMA_V1.equals(document.schemaVersion())) {
            failures.add("unsupported schemaVersion: " + document.schemaVersion());
        }
        if (document.families() == null || document.families().isEmpty()) {
            failures.add("families are required");
        }
        if (document.entry() == null || document.entry().kind() == null) {
            failures.add("entry.kind is required");
            return failures;
        }
        if (document.sizeVariables() == null) {
            failures.add("sizeVariables are required");
        } else {
            validateVariableNames(document.sizeVariables(), failures);
        }
        if (document.structures() == null) {
            failures.add("structures are required");
        }
        if (document.mutatedInputs() == null) {
            failures.add("mutatedInputs are required");
        }
        if (!failures.isEmpty()) {
            return failures;
        }
        boolean design = document.entry().kind() == EntryKind.DESIGN_CLASS;
        if (design) {
            validateDesignEntry(document, failures);
        } else if (document.families().contains(SemanticFamily.DESIGN_CLASS)) {
            failures.add("DESIGN_CLASS family requires a DESIGN_CLASS entry");
        }
        boolean mutated = !document.mutatedInputs().isEmpty();
        if (mutated != document.families().contains(SemanticFamily.MUTATED_INPUT)) {
            failures.add("MUTATED_INPUT family must match mutatedInputs");
        }
        validateStructures(document, failures);
        validateBindings(document, failures);
        validateSudokuShape(document, failures);
        return failures;
    }

    public static List<String> validateLanguage(SemanticProfileDocument document, QuestionMetadata metadata) {
        List<String> failures = new ArrayList<>(validateDocument(document));
        if (!failures.isEmpty() || metadata == null) {
            if (metadata == null) {
                failures.add("language metadata is missing");
            }
            return failures;
        }
        if (metadata.getParamNames() == null || metadata.getParamTypes() == null
                || metadata.getParamNames().size() != metadata.getParamTypes().size()) {
            failures.add("parameter order is not deterministic");
            return failures;
        }
        if (document.entry().kind() == EntryKind.DESIGN_CLASS) {
            validateDesignMetadata(document, metadata, failures);
            return failures;
        }
        if ("DESIGN_CLASS".equalsIgnoreCase(metadata.getQuestionType())) {
            failures.add("function profile does not match DESIGN_CLASS metadata");
        }
        int arity = metadata.getParamTypes().size();
        for (SemanticStructure structure : document.structures()) {
            Integer index = structure.binding() == null ? null : structure.binding().parameterIndex();
            if (index == null || index < 0 || index >= arity) {
                failures.add("structure parameter index out of range: " + index);
                continue;
            }
            if (!typeCompatible(structure.kind(), metadata.getParamTypes().get(index))) {
                failures.add("structure " + structure.kind() + " is incompatible with Java type "
                        + metadata.getParamTypes().get(index) + " at index " + index);
            }
        }
        for (SizeVariable variable : document.sizeVariables()) {
            SizeBinding binding = variable.binding();
            if (binding == null || binding.parameterIndex() == null) {
                failures.add("function size variable " + variable.name() + " is missing a parameter index");
                continue;
            }
            if (binding.parameterIndex() < 0 || binding.parameterIndex() >= arity) {
                failures.add("size variable " + variable.name() + " parameter index out of range");
                continue;
            }
            String type = metadata.getParamTypes().get(binding.parameterIndex());
            if (binding.dimension() == BindingDimension.SCALAR && !isScalar(type)) {
                failures.add("scalar variable " + variable.name() + " is bound to non-scalar type " + type);
            }
        }
        validateMutation(document, metadata, failures);
        return failures;
    }

    /**
     * Java must be compatible. A non-Java incompatibility does not block activation.
     */
    public static boolean javaActivationAllowed(List<String> javaFailures) {
        return javaFailures.isEmpty();
    }

    public static List<String> validateBenchmarkAgreement(
            SemanticProfileDocument document,
            List<String> javaParameterNames,
            List<BenchmarkVariable> benchmarkVariables) {
        List<String> failures = new ArrayList<>();
        if (benchmarkVariables == null || benchmarkVariables.isEmpty()) {
            return failures;
        }
        for (BenchmarkVariable benchmark : benchmarkVariables) {
            SizeVariable match = document.sizeVariables().stream()
                    .filter(variable -> variable.name().equals(benchmark.name()))
                    .findFirst()
                    .orElse(null);
            if (match == null || match.binding() == null || match.binding().parameterIndex() == null) {
                failures.add("benchmark variable " + benchmark.name() + " has no semantic binding");
                continue;
            }
            int index = match.binding().parameterIndex();
            if (javaParameterNames == null || index < 0 || index >= javaParameterNames.size()) {
                failures.add("benchmark variable " + benchmark.name() + " parameter index is not in Java metadata");
                continue;
            }
            if (!benchmark.parameter().equals(javaParameterNames.get(index))) {
                failures.add("benchmark variable " + benchmark.name() + " parameter does not match semantic binding");
            }
            if (benchmark.dimension() != null && !dimensionsAgree(benchmark.dimension(), match.binding().dimension())) {
                failures.add("benchmark variable " + benchmark.name() + " dimension does not match semantic binding");
            }
        }
        return failures;
    }

    private static void validateVariableNames(List<SizeVariable> variables, List<String> failures) {
        Set<String> names = new HashSet<>();
        for (SizeVariable variable : variables) {
            if (variable.name() == null || variable.name().isBlank()) {
                failures.add("size variable name is blank");
                continue;
            }
            if (!names.add(variable.name())) {
                failures.add("duplicate size variable: " + variable.name());
            }
            if (variable.kind() == null || variable.binding() == null || variable.binding().dimension() == null) {
                failures.add("size variable " + variable.name() + " is incomplete");
            }
        }
    }

    private static void validateDesignEntry(SemanticProfileDocument document, List<String> failures) {
        SemanticEntry entry = document.entry();
        if (!document.families().contains(SemanticFamily.DESIGN_CLASS)
                || !document.families().contains(SemanticFamily.MULTI_ENTRY)) {
            failures.add("design-class profile requires DESIGN_CLASS and MULTI_ENTRY");
        }
        if (entry.className() == null || entry.className().isBlank()) {
            failures.add("design-class className is required");
        }
        if (entry.inputEncoding() != SemanticEncoding.OPERATION_TRACE) {
            failures.add("design-class inputEncoding must be OPERATION_TRACE");
        }
        if (entry.operations() == null || entry.operations().isEmpty()) {
            failures.add("design-class operations are required");
            return;
        }
        long constructors = entry.operations().stream().filter(op -> op.role() == OperationRole.CONSTRUCTOR).count();
        if (constructors != 1) {
            failures.add("design-class profile requires exactly one constructor");
        }
        Set<String> signatures = new HashSet<>();
        for (SemanticOperation operation : entry.operations()) {
            if (operation.name() == null || operation.name().isBlank() || operation.role() == null) {
                failures.add("design-class operation is incomplete");
                continue;
            }
            int arity = operation.parameters() == null ? 0 : operation.parameters().size();
            if (!signatures.add(operation.name() + "#" + arity)) {
                failures.add("duplicate operation signature: " + operation.name());
            }
            if (operation.role() == OperationRole.METHOD && operation.returns() == null) {
                failures.add("method " + operation.name() + " is missing a return kind");
            }
        }
    }

    private static void validateStructures(SemanticProfileDocument document, List<String> failures) {
        Set<Integer> seen = new HashSet<>();
        for (SemanticStructure structure : document.structures()) {
            if (structure.kind() == null || structure.binding() == null || structure.binding().parameterIndex() == null) {
                failures.add("structure binding is incomplete");
                continue;
            }
            if (!seen.add(structure.binding().parameterIndex())) {
                failures.add("duplicate structure binding for parameter " + structure.binding().parameterIndex());
            }
            if (!encodingCompatible(structure.kind(), structure.encoding())) {
                failures.add("encoding " + structure.encoding() + " is incompatible with " + structure.kind());
            }
            if (structure.constraints() != null && structure.constraints().fixedRows() != null
                    && structure.kind() != StructureKind.CHAR_MATRIX && structure.kind() != StructureKind.MATRIX) {
                failures.add("fixed dimensions require a matrix structure");
            }
        }
    }

    private static void validateBindings(SemanticProfileDocument document, List<String> failures) {
        for (SizeVariable variable : document.sizeVariables()) {
            SizeBinding binding = variable.binding();
            if (binding == null) {
                continue;
            }
            boolean functionBinding = binding.parameterIndex() != null;
            boolean operationBinding = binding.operationName() != null || binding.operationParameterIndex() != null;
            if (functionBinding == operationBinding) {
                failures.add("size variable " + variable.name() + " must bind to exactly one input");
                continue;
            }
            if (document.entry().kind() == EntryKind.DESIGN_CLASS && functionBinding) {
                failures.add("design-class variable " + variable.name() + " must bind to an operation parameter");
            }
            if (document.entry().kind() == EntryKind.FUNCTION && operationBinding) {
                failures.add("function variable " + variable.name() + " must bind to a parameter index");
            }
            if (operationBinding) {
                validateOperationBinding(document, variable, failures);
            } else {
                SemanticStructure structure = structureAt(document, binding.parameterIndex());
                if (structure != null && !dimensionFits(binding.dimension(), structure.kind(), variable.kind())) {
                    failures.add("dimension " + binding.dimension() + " does not fit " + structure.kind()
                            + " for " + variable.name());
                }
                if (structure == null && binding.dimension() != BindingDimension.SCALAR) {
                    failures.add("variable " + variable.name() + " has no structure for dimension " + binding.dimension());
                }
            }
        }
    }

    private static void validateOperationBinding(
            SemanticProfileDocument document,
            SizeVariable variable,
            List<String> failures) {
        SizeBinding binding = variable.binding();
        SemanticOperation operation = document.entry().operations() == null ? null : document.entry().operations().stream()
                .filter(candidate -> candidate.name().equals(binding.operationName()))
                .findFirst()
                .orElse(null);
        if (operation == null || binding.operationParameterIndex() == null
                || operation.parameters() == null
                || binding.operationParameterIndex() < 0
                || binding.operationParameterIndex() >= operation.parameters().size()) {
            failures.add("operation binding for " + variable.name() + " is not in the operation contract");
            return;
        }
        OperationParameter parameter = operation.parameters().get(binding.operationParameterIndex());
        if (!dimensionFits(binding.dimension(), parameter.kind(), variable.kind())) {
            failures.add("operation parameter kind " + parameter.kind() + " does not fit " + variable.name());
        }
    }

    private static void validateSudokuShape(SemanticProfileDocument document, List<String> failures) {
        for (SemanticStructure structure : document.structures()) {
            if (structure.kind() != StructureKind.CHAR_MATRIX) {
                continue;
            }
            if (structure.constraints() == null
                    || structure.constraints().fixedRows() == null
                    || structure.constraints().fixedColumns() == null) {
                failures.add("CHAR_MATRIX requires an explicit fixed shape");
            }
        }
        for (SizeVariable variable : document.sizeVariables()) {
            if (isSearchComplexityName(variable.name())) {
                failures.add("semantic profile must not encode search complexity as " + variable.name());
            }
        }
    }

    private static boolean isSearchComplexityName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.contains("backtrack") || normalized.contains("search") || normalized.equals("complexity");
    }

    private static void validateDesignMetadata(
            SemanticProfileDocument document,
            QuestionMetadata metadata,
            List<String> failures) {
        if (!"DESIGN_CLASS".equalsIgnoreCase(metadata.getQuestionType())) {
            failures.add("metadata questionType is not DESIGN_CLASS");
        }
        if (document.entry().className() == null || !document.entry().className().equals(metadata.getFunctionName())) {
            failures.add("metadata class name does not match semantic className");
        }
    }

    private static void validateMutation(
            SemanticProfileDocument document,
            QuestionMetadata metadata,
            List<String> failures) {
        Integer target = parseMutationTarget(metadata.getMutationTarget(), failures);
        if (target == null && (metadata.getMutationTarget() == null || metadata.getMutationTarget().isBlank())) {
            if (!document.mutatedInputs().isEmpty()) {
                failures.add("semantic mutatedInputs disagree with absent mutationTarget");
            }
            return;
        }
        if (target == null) {
            return;
        }
        boolean matched = false;
        for (MutatedInput mutated : document.mutatedInputs()) {
            if (mutated.parameterIndex() == target) {
                matched = true;
            } else {
                failures.add("mutated input " + mutated.parameterIndex() + " is not mutationTarget " + target);
            }
        }
        if (!matched) {
            failures.add("mutationTarget " + target + " is missing from mutatedInputs");
        }
    }

    private static Integer parseMutationTarget(String raw, List<String> failures) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(raw.trim());
        } catch (NumberFormatException ex) {
            failures.add("mutationTarget is not an index: " + raw);
            return null;
        }
    }

    private static SemanticStructure structureAt(SemanticProfileDocument document, int parameterIndex) {
        for (SemanticStructure structure : document.structures()) {
            if (structure.binding() != null && structure.binding().parameterIndex() != null
                    && structure.binding().parameterIndex() == parameterIndex) {
                return structure;
            }
        }
        return null;
    }

    static boolean dimensionFits(BindingDimension dimension, StructureKind kind, SizeVariableKind variableKind) {
        if (dimension == null || kind == null) {
            return false;
        }
        return switch (dimension) {
            case ROWS, COLUMNS -> (kind == StructureKind.MATRIX || kind == StructureKind.CHAR_MATRIX)
                    && variableKind == SizeVariableKind.DIMENSION;
            case LENGTH -> (kind == StructureKind.STRING && variableKind == SizeVariableKind.LENGTH)
                    || (kind == StructureKind.ARRAY && (variableKind == SizeVariableKind.LENGTH
                    || variableKind == SizeVariableKind.COUNT
                    || variableKind == SizeVariableKind.AGGREGATE_LENGTH));
            case NODE_COUNT -> (kind == StructureKind.LIST_NODE || kind == StructureKind.TREE_NODE
                    || kind == StructureKind.GRAPH_NODE) && variableKind == SizeVariableKind.COUNT;
            case EDGE_COUNT -> (kind == StructureKind.EDGE_LIST || kind == StructureKind.GRAPH_NODE)
                    && variableKind == SizeVariableKind.COUNT;
            case SCALAR -> false;
        };
    }

    static boolean typeCompatible(StructureKind kind, String javaType) {
        if (javaType == null) {
            return false;
        }
        String type = javaType.replace(" ", "");
        return switch (kind) {
            case MATRIX -> "int[][]".equals(type);
            case CHAR_MATRIX -> "char[][]".equals(type);
            case STRING -> "String".equals(type);
            case LIST_NODE -> "ListNode".equals(type);
            case TREE_NODE -> "TreeNode".equals(type);
            case GRAPH_NODE -> "Node".equals(type);
            case ARRAY -> "int[]".equals(type) || "List<String>".equals(type);
            case EDGE_LIST -> "List<List<Integer>>".equals(type);
        };
    }

    private static boolean isScalar(String javaType) {
        if (javaType == null) {
            return false;
        }
        String type = javaType.replace(" ", "");
        return "int".equals(type) || "long".equals(type) || "Integer".equals(type) || "Long".equals(type);
    }

    private static boolean encodingCompatible(StructureKind kind, SemanticEncoding encoding) {
        if (encoding == null) {
            return kind == StructureKind.STRING;
        }
        return switch (kind) {
            case ARRAY -> encoding == SemanticEncoding.JSON_ARRAY;
            case MATRIX, CHAR_MATRIX -> encoding == SemanticEncoding.JSON_NESTED_ARRAY;
            case LIST_NODE -> encoding == SemanticEncoding.JSON_ARRAY;
            case TREE_NODE -> encoding == SemanticEncoding.LEVEL_ORDER;
            case GRAPH_NODE -> encoding == SemanticEncoding.ADJACENCY_LIST;
            case EDGE_LIST -> encoding == SemanticEncoding.EDGE_PAIRS;
            case STRING -> false;
        };
    }

    private static boolean dimensionsAgree(String benchmarkDimension, BindingDimension semantic) {
        String normalized = benchmarkDimension.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "rows" -> semantic == BindingDimension.ROWS;
            case "cols", "columns" -> semantic == BindingDimension.COLUMNS;
            case "length" -> semantic == BindingDimension.LENGTH;
            case "edgecount" -> semantic == BindingDimension.EDGE_COUNT;
            default -> false;
        };
    }

    public static boolean blocksJavaActivation(Language language, List<String> failures) {
        return language == Language.JAVA && !failures.isEmpty();
    }
}
