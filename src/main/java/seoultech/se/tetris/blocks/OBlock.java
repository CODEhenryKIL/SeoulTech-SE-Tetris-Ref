package seoultech.se.tetris.blocks;

import java.awt.Color;

public class OBlock extends Block {

	public OBlock() {
		shape = new int[][] { 
			{1, 1}, 
			{1, 1}
		};
		text = new String[][]{
			{"O", "O"},
			{"O", "O"}
		};
		color = Color.YELLOW;
	}

	@Override 
	public Block createNew() {
		return new OBlock();
	}
}
