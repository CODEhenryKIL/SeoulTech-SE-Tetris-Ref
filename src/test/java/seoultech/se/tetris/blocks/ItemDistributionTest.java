package seoultech.se.tetris.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ItemDistributionTest {
    private static final long SEED = 20_261_025L;
    private static final int SAMPLES = 100_000;
    private static final double EXPECTED_PROBABILITY = 0.20;
    private static final double MAX_RELATIVE_ERROR = 0.05;
    private static final Path REPORT =
            Path.of("build", "reports", "req2", "item-distribution.csv");

    @Test
    void everyItemIsSuppliedAtTwentyPercentWithinFivePercentRelativeError() throws IOException {
        Map<Class<? extends Block>, ItemObservation> observations = expectedItems();
        BlockGenerator generator = new BlockGenerator(new Random(SEED));

        for (int draw = 0; draw < SAMPLES; draw++) {
            Block block = generator.nextItemBlock();
            ItemObservation observation = observations.get(block.getClass());
            assertTrue(observation != null, "Unexpected item class: " + block.getClass().getName());
            assertTrue(hasOccupiedLabel(block, observation.label()),
                    () -> block.getClass().getSimpleName() + " does not expose label "
                            + observation.label());
            observation.increment();
        }

        writeReport(observations);

        assertEquals(5, observations.size());
        assertEquals(5, observations.values().stream().map(ItemObservation::label).distinct().count());
        for (Map.Entry<Class<? extends Block>, ItemObservation> entry : observations.entrySet()) {
            double relativeError = relativeError(entry.getValue().count());
            assertTrue(relativeError <= MAX_RELATIVE_ERROR,
                    () -> entry.getKey().getSimpleName() + " relative error was " + relativeError);
        }
    }

    private static Map<Class<? extends Block>, ItemObservation> expectedItems() {
        Map<Class<? extends Block>, ItemObservation> observations = new LinkedHashMap<>();
        observations.put(AnchorBlock.class, new ItemObservation("O"));
        observations.put(BombBlock.class, new ItemObservation("B"));
        observations.put(RowBlock.class, new ItemObservation("L"));
        observations.put(ColumnBlock.class, new ItemObservation("C"));
        observations.put(CrossBlock.class, new ItemObservation("P"));
        return observations;
    }

    private static boolean hasOccupiedLabel(Block block, String label) {
        for (int y = 0; y < block.height(); y++) {
            for (int x = 0; x < block.width(); x++) {
                if (block.getShape(x, y) == 1 && label.equals(block.getText(x, y))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double relativeError(int count) {
        double observedProbability = count / (double) SAMPLES;
        return Math.abs(observedProbability - EXPECTED_PROBABILITY) / EXPECTED_PROBABILITY;
    }

    private static void writeReport(Map<Class<? extends Block>, ItemObservation> observations)
            throws IOException {
        StringBuilder csv = new StringBuilder(
                "seed,samples,item,label,count,expected,observed,relative_error,pass\n");
        for (Map.Entry<Class<? extends Block>, ItemObservation> entry : observations.entrySet()) {
            ItemObservation observation = entry.getValue();
            double observedProbability = observation.count() / (double) SAMPLES;
            double relativeError = relativeError(observation.count());
            csv.append(SEED).append(',')
                    .append(SAMPLES).append(',')
                    .append(entry.getKey().getSimpleName()).append(',')
                    .append(observation.label()).append(',')
                    .append(observation.count()).append(',')
                    .append(EXPECTED_PROBABILITY).append(',')
                    .append(observedProbability).append(',')
                    .append(relativeError).append(',')
                    .append(relativeError <= MAX_RELATIVE_ERROR).append('\n');
        }
        Files.createDirectories(REPORT.getParent());
        Files.writeString(REPORT, csv, StandardCharsets.UTF_8);
    }

    private static final class ItemObservation {
        private final String label;
        private int count;

        ItemObservation(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }

        int count() {
            return count;
        }

        void increment() {
            count++;
        }
    }
}
