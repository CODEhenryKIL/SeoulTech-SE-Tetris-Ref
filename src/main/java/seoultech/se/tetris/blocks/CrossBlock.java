package seoultech.se.tetris.blocks;

import java.awt.Color;

public class CrossBlock extends ItemBlock {
    private static final Color ITEM_COLOR = new Color(0xB04CFF);

    public CrossBlock() {
        shape = new int[][] {
            {1, 1},
            {1, 1}
        };
        text = new String[][] {
            {"P", "P"},
            {"P", "P"}
        };
        color = ITEM_COLOR;
    }

    @Override
    public void rotate() {
        // The item is an O-shaped block, so rotation does not change its state.
    }

    @Override
    public Block createNew() {
        return new CrossBlock();
    }
}
