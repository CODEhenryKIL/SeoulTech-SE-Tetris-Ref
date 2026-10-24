package seoultech.se.tetris.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import org.junit.jupiter.api.Test;

class RowBlockTest {

    @Test
    void itemMarkerRemainsUniqueAndTracksItsOccupiedCellAcrossAllShapesAndRotations() {
        for (int shape = 0; shape < 7; shape++) {
            for (int attachment = 0; attachment < 4; attachment++) {
                RowBlock block = new RowBlock(new SequenceRandom(shape, attachment));

                for (int rotation = 0; rotation < 4; rotation++) {
                    assertMarkerInvariant(block, shape, attachment, rotation);
                    block.rotate();
                }
            }
        }
    }

    private static void assertMarkerInvariant(
            RowBlock block, int shape, int attachment, int rotation) {
        int markerCount = 0;
        int markerX = -1;
        int markerY = -1;

        for (int y = 0; y < block.height(); y++) {
            for (int x = 0; x < block.width(); x++) {
                if ("L".equals(block.getText(x, y))) {
                    markerCount++;
                    markerX = x;
                    markerY = y;
                    assertEquals(1, block.getShape(x, y), context(shape, attachment, rotation));
                }
            }
        }

        String context = context(shape, attachment, rotation);
        assertEquals(1, markerCount, context);
        assertEquals(markerX, block.getItemX(), context);
        assertEquals(markerY, block.getItemY(), context);
    }

    private static String context(int shape, int attachment, int rotation) {
        return "shape=" + shape + ", attachment=" + attachment + ", rotation=" + rotation;
    }

    private static final class SequenceRandom extends Random {
        private static final long serialVersionUID = 1L;
        private final int[] values;
        private int index;

        SequenceRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            int value = values[index++];
            if (value < 0 || value >= bound) {
                throw new AssertionError("Scripted value " + value + " is outside bound " + bound);
            }
            return value;
        }
    }
}
