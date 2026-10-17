package com.hrishabh.problemservice.semantic.model;

public final class SemanticActiveSlot {

    private SemanticActiveSlot() {
    }

    /** Matches the generated {@code active_slot} column: only ACTIVE occupies the unique slot. */
    public static Integer occupiedSlot(SemanticProfileStatus status) {
        return status == SemanticProfileStatus.ACTIVE ? 1 : null;
    }
}
