package seoultech.se.tetris.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import seoultech.se.tetris.Difficulty;
import seoultech.se.tetris.blocks.Block;
import seoultech.se.tetris.blocks.BombBlock;
import seoultech.se.tetris.blocks.ColumnBlock;
import seoultech.se.tetris.blocks.OBlock;

/** R2-13 acceptance boundaries for the original column and bomb items. */
class ColumnBombAcceptanceTest {
    private final String originalDataDir = System.getProperty("tetris.dataDir");
    private String originalPalette;

    @TempDir
    Path temporary;

    @BeforeEach
    void isolatePersistentSettings() {
        originalPalette = Maincontainer.colorPalette;
        System.setProperty("tetris.dataDir", temporary.toString());
    }

    @AfterEach
    void restoreGlobalSettings() {
        Maincontainer.colorPalette = originalPalette;
        if (originalDataDir == null) System.clearProperty("tetris.dataDir");
        else System.setProperty("tetris.dataDir", originalDataDir);
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-13: C가 있던 원래 열 전체만 격자·문자에서 삭제하고 점수와 줄 수는 유지")
    void columnClearsOnlyItsOriginalMarkedColumn() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                ColumnBlock item = columnBlock(0, 0);
                int effectColumn = 3 + item.getItemX();
                int outsideColumn = effectColumn + 1;
                int[][] grid = new int[20][10];
                grid[2][effectColumn] = 1;
                grid[17][effectColumn] = 1;
                grid[2][outsideColumn] = 1;
                fixture.prepare(item, new OBlock(), grid, 3, 10);
                setText(fixture, 2, effectColumn, "T", Color.CYAN);
                setText(fixture, 2, outsideColumn, "S", Color.MAGENTA);

                assertTrue(fixture.board.useItem());

                int[][] result = board(fixture);
                for (int row = 0; row < 20; row++) {
                    assertEquals(0, result[row][effectColumn], "C 효과 열에 블록이 남았습니다: " + row);
                }
                assertEquals(1, result[2][outsideColumn]);
                assertEquals(" ", text(fixture, 2, effectColumn).text);
                assertEquals("S", text(fixture, 2, outsideColumn).text);
                assertEquals(0, fixture.board.getScore());
                assertEquals(0, intField(fixture.board, "erasedLineNum"));
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-13: C 블록 하드드롭은 실제 낙하 점수만 주고 다음 블록으로 한 번 전환")
    void columnHardDropAppliesEffectAndAdvancesOnce() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                ColumnBlock item = columnBlock(0, 0);
                OBlock queued = new OBlock();
                int effectColumn = 3 + item.getItemX();
                fixture.prepare(item, queued, new int[20][10], 3, 0);

                fixture.board.hardDrop();

                int[][] result = board(fixture);
                assertEquals(19, fixture.board.getScore());
                assertEquals(0, intField(fixture.board, "erasedLineNum"));
                assertEquals(0, result[19][effectColumn]);
                assertSame(queued, ExistingFeatureFixture.get(fixture.board, "curr", Block.class));
                assertEquals(3, fixture.board.x);
                assertEquals(0, fixture.board.y);
            }
            return null;
        });
    }

    @Test
    @DisplayName("R2-13: B 아이템은 회전하지 않는 3x4 형상에 정확히 열 칸을 점유")
    void bombHasSpecifiedUnrotatedShape() {
        BombBlock item = new BombBlock();

        assertEquals(3, item.width());
        assertEquals(4, item.height());
        assertEquals(10, occupiedCells(item));
        item.rotate();
        assertEquals(3, item.width());
        assertEquals(4, item.height());
        assertEquals(10, occupiedCells(item));
    }

    @ParameterizedTest(name = "x={0}, y={1}")
    @CsvSource({
        "0, 0, 5, 4, 6, 6",
        "7, 16, 14, 5, 13, 4"
    })
    @Timeout(15)
    @DisplayName("R2-13: B 효과는 모서리에서 형상 경계 밖 두 칸까지 잘라내고 바깥 격자·문자는 보존")
    void bombClearsClippedTwoCellMarginAtCorners(int x, int y, int insideRow,
            int insideColumn, int outsideRow, int outsideColumn) throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                int[][] grid = new int[20][10];
                grid[insideRow][insideColumn] = 1;
                grid[outsideRow][outsideColumn] = 1;
                fixture.prepare(new BombBlock(), new OBlock(), grid, x, y);
                setText(fixture, insideRow, insideColumn, "T", Color.ORANGE);
                setText(fixture, outsideRow, outsideColumn, "S", Color.MAGENTA);

                assertTrue(fixture.board.useItem());

                assertEquals(0, board(fixture)[insideRow][insideColumn]);
                assertEquals(1, board(fixture)[outsideRow][outsideColumn]);
                assertEquals(" ", text(fixture, insideRow, insideColumn).text);
                assertEquals("S", text(fixture, outsideRow, outsideColumn).text);
                assertEquals(0, fixture.board.getScore());
                assertEquals(0, intField(fixture.board, "erasedLineNum"));
            }
            return null;
        });
    }

    @ParameterizedTest(name = "supportRow={0}")
    @CsvSource({
        "-1, 16, 14, 19",
        "10, 6, 4, 11"
    })
    @Timeout(15)
    @DisplayName("R2-13: B 블록은 바닥·쌓인 블록에서 효과를 한 번 적용하고 다음 블록으로 전환")
    void bombHardDropAppliesOneEffectAndAdvancesOnce(int supportRow, int expectedScore,
            int effectStartRow, int effectEndRow) throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                OBlock queued = new OBlock();
                int[][] grid = new int[20][10];
                grid[0][0] = 1;
                if (supportRow >= 0) grid[supportRow][4] = 1;
                fixture.prepare(new BombBlock(), queued, grid, 3, 0);
                setText(fixture, 0, 0, "S", Color.MAGENTA);

                fixture.board.hardDrop();

                int[][] result = board(fixture);
                assertEquals(expectedScore, fixture.board.getScore());
                assertEquals(0, intField(fixture.board, "erasedLineNum"));
                assertEquals(1, result[0][0]);
                assertEquals("S", text(fixture, 0, 0).text);
                for (int row = effectStartRow; row <= effectEndRow; row++) {
                    for (int column = 1; column < 8; column++) {
                        assertEquals(0, result[row][column]);
                    }
                }
                assertSame(queued, ExistingFeatureFixture.get(fixture.board, "curr", Block.class));
                assertEquals(3, fixture.board.x);
                assertEquals(0, fixture.board.y);
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-13: B 범위 밖 완성 행은 일반 100점·1줄만 반영하고 정상 압축")
    void bombHardDropScoresOnlyOrdinaryCompletedRowsOutsideItsEffect() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                OBlock queued = new OBlock();
                int[][] grid = new int[20][10];
                Arrays.fill(grid[10], 1);
                grid[1][0] = 1;
                fixture.prepare(new BombBlock(), queued, grid, 3, 11);
                setText(fixture, 1, 0, "S", Color.MAGENTA);

                fixture.board.hardDrop();

                assertEquals(105, fixture.board.getScore(),
                    "5칸 낙하와 범위 밖 일반 한 줄의 100점만 더해야 합니다.");
                assertEquals(1, intField(fixture.board, "erasedLineNum"));
                assertEquals(1, board(fixture)[2][0], "완성 행 위 셀은 한 행 아래로 압축되어야 합니다.");
                assertEquals("S", text(fixture, 2, 0).text);
                assertSame(queued, ExistingFeatureFixture.get(fixture.board, "curr", Block.class));
                assertEquals(3, fixture.board.x);
                assertEquals(0, fixture.board.y);
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-13: 회전 불가 B 블록은 벽·바닥에서도 반복 회전 입력으로 위치가 바뀌지 않음")
    void bombRotationInputDoesNotKickAtWallOrFloor() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                fixture.prepare(new BombBlock(), new OBlock(), new int[20][10], 7, 16);

                for (int attempt = 0; attempt < 4; attempt++) {
                    fixture.board.rotate();
                    assertEquals(7, fixture.board.x, "회전 불가 블록이 벽에서 좌우 이동했습니다.");
                    assertEquals(16, fixture.board.y, "회전 불가 블록이 바닥에서 상하 이동했습니다.");
                }
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-13: 모든 팔레트에서 C와 B 마커를 실제 화면으로 식별")
    void itemMarkersRemainVisibleInEveryPalette() throws Exception {
        for (String palette : List.of("Normal", "Protanopia", "Deuteranopia", "Tritanopia")) {
            Maincontainer.colorPalette = palette;
            ExistingFeatureFixture.onEdt(() -> {
                try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                    ColumnBlock column = columnBlock(0, 0);
                    fixture.prepare(column, new OBlock(), new int[20][10], 3, 5);
                    BufferedImage columnImage = fixture.render(
                        palette.toLowerCase() + "-palette-column.png");
                    assertEquals("C", column.getText(column.getItemX(), column.getItemY()));
                    assertTrue(fixture.contrastingPixelsInCell(columnImage,
                        3 + column.getItemX() + 1, 5 + column.getItemY() + 1) > 4,
                        palette + " 팔레트에서 C 마커가 보이지 않습니다.");

                    BombBlock bomb = new BombBlock();
                    fixture.prepare(bomb, new OBlock(), new int[20][10], 3, 5);
                    BufferedImage bombImage = fixture.render(
                        palette.toLowerCase() + "-palette-bomb.png");
                    assertEquals("B", bomb.getText(1, 2));
                    assertTrue(fixture.contrastingPixelsInCell(bombImage, 5, 8) > 4,
                        palette + " 팔레트에서 B 마커가 보이지 않습니다.");
                }
                return null;
            });
        }
    }

    private static ColumnBlock columnBlock(int type, int attachment) throws Exception {
        Constructor<ColumnBlock> constructor = ColumnBlock.class.getDeclaredConstructor(Random.class);
        constructor.setAccessible(true);
        return constructor.newInstance(new SequenceRandom(type, attachment));
    }

    private static int occupiedCells(Block block) {
        int occupied = 0;
        for (int row = 0; row < block.height(); row++) {
            for (int column = 0; column < block.width(); column++) {
                if (block.getShape(column, row) != 0) occupied++;
            }
        }
        return occupied;
    }

    private static int[][] board(ExistingFeatureFixture fixture) throws Exception {
        return ExistingFeatureFixture.get(fixture.board, "board", int[][].class);
    }

    @SuppressWarnings("unchecked")
    private static Board.TextChunk text(ExistingFeatureFixture fixture, int row, int column)
            throws Exception {
        ArrayList<Board.TextChunk> chunks = ExistingFeatureFixture.get(
            fixture.board, "textChunks", ArrayList.class);
        return chunks.get((row + 1) * 13 + column + 1);
    }

    @SuppressWarnings("unchecked")
    private static void setText(ExistingFeatureFixture fixture, int row, int column,
            String value, Color color) throws Exception {
        ArrayList<Board.TextChunk> chunks = ExistingFeatureFixture.get(
            fixture.board, "textChunks", ArrayList.class);
        chunks.set((row + 1) * 13 + column + 1, fixture.board.new TextChunk(value, color));
    }

    private static int intField(Object target, String name) throws Exception {
        return ExistingFeatureFixture.get(target, name, Integer.class);
    }

    private static final class SequenceRandom extends Random {
        private final int[] values;
        private int index;

        private SequenceRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            return Math.floorMod(values[index++], bound);
        }
    }
}
