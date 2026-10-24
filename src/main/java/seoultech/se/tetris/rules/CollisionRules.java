package seoultech.se.tetris.rules;

import seoultech.se.tetris.blocks.Block;

/**
 * Collision and rotation calculations for a nonempty rectangular grid and valid block coordinates.
 * Reads the grid as supplied, including any active block cells, without changing the grid or block.
 */
public final class CollisionRules {
    private CollisionRules() {}

    public static boolean canDown(int[][] grid, Block block, int x, int y) {
        if(y < grid.length - block.height()) {
            for(int j=0; j<block.height(); j++) {
                for(int i=0; i<block.width(); i++) {
                    if ((block.getShape(i, j) + grid[y+j+1][x+i]) > 1) return false;
                }
            }
            return true;
        } else return false;
    }

    public static boolean canRight(int[][] grid, Block block, int x, int y) {
        if(x < grid[0].length - block.width()) {
            for(int j=0; j<block.height(); j++) {
                for(int i=0; i<block.width(); i++) {
                    if ((block.getShape(i, j) + grid[y+j][x+i+1]) > 1) return false;
                }
            }
            return true;
        } else return false;
    }

    public static boolean canLeft(int[][] grid, Block block, int x, int y) {
        if(x > 0) {
            for(int j=0; j<block.height(); j++) {
                for(int i=0; i<block.width(); i++) {
                    if ((block.getShape(i, j) + grid[y+j][x+i-1]) > 1) return false;
                }
            }
            return true;
        } else return false;
    }

    public static int[] rotationPosition(int[][] grid, Block block, int x, int y) {
        int rotatedWidth = block.height();
        int rotatedHeight = block.width();
        int rotatedX = Math.min(x, grid[0].length - rotatedWidth);
        int startY = Math.min(y, grid.length - rotatedHeight);

        for (int rotatedY = startY; rotatedY >= Math.max(0, startY - rotatedHeight); rotatedY--) {
            boolean clear = true;
            for (int row = 0; row < block.height() && clear; row++) {
                for (int col = 0; col < block.width(); col++) {
                    if (block.getShape(col, row) > 0
                            && grid[rotatedY + col][rotatedX + block.height() - row - 1] != 0) {
                        clear = false;
                        break;
                    }
                }
            }
            if (clear) return new int[] {rotatedX, rotatedY};
        }
        return null;
    }
}
