package seoultech.se.tetris.blocks;

import java.awt.Color;

public class TBlock extends Block {
	
	public TBlock() {
		shape = new int[][] { 
			{0, 1, 0},
			{1, 1, 1}
		};
		text = new String[][] {
			{" ", "O", " "},
			{"O", "O", "O"}
		};
		color = Color.MAGENTA;
	}

	@Override 
	public Block createNew() {
		return new TBlock();
	}
}
