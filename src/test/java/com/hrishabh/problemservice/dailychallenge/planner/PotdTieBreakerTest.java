package com.hrishabh.problemservice.dailychallenge.planner;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PotdTieBreakerTest {

    @Test
    void tieBreak_isIndependentOfInputOrder() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        LocalDate date = LocalDate.of(2026, 1, 10);
        String configHash = "abc123";

        record Tie(long id, String seed) {
        }

        List<Tie> ties = new ArrayList<>();
        for (long id : List.of(42L, 7L, 99L)) {
            ties.add(new Tie(id, PotdTieBreaker.deterministicSeed(
                    "potd-planner-v1", configHash, start, end, date, id)));
        }

        ties.sort((a, b) -> PotdTieBreaker.compareAscending(a.seed(), a.id(), b.seed(), b.id()));
        Tie winner = ties.getFirst();

        List<Tie> shuffled = new ArrayList<>(ties);
        Collections.shuffle(shuffled);
        shuffled.sort((a, b) -> PotdTieBreaker.compareAscending(a.seed(), a.id(), b.seed(), b.id()));

        assertThat(shuffled.getFirst().id()).isEqualTo(winner.id());
    }
}
