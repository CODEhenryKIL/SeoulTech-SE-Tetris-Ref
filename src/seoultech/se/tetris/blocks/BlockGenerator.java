package seoultech.se.tetris.blocks;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public final class BlockGenerator implements Serializable {
    private static final long serialVersionUID = 1L;
    private Block[] blockList = {new IBlock(), new JBlock(), new LBlock(), new ZBlock(), new SBlock(), new TBlock(), new OBlock()};
	private Block[] itemList = {new AnchorBlock(), new BombBlock(), new RowBlock(), new ColumnBlock()};
	private static final LinkedHashMap<String, int[]> rouletteWheelSelection = new LinkedHashMap<>(Map.of(
		"Easy", 	new int[]{72, 12, 10, 10, 10, 10, 10, 10}, // 첫 번째 숫자는 모든 숫자의 합
		"Normal", 	new int[]{70, 10, 10, 10, 10, 10, 10, 10},
		"Hard", 	new int[]{68, 8, 10, 10, 10, 10, 10, 10},
		"Item", 	new int[]{40, 10, 10, 10, 10}
	));

    private final Random random = new Random();

    public Block nextBlock(String difficulty) {
        int[] rouletteList = rouletteWheelSelection.get(difficulty);
		int sum = rouletteList[0];
		int block = random.nextInt(sum); block++;
		for (int i = 1; i < rouletteList.length; i++) {
			int num = rouletteList[i];
			if (num >= block) {
				try {
					return blockList[i-1].createNew();
				} catch (Exception e) {e.printStackTrace();}
			} 
			else {block -= num;}
		}
		return null;
    }

    public Block nextItemBlock() {
        int[] itemRouletteList = rouletteWheelSelection.get("Item");
		int itemSum = itemRouletteList[0];
		int block = random.nextInt(itemSum); block++;
		for (int i = 1; i < itemRouletteList.length; i++) {
			int num = itemRouletteList[i];
			if (num >= block) {
				try {
					return itemList[i-1].createNew();
				} catch (Exception e) {e.printStackTrace();}
			} 
			else {block -= num;}
		}
		return null;
    }
}
