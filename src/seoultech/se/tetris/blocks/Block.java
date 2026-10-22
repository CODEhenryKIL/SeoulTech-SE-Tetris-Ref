package seoultech.se.tetris.blocks;

import java.awt.Color;

public abstract class Block {
		
	protected int[][] shape;
	protected String[][] text;
	protected Color color;
	
	public int getShape(int x, int y) {
		return shape[y][x];
	}

	public String getText(int x, int y) {
		return text[y][x];
	}

	public void setText(int x, int y, String s) {
		text[y][x] = s;
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
		String[][] newText = new String[this.width()][this.height()];
		for (int i = 0; i < this.height(); i++) {
			for (int j = 0; j < this.width(); j++) {
				newText[j][this.height() - i -1] = text[i][j];
			}
		}
		shape = newshape;
		text = newText;
	}
	
	public int height() {
		return shape.length;
	}
	
	public int width() {
		if(shape.length > 0)
			return shape[0].length;
		return 0;
	}

	public Block createNew() {
        try {
            return this.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 예외 발생 시 처리
        }
    }
}
