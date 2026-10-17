package com.hrishabh.problemservice.semantic.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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

@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SemanticProfileDocument(
        String schemaVersion,
        List<SemanticFamily> families,
        SemanticEntry entry,
        List<SizeVariable> sizeVariables,
        List<SemanticStructure> structures,
        List<MutatedInput> mutatedInputs) {

    public static final String SCHEMA_V1 = "semantic-v1";

    @JsonIgnoreProperties(ignoreUnknown = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SemanticEntry(
            EntryKind kind,
            String className,
            SemanticEncoding inputEncoding,
            List<SemanticOperation> operations) {
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SemanticOperation(
            String name,
            OperationRole role,
            List<OperationParameter> parameters,
            OperationResultKind returns) {
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OperationParameter(
            StructureKind kind,
            SemanticEncoding encoding) {
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SizeVariable(
            String name,
            SizeVariableKind kind,
            SizeBinding binding) {
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SizeBinding(
            Integer parameterIndex,
            String operationName,
            Integer operationParameterIndex,
            BindingDimension dimension) {
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SemanticStructure(
            SizeBinding binding,
            StructureKind kind,
            ElementKind elementKind,
            SemanticEncoding encoding,
            StructureConstraints constraints) {
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StructureConstraints(
            Boolean rectangular,
            Boolean singlyLinked,
            Boolean undirected,
            Boolean zeroBasedVertexLabels,
            Boolean oneIndexedEntryNode,
            Integer fixedRows,
            Integer fixedColumns) {
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MutatedInput(
            int parameterIndex,
            StructureKind structureKind) {
    }
}
