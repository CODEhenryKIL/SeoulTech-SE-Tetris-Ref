package seoultech.se.tetris.blocks;

import java.io.Serializable;
import java.util.Random;

public final class BlockGenerator implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Random random = new Random();

    public Block nextBlock() {
        int index = random.nextInt(7);
        return switch (index) {
            case 0 -> new IBlock();
            case 1 -> new JBlock();
            case 2 -> new LBlock();
            case 3 -> new ZBlock();
            case 4 -> new SBlock();
            case 5 -> new TBlock();
            case 6 -> new OBlock();
            default -> throw new IllegalStateException("Unexpected block index: " + index);
        };
    }
}
