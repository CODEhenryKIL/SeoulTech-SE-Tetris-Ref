package seoultech.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DifficultyAcceptanceTest {
    private static final int[][] EXPECTED_INTERVALS = {
        {1_000, 827, 669, 528, 408, 307, 226, 163, 115, 79, 53, 35, 23, 14, 9, 5, 3, 2, 1},
        {1_000, 793, 618, 473, 355, 262, 190, 135, 94, 64, 43, 28, 18, 11, 7, 4, 3, 1, 1},
        {1_000, 761, 574, 428, 315, 228, 163, 115, 79, 54, 36, 24, 15, 10, 6, 4, 2, 1, 1}
    };

    @Test
    @DisplayName("R2-09: 1~19레벨의 세 난이도 간격과 속도 순서는 승인된 표와 일치")
    void everyLevelMatchesTheAcceptedIntervalsAndOrdering() {
        for (Difficulty difficulty : Difficulty.values()) {
            int previous = Integer.MAX_VALUE;
            for (int level = 1; level <= 19; level++) {
                int actual = difficulty.dropIntervalMillis(level);
                assertEquals(EXPECTED_INTERVALS[difficulty.ordinal()][level - 1], actual,
                    difficulty + " level " + level);
                assertTrue(actual <= previous,
                    difficulty + " must not slow down at level " + level);
                previous = actual;
            }
        }

        for (int level = 1; level <= 19; level++) {
            int easy = Difficulty.EASY.dropIntervalMillis(level);
            int normal = Difficulty.NORMAL.dropIntervalMillis(level);
            int hard = Difficulty.HARD.dropIntervalMillis(level);
            assertTrue(easy >= normal, "Easy must not be faster than Normal at level " + level);
            assertTrue(normal >= hard, "Hard must not be slower than Normal at level " + level);
        }
    }

    @Test
    @DisplayName("R2-09: Normal은 기존 레벨별 타이머 공식을 유지")
    void normalPreservesTheExistingFormulaThroughLevelNineteen() {
        for (int level = 1; level <= 19; level++) {
            int legacyInterval = (int) Math.round(
                Math.pow(0.8 - (level - 1) * 0.007, level - 1) * 1_000);
            assertEquals(legacyInterval, Difficulty.NORMAL.dropIntervalMillis(level),
                "Normal level " + level);
        }
    }

    @Test
    @DisplayName("R2-09: 초기값·레벨 범위·최소 타이머 간격 경계를 유지")
    void levelBoundsClampAndIntervalsNeverDropBelowOneMillisecond() {
        for (Difficulty difficulty : Difficulty.values()) {
            assertEquals(1_000, difficulty.dropIntervalMillis(1));
            assertEquals(difficulty.dropIntervalMillis(1), difficulty.dropIntervalMillis(0));
            assertEquals(difficulty.dropIntervalMillis(1), difficulty.dropIntervalMillis(-100));
            assertEquals(difficulty.dropIntervalMillis(19), difficulty.dropIntervalMillis(20));
            assertEquals(difficulty.dropIntervalMillis(19),
                difficulty.dropIntervalMillis(Integer.MAX_VALUE));
            assertTrue(difficulty.dropIntervalMillis(Integer.MAX_VALUE) >= 1);
        }
    }
}
