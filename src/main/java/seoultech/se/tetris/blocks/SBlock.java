package seoultech.se.tetris.blocks;

import java.awt.Color;

public class SBlock extends Block {

	public SBlock() {
		shape = new int[][] { 
			{0, 1, 1},
			{1, 1, 0}
		};
		text = new String[][] {
			{" ", "O", "O"},
			{"O", "O", " "}
		};
		color = Color.GREEN;
	}

	@Override 
	public Block createNew() {
		return new SBlock();
	}
}
