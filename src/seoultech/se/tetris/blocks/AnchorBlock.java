package seoultech.se.tetris.blocks;

import java.awt.Color;

public class AnchorBlock extends ItemBlock {
	
	public AnchorBlock() {
		shape = new int[][] { 
			{0, 1, 1, 0},
			{1, 1, 1, 1},
			{1, 1, 1, 1}
		};
		text = new String[][]{
			{" ", "O", "O", " "},
			{"O", "O", "O", "O"},
			{"O", "O", "O", "O"}
		};
		color = Color.GRAY;
	}

    @Override 
    public void rotate() {
        return;
    }

	@Override 
	public Block createNew() {
		return new AnchorBlock();
	}
}
