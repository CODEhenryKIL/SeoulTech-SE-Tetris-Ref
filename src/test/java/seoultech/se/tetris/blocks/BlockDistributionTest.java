package seoultech.se.tetris.blocks;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import seoultech.se.tetris.Difficulty;

class BlockDistributionTest {
    private static final int SAMPLES_PER_DIFFICULTY = 100_000;
    private static final long RANDOM_SEED = 20_261_024L;
    private static final double MAX_RELATIVE_ERROR = 0.05;
    private static final Path REPORT =
        Path.of("build", "reports", "req2", "block-distribution.csv");
    private static final Class<?>[] BLOCK_TYPES = {
        IBlock.class, JBlock.class, LBlock.class, ZBlock.class,
        SBlock.class, TBlock.class, OBlock.class
    };

    @Test
    @DisplayName("R2-09: 실제 생성기의 난이도별 10만 회 분포는 기대 확률의 상대오차 5% 이내")
    void productionGeneratorMatchesEveryConfiguredProbability() throws IOException {
        List<String> report = new ArrayList<>();
        List<Executable> checks = new ArrayList<>();
        report.add("seed,samples,difficulty,block,count,expected_probability,"
            + "observed_probability,relative_error_percent,threshold_percent,pass");

        for (Difficulty difficulty : Difficulty.values()) {
            int[] weights = weights(difficulty);
            int weightSum = java.util.Arrays.stream(weights).sum();
            int[] counts = drawCounts(difficulty);

            for (int index = 0; index < BLOCK_TYPES.length; index++) {
                double expected = (double) weights[index] / weightSum;
                double observed = (double) counts[index] / SAMPLES_PER_DIFFICULTY;
                double relativeError = Math.abs(observed - expected) / expected;
                boolean passed = relativeError <= MAX_RELATIVE_ERROR;
                String context = difficulty.displayName() + " "
                    + BLOCK_TYPES[index].getSimpleName();

                report.add(String.format(Locale.ROOT,
                    "%d,%d,%s,%s,%d,%.8f,%.8f,%.4f,%.2f,%s",
                    RANDOM_SEED, SAMPLES_PER_DIFFICULTY, difficulty.displayName(),
                    BLOCK_TYPES[index].getSimpleName(), counts[index], expected, observed,
                    relativeError * 100.0, MAX_RELATIVE_ERROR * 100.0, passed));
                checks.add(() -> assertTrue(passed,
                    () -> context + " relative error was "
                        + String.format(Locale.ROOT, "%.4f%%", relativeError * 100.0)
                        + ", expected at most 5.0000%"));
            }
        }

        Files.createDirectories(REPORT.getParent());
        Files.write(REPORT, report);
        assertAll(checks);
    }

    private static int[] drawCounts(Difficulty difficulty) {
        BlockGenerator generator = new BlockGenerator(new Random(RANDOM_SEED));
        int[] counts = new int[BLOCK_TYPES.length];
        for (int draw = 0; draw < SAMPLES_PER_DIFFICULTY; draw++) {
            counts[indexOf(generator.nextBlock(difficulty).getClass())]++;
        }
        return counts;
    }

    private static int[] weights(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new int[] {12, 10, 10, 10, 10, 10, 10};
            case NORMAL -> new int[] {1, 1, 1, 1, 1, 1, 1};
            case HARD -> new int[] {8, 10, 10, 10, 10, 10, 10};
        };
    }

    private static int indexOf(Class<?> type) {
        for (int index = 0; index < BLOCK_TYPES.length; index++) {
            if (BLOCK_TYPES[index] == type) return index;
        }
        throw new AssertionError("Unexpected block type: " + type.getName());
    }
}
