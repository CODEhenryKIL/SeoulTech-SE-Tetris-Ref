package seoultech.se.tetris.blocks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import org.junit.jupiter.api.Test;
import seoultech.se.tetris.Difficulty;

class BlockGeneratorDifficultyTest {
    private static final Class<?>[] BLOCK_TYPES = {
        IBlock.class, JBlock.class, LBlock.class, ZBlock.class,
        SBlock.class, TBlock.class, OBlock.class
    };

    @Test
    void difficultyWeightsMatchTheApprovedDistribution() {
        assertArrayEquals(new int[] {12, 10, 10, 10, 10, 10, 10}, counts(Difficulty.EASY, 72));
        assertArrayEquals(new int[] {1, 1, 1, 1, 1, 1, 1}, counts(Difficulty.NORMAL, 7));
        assertArrayEquals(new int[] {8, 10, 10, 10, 10, 10, 10}, counts(Difficulty.HARD, 68));
    }

    @Test
    void defaultAndNormalKeepTheOriginalSeededSequence() {
        BlockGenerator defaultGenerator = new BlockGenerator(new Random(20261024L));
        BlockGenerator normalGenerator = new BlockGenerator(new Random(20261024L));
        BlockGenerator nullGenerator = new BlockGenerator(new Random(20261024L));

        for (int draw = 0; draw < 100; draw++) {
            Class<?> expected = defaultGenerator.nextBlock().getClass();
            assertEquals(expected, normalGenerator.nextBlock(Difficulty.NORMAL).getClass());
            assertEquals(expected, nullGenerator.nextBlock(null).getClass());
        }
    }

    private static int[] counts(Difficulty difficulty, int bound) {
        int[] counts = new int[BLOCK_TYPES.length];
        for (int ticket = 0; ticket < bound; ticket++) {
            FixedRandom random = new FixedRandom(ticket);
            Block block = new BlockGenerator(random).nextBlock(difficulty);
            assertEquals(bound, random.observedBound);
            counts[indexOf(block.getClass())]++;
        }
        return counts;
    }

    private static int indexOf(Class<?> type) {
        for (int index = 0; index < BLOCK_TYPES.length; index++) {
            if (BLOCK_TYPES[index] == type) return index;
        }
        throw new AssertionError("Unexpected block type: " + type.getName());
    }

    private static final class FixedRandom extends Random {
        private static final long serialVersionUID = 1L;
        private final int ticket;
        private int observedBound;

        FixedRandom(int ticket) {
            this.ticket = ticket;
        }

        @Override
        public int nextInt(int bound) {
            observedBound = bound;
            return ticket;
        }
    }
}
