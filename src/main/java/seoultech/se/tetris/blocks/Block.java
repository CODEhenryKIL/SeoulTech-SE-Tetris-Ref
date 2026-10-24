package seoultech.se.tetris.blocks;

import java.awt.Color;

public abstract class Block {
		
	protected int[][] shape;
	protected Color color;
	
	public int getShape(int x, int y) {
		return shape[y][x];
	}
	
	public Color getColor() {
		return color;
	}
	
	public void rotate() {
		//Rotate the block 90 deg. clockwise.
		int[][] newshape = new int[this.width()][this.height()];
		for (int i = 0; i < this.height(); i++) {
			for (int j = 0; j < this.width(); j++) {
				newshape[j][this.height() - i -1] = shape[i][j];
			}
		}
		shape = newshape;
	}
	
	public int height() {
		return shape.length;
	}
	
	public int width() {
		if(shape.length > 0)
			return shape[0].length;
		return 0;
	}
}
