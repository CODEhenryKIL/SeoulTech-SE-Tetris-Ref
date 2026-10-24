package seoultech.se.tetris.blocks;

import java.util.Random;

public class RowBlock extends ItemBlock {

    private int itemX;
    private int itemY;
    
    public RowBlock() {
        Random random = new Random();
        int block = random.nextInt(7);
        Block newBlock;
        switch (block) {
            case 0 -> newBlock = new IBlock();
            case 1 -> newBlock = new JBlock();
            case 2 -> newBlock = new LBlock();
            case 3 -> newBlock = new ZBlock();
            case 4 -> newBlock = new SBlock();
            case 5 -> newBlock = new TBlock();
            case 6 -> newBlock = new OBlock();
            default -> newBlock = new IBlock();
        };
        int index = random.nextInt(4);
        int cnt = -1;
        for (int i = 0; i < newBlock.height(); i++) {
            for (int j = 0; j < newBlock.width(); j++) {
                if (newBlock.getShape(j,i) == 1) {cnt++;}
                if (cnt == index) {
                    newBlock.setText(j, i, "L");
                    itemX = j; itemY = i;
                }
            }
        }
        this.text = newBlock.text;
        this.shape = newBlock.shape;
        this.color = newBlock.color;
    }

    @Override 
    public void rotate() {
        int tmpY = itemX;
        int tmpX = this.height() - itemY - 1;
        this.itemX = tmpX; this.itemY = tmpY;
        super.rotate();
    }

    @Override 
    public Block createNew() {
        return new RowBlock();
    }

    public int getItemX() {return itemX;}
    public int getItemY() {return itemY;}
}
