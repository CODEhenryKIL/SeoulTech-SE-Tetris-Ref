package seoultech.se.tetris.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import seoultech.se.tetris.Difficulty;
import seoultech.se.tetris.blocks.OBlock;
import seoultech.se.tetris.blocks.RowBlock;

/** R2-11 acceptance boundaries not already covered by the BUG-03 regression suite. */
class LineItemAcceptanceTest {
    private final String originalDataDir = System.getProperty("tetris.dataDir");
    private final String originalPalette = Maincontainer.colorPalette;

    @TempDir
    Path temporary;

    @AfterEach
    void restoreGlobalSettings() {
        if (originalDataDir == null) System.clearProperty("tetris.dataDir");
        else System.setProperty("tetris.dataDir", originalDataDir);
        Maincontainer.colorPalette = originalPalette;
    }

    @Test
    @Timeout(10)
    @DisplayName("R2-11: L은 미완성 최상단·최하단 행도 삭제")
    void lineItemClearsIncompleteTopAndBottomRows() throws Exception {
        System.setProperty("tetris.dataDir", temporary.toString());
        ExistingFeatureFixture.onEdt(() -> {
            RowBlock topItem = rowItem(0);
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                int[][] grid = new int[20][10];
                grid[0][9] = 1;
                fixture.prepare(topItem, new OBlock(), grid, 3, 0);

                assertTrue(fixture.board.useItem());

                assertTrue(Arrays.stream(board(fixture)[0]).allMatch(cell -> cell == 0));
                assertEquals(100, fixture.board.getScore());
                assertEquals(1, intField(fixture.board, "erasedLineNum"));
            }

            RowBlock bottomItem = rowItem(3);
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                int[][] grid = new int[20][10];
                grid[18][9] = 1;
                grid[19][0] = 1;
                fixture.prepare(bottomItem, new OBlock(), grid, 3, 19);

                assertTrue(fixture.board.useItem());

                assertEquals(1, board(fixture)[19][9], "윗행은 삭제된 최하단 행으로 내려와야 합니다.");
                assertEquals(0, board(fixture)[19][0], "삭제한 미완성 행의 칸은 남으면 안 됩니다.");
                assertEquals(100, fixture.board.getScore());
                assertEquals(1, intField(fixture.board, "erasedLineNum"));
            }
            return null;
        });
    }

    @Test
    @Timeout(10)
    @DisplayName("R2-11: L행과 비연속 완성 행을 한 번에 압축하고 문자 좌표도 이동")
    void nonAdjacentRowsCompressGridAndTextTogether() throws Exception {
        System.setProperty("tetris.dataDir", temporary.toString());
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                int[][] grid = new int[20][10];
                grid[2][0] = 1;
                Arrays.fill(grid[3], 1);
                Arrays.fill(grid[10], 1);
                grid[19][0] = 1;
                fixture.prepare(rowItem(2), new OBlock(), grid, 3, 19);
                text(fixture, 2, 0, "S", Color.MAGENTA);

                assertTrue(fixture.board.useItem());

                assertEquals(600, fixture.board.getScore(), "세 행 콤보는 100+200+300점이어야 합니다.");
                assertEquals(3, intField(fixture.board, "erasedLineNum"));
                assertEquals(1, board(fixture)[5][0], "세 삭제 행 아래로 원래 2행이 이동해야 합니다.");
                assertEquals("S", text(fixture, 5, 0).text,
                    "격자와 화면 문자는 같은 목적지 행으로 이동해야 합니다.");
                assertTrue(Arrays.stream(board(fixture)[0]).allMatch(cell -> cell == 0));
                assertTrue(Arrays.stream(board(fixture)[1]).allMatch(cell -> cell == 0));
                assertTrue(Arrays.stream(board(fixture)[2]).allMatch(cell -> cell == 0));
            }
            return null;
        });
    }

    @Test
    @Timeout(10)
    @DisplayName("R2-11: 모든 팔레트에서 L 문자와 일반 삭제 행 강조를 화면으로 구분")
    void markerAndClearHighlightRemainVisibleInEveryPalette() throws Exception {
        System.setProperty("tetris.dataDir", temporary.toString());
        for (String palette : new String[] {"Normal", "Protanopia", "Deuteranopia", "Tritanopia"}) {
            Maincontainer.colorPalette = palette;
            ExistingFeatureFixture.onEdt(() -> {
                RowBlock item = rowItem(1);
                try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                    fixture.prepare(item, new OBlock(), new int[20][10], 3, 5);
                    BufferedImage marker = fixture.render(palette + "-L.png");
                    assertTrue(fixture.contrastingPixelsInCell(marker,
                        3 + item.getItemX() + 1, 5 + item.getItemY() + 1) > 4,
                        palette + " 팔레트에서 L 문자가 보이지 않습니다.");
                }

                try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                    int[][] grid = new int[20][10];
                    Arrays.fill(grid[10], 1);
                    fixture.prepare(new OBlock(), new OBlock(), grid, 3, 0);
                    fixture.board.eraseLine();
                    BitSet highlighted = ExistingFeatureFixture.get(
                        fixture.board, "clearingRows", BitSet.class);
                    assertTrue(highlighted.get(10), palette + " 팔레트가 삭제 행을 강조하지 않았습니다.");
                    BufferedImage withHighlight = fixture.render(palette + "-clear.png");
                    fixture.board.stopAnimations();
                    BufferedImage withoutHighlight = fixture.render(null);
                    assertTrue(differentPixels(withHighlight, withoutHighlight) > 500,
                        palette + " 팔레트에서 삭제 행 강조가 실제 화면과 구분되지 않습니다.");
                }
                return null;
            });
        }
    }

    private static RowBlock rowItem(int attachment) throws Exception {
        Constructor<RowBlock> constructor = RowBlock.class.getDeclaredConstructor(Random.class);
        constructor.setAccessible(true);
        return constructor.newInstance(new SequenceRandom(0, attachment));
    }

    private static int[][] board(ExistingFeatureFixture fixture) throws Exception {
        return ExistingFeatureFixture.get(fixture.board, "board", int[][].class);
    }

    @SuppressWarnings("unchecked")
    private static Board.TextChunk text(ExistingFeatureFixture fixture, int row, int column) throws Exception {
        ArrayList<Board.TextChunk> chunks = ExistingFeatureFixture.get(
            fixture.board, "textChunks", ArrayList.class);
        return chunks.get((row + 1) * 13 + column + 1);
    }

    @SuppressWarnings("unchecked")
    private static void text(ExistingFeatureFixture fixture, int row, int column,
            String value, Color color) throws Exception {
        ArrayList<Board.TextChunk> chunks = ExistingFeatureFixture.get(
            fixture.board, "textChunks", ArrayList.class);
        chunks.set((row + 1) * 13 + column + 1, fixture.board.new TextChunk(value, color));
    }

    private static int intField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(target);
    }

    private static int differentPixels(BufferedImage first, BufferedImage second) {
        int count = 0;
        for (int y = 0; y < first.getHeight(); y++) {
            for (int x = 0; x < first.getWidth(); x++) {
                if (first.getRGB(x, y) != second.getRGB(x, y)) count++;
            }
        }
        return count;
    }

    private static final class SequenceRandom extends Random {
        private static final long serialVersionUID = 1L;
        private final int[] values;
        private int index;

        SequenceRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            int value = values[index++];
            if (value < 0 || value >= bound) throw new AssertionError("value outside bound: " + value);
            return value;
        }
    }
}
