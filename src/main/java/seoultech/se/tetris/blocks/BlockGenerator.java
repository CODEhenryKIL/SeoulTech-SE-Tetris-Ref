package seoultech.se.tetris.blocks;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import seoultech.se.tetris.Difficulty;

public final class BlockGenerator implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final List<Integer> EASY_WEIGHTS = List.of(12, 10, 10, 10, 10, 10, 10);
    private static final List<Integer> HARD_WEIGHTS = List.of(8, 10, 10, 10, 10, 10, 10);
    private final Random random;

    public BlockGenerator() {
        this(new Random());
    }

    BlockGenerator(Random random) {
        this.random = random;
    }

    public Block nextBlock() {
        return nextBlock(Difficulty.NORMAL);
    }

    public Block nextBlock(Difficulty difficulty) {
        Difficulty selected = difficulty == null ? Difficulty.NORMAL : difficulty;
        int index;
        if (selected == Difficulty.NORMAL) {
            // Preserve the original seeded sequence as well as the equal distribution.
            index = random.nextInt(7);
            return createBlock(index);
        }
        List<Integer> weights = switch (selected) {
            case EASY -> EASY_WEIGHTS;
            case HARD -> HARD_WEIGHTS;
            case NORMAL -> throw new IllegalStateException("NORMAL is handled above");
        };
        int ticket = random.nextInt(weights.stream().mapToInt(Integer::intValue).sum());
        index = 0;
        while (ticket >= weights.get(index)) {
            ticket -= weights.get(index++);
        }
        return createBlock(index);
    }

    public Block nextItemBlock() {
        return switch (random.nextInt(5)) {
            case 0 -> new AnchorBlock();
            case 1 -> new BombBlock();
            case 2 -> new RowBlock();
            case 3 -> new ColumnBlock();
            case 4 -> new CrossBlock();
            default -> throw new IllegalStateException("Unexpected item block index");
        };
    }

    private static Block createBlock(int index) {
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
