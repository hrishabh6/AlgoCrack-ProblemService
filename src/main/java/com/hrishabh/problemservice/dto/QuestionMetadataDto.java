package com.hrishabh.problemservice.dto;


import com.hrishabh.algocrackentityservice.models.Language;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionMetadataDto {
    private String functionName;
    private String returnType;
    private List<String> paramTypes;
    private List<String> paramNames;
    private Language language;
    private String codeTemplate;
    private String executionStrategy;
    private Boolean customInputEnabled;
}

