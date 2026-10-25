package seoultech.se.tetris.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import seoultech.se.tetris.Difficulty;
import seoultech.se.tetris.blocks.Block;
import seoultech.se.tetris.blocks.CrossBlock;
import seoultech.se.tetris.blocks.OBlock;

/** R2-13 acceptance boundaries for the two-row/two-column item. */
class CrossItemAcceptanceTest {
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

    @ParameterizedTest(name = "x={0}")
    @ValueSource(ints = {0, 8})
    @Timeout(15)
    @DisplayName("R2-13: 좌우 모서리에서 원래 두 행·두 열만 삭제하고 격자와 문자를 함께 압축")
    void clearsOriginalRowsAndColumnsAtBothCorners(int itemX) throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                int outsideColumn = itemX == 0 ? 2 : 7;
                int[][] grid = new int[20][10];
                grid[5][itemX] = 1;
                grid[5][outsideColumn] = 1;
                fixture.prepare(new CrossBlock(), new OBlock(), grid, itemX, 18);
                setText(fixture, 5, outsideColumn, "S", Color.MAGENTA);

                assertTrue(fixture.board.useItem());

                int[][] result = board(fixture);
                assertEquals(0, result[7][itemX], "원래 효과 열의 블록은 행 압축 전에 삭제되어야 합니다.");
                assertEquals(1, result[7][outsideColumn], "효과 밖 블록은 두 행만큼 내려와야 합니다.");
                assertEquals("S", text(fixture, 7, outsideColumn).text,
                    "격자와 화면 문자는 같은 목적지 행으로 이동해야 합니다.");
                assertEquals(300, fixture.board.getScore());
                assertEquals(2, intField(fixture.board, "erasedLineNum"));
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-13: 최상단 고정에서도 원래 0·1행을 삭제하고 아래 행의 높이는 유지")
    void clearsTopRowsWithoutMovingTheRemainingHeight() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                int[][] grid = new int[20][10];
                grid[2][3] = 1;
                grid[2][2] = 1;
                fixture.prepare(new CrossBlock(), new OBlock(), grid, 3, 0);

                assertTrue(fixture.board.useItem());

                int[][] result = board(fixture);
                assertTrue(Arrays.stream(result[0]).allMatch(cell -> cell == 0));
                assertTrue(Arrays.stream(result[1]).allMatch(cell -> cell == 0));
                assertEquals(0, result[2][3], "원래 효과 열의 블록은 삭제되어야 합니다.");
                assertEquals(1, result[2][2], "최상단 두 행 삭제는 나머지 행의 높이를 바꾸지 않습니다.");
                assertEquals(300, fixture.board.getScore());
                assertEquals(2, intField(fixture.board, "erasedLineNum"));
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-13: 쌓인 블록 위 고정 시 실제 낙하 점수와 효과 점수를 합산하고 다음 블록으로 한 번 전환")
    void hardDropOntoStackPreservesDropPointsAndAdvancesOnce() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                OBlock queued = new OBlock();
                int[][] grid = new int[20][10];
                grid[5][4] = 1;
                grid[10][3] = 1;
                fixture.prepare(new CrossBlock(), queued, grid, 4, 0);

                fixture.board.hardDrop();

                int[][] result = board(fixture);
                assertEquals(303, fixture.board.getScore(), "3칸 낙하와 두 행 삭제 점수를 모두 보존해야 합니다.");
                assertEquals(2, intField(fixture.board, "erasedLineNum"));
                assertEquals(0, result[5][4], "원래 효과 열에 있던 받침 블록은 삭제되어야 합니다.");
                assertEquals(1, result[10][3],
                    "삭제 행보다 아래에 있던 효과 밖 블록은 원래 행에 보존되어야 합니다.");
                assertSame(queued, ExistingFeatureFixture.get(fixture.board, "curr", Block.class));
                assertEquals(3, fixture.board.x);
                assertEquals(0, fixture.board.y);
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-13: 아이템 행과 기존 완성 행의 합집합을 중복 없이 3줄 콤보로 계산")
    void completedRowsAndItemRowsAreScoredAsOneDeduplicatedUnion() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                int[][] grid = new int[20][10];
                Arrays.fill(grid[10], 1);
                Arrays.fill(grid[18], 1);
                Arrays.fill(grid[19], 1);
                grid[18][4] = 0;
                grid[18][5] = 0;
                grid[19][4] = 0;
                grid[19][5] = 0;
                fixture.prepare(new CrossBlock(), new OBlock(), grid, 4, 18);

                assertTrue(fixture.board.useItem());

                assertEquals(600, fixture.board.getScore(), "세 행은 100+200+300점이어야 합니다.");
                assertEquals(3, intField(fixture.board, "erasedLineNum"));
                assertTrue(Arrays.stream(board(fixture)[19]).allMatch(cell -> cell == 0));
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-13: 모든 팔레트에서 2x2 P 마커 네 칸을 실제 화면으로 식별")
    void markerRemainsVisibleInEveryPalette() throws Exception {
        for (String palette : List.of("Normal", "Protanopia", "Deuteranopia", "Tritanopia")) {
            Maincontainer.colorPalette = palette;
            ExistingFeatureFixture.onEdt(() -> {
                try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                    CrossBlock item = new CrossBlock();
                    fixture.prepare(item, new OBlock(), new int[20][10], 3, 5);
                    fixture.game.drawNextBoard(item);
                    Nextblockpanel preview = ExistingFeatureFixture.get(
                        fixture.game, "nextblockpanel", Nextblockpanel.class);
                    List<?> previewChunks = ExistingFeatureFixture.get(preview, "textChunks", ArrayList.class);
                    int previewMarkers = 0;
                    for (Object chunk : previewChunks) {
                        if (ExistingFeatureFixture.get(chunk, "text", String.class).equals("P")) {
                            previewMarkers++;
                            assertEquals(item.getColor(), ExistingFeatureFixture.get(chunk, "color", Color.class));
                        }
                    }
                    assertEquals(4, previewMarkers, "미리보기에도 보라색 P 네 칸이 표시되어야 합니다.");
                    BufferedImage rendered = fixture.render(palette + "-cross-P.png");
                    for (int row = 0; row < item.height(); row++) {
                        for (int column = 0; column < item.width(); column++) {
                            assertEquals("P", item.getText(column, row));
                            assertEquals(item.getColor(), text(fixture, 5 + row, 3 + column).color);
                            assertTrue(fixture.contrastingPixelsInCell(rendered,
                                3 + column + 1, 5 + row + 1) > 4,
                                palette + " 팔레트에서 P 마커가 보이지 않습니다.");
                        }
                    }
                }
                return null;
            });
        }
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
    private static void setText(ExistingFeatureFixture fixture, int row, int column,
            String value, Color color) throws Exception {
        ArrayList<Board.TextChunk> chunks = ExistingFeatureFixture.get(
            fixture.board, "textChunks", ArrayList.class);
        chunks.set((row + 1) * 13 + column + 1, fixture.board.new TextChunk(value, color));
    }

    private static int intField(Object target, String name) throws Exception {
        return ExistingFeatureFixture.get(target, name, Integer.class);
    }
}
