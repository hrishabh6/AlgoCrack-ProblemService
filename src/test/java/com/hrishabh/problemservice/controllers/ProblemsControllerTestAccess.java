package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.QuestionQuery;

/** Exposes the package-private query builder to tests in other packages. */
public final class ProblemsControllerTestAccess {

    private ProblemsControllerTestAccess() {
    }

    public static QuestionQuery buildQuery(String tags, String tag) {
        return ProblemsController.buildQuery(0, 20, null, tag, tags, null, null, null, null);
    }

    public static QuestionQuery buildExcludeQuery(String excludeDifficulty, String excludeTags) {
        return ProblemsController.buildQuery(0, 20, null, null, null, excludeDifficulty, excludeTags, null, null, null,
                null);
    }
}
