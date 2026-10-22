package seoultech.se.tetris.blocks;

import java.util.Random;

public class ColumnBlock extends ItemBlock {
    
    public ColumnBlock() {
        Random random = new Random();
        int block = random.nextInt(7);
        Block newBlock;
        switch(block) {
            case 0:
                newBlock = new IBlock();
                break;
            case 1:
                newBlock = new JBlock();
                break;
            case 2:
                newBlock = new LBlock();
                break;
            case 3:
                newBlock = new ZBlock();
                break;
            case 4:
                newBlock = new SBlock();
                break;
            case 5:
                newBlock = new TBlock();
                break;
            case 6:
                newBlock = new OBlock();
                break;
            default:
                newBlock = new LBlock();
                break;
        }
        int index = random.nextInt(4);
        for (int i = 0; i < newBlock.height(); i++) {
            for (int j = 0; j < newBlock.width(); j++) {
                if ((i*newBlock.width())+j == index) {
                    newBlock.setText(j, i, "C");
                }
            }
        }
        this.text = newBlock.text;
        this.shape = newBlock.shape;
        this.color = newBlock.color;
    }
}
