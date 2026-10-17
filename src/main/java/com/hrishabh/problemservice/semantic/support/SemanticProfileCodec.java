package com.hrishabh.problemservice.semantic.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hrishabh.problemservice.complexity.support.ProfileContentHasher;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument;

public final class SemanticProfileCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    private SemanticProfileCodec() {
    }

    public static String canonicalJson(SemanticProfileDocument document) {
        try {
            return MAPPER.writeValueAsString(document);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to serialize semantic profile", ex);
        }
    }

    public static String profileHash(SemanticProfileDocument document) {
        return ProfileContentHasher.sha256Hex(canonicalJson(document));
    }

    public static SemanticProfileDocument parse(String json) throws JsonProcessingException {
        return MAPPER.readValue(json, SemanticProfileDocument.class);
    }
}
