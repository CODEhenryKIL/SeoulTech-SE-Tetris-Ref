package seoultech.se.tetris.blocks;

import java.awt.Color;

public class BombBlock extends ItemBlock {
	
	public BombBlock() {
		shape = new int[][] { 
			{1, 1, 1},
			{1, 1, 1},
			{1, 1, 1}
		};
		text = new String[][] {
			{"O", "O", "O"},
			{"O", "B", "O"},
			{"O", "O", "O"}
		};
		color = new Color(0x964B00);
	}

    @Override 
    public void rotate() {
        return;
    }
}
