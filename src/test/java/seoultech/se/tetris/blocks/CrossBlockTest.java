package seoultech.se.tetris.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class CrossBlockTest {

    @Test
    void everyCellIsOccupiedAndMarkedAcrossAllRotations() {
        CrossBlock block = new CrossBlock();

        for (int rotation = 0; rotation < 4; rotation++) {
            assertEquals(2, block.width());
            assertEquals(2, block.height());
            assertEquals(new Color(0xB04CFF), block.getColor());
            for (int y = 0; y < block.height(); y++) {
                for (int x = 0; x < block.width(); x++) {
                    assertEquals(1, block.getShape(x, y));
                    assertEquals("P", block.getText(x, y));
                }
            }
            block.rotate();
        }
    }

    @Test
    void createNewReturnsAnIndependentBlock() {
        CrossBlock original = new CrossBlock();
        CrossBlock copy = (CrossBlock) original.createNew();

        assertNotSame(original, copy);
        original.setText(0, 0, "changed");

        assertEquals("changed", original.getText(0, 0));
        assertEquals("P", copy.getText(0, 0));
    }
}
