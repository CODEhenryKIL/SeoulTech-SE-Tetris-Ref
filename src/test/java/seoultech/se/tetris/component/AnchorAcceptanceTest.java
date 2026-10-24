package seoultech.se.tetris.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
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
import seoultech.se.tetris.blocks.AnchorBlock;
import seoultech.se.tetris.blocks.Block;
import seoultech.se.tetris.blocks.BombBlock;
import seoultech.se.tetris.blocks.ColumnBlock;
import seoultech.se.tetris.blocks.OBlock;
import seoultech.se.tetris.blocks.RowBlock;

class AnchorAcceptanceTest {
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
    @Timeout(10)
    @DisplayName("R2-12: 무게추는 모든 색상 모드에서 너비 4와 고유 형상으로 식별")
    void anchorKeepsDistinctGeometryAndVisibleCellsAcrossEveryPalette() throws Exception {
        AnchorBlock anchor = new AnchorBlock();

        assertEquals(4, anchor.width());
        assertEquals(2, anchor.height());
        assertEquals(List.of(" OO ", "OOOO"), rows(anchor));
        assertTrue(rows(new RowBlock()).stream().anyMatch(row -> row.contains("L")));
        assertTrue(rows(new ColumnBlock()).stream().anyMatch(row -> row.contains("C")));
        assertTrue(rows(new BombBlock()).stream().anyMatch(row -> row.contains("B")));

        for (String palette : List.of("Normal", "Protanopia", "Deuteranopia", "Tritanopia")) {
            Maincontainer.colorPalette = palette;
            assertFalse(Maincontainer.changeColor(anchor.getColor())
                    .equals(Maincontainer.changeColor(Color.BLACK)), palette);
            assertEquals(List.of(" OO ", "OOOO"), rows(anchor), palette);
            ExistingFeatureFixture.onEdt(() -> {
                try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                    fixture.prepare(anchor, new OBlock(), new int[20][10], 3, 5);
                    BufferedImage rendered = fixture.render(palette + "-anchor.png");
                    for (int row = 0; row < anchor.height(); row++) {
                        for (int col = 0; col < anchor.width(); col++) {
                            if (anchor.getShape(col, row) != 0) {
                                assertTrue(fixture.contrastingPixelsInCell(rendered, 4 + col, 6 + row) > 4,
                                    palette + " 무게추 점유 칸의 실제 렌더링 대비가 부족합니다.");
                            }
                        }
                    }
                }
                return null;
            });
        }
    }

    @ParameterizedTest(name = "x={0}")
    @ValueSource(ints = {0, 6})
    @Timeout(10)
    @DisplayName("R2-12: 좌우 벽에서 효과 폭 4칸 내부만 제거하고 바깥 블록은 보존")
    void effectClearsOnlyItsFourColumnsAtBothWalls(int anchorX) throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                AnchorBlock anchor = new AnchorBlock();
                int[][] grid = new int[20][10];
                int outside = anchorX == 0 ? 4 : 5;
                grid[1][anchorX] = 1;
                for (int row : new int[] {6, 10, 17}) {
                    for (int column = anchorX; column < anchorX + anchor.width(); column++) {
                        grid[row][column] = 1;
                    }
                    grid[row][outside] = 1;
                }
                fixture.prepare(anchor, new OBlock(), grid, anchorX, 2);

                assertTrue(fixture.board.useItem());

                int[][] result = ExistingFeatureFixture.get(fixture.board, "board", int[][].class);
                assertEquals(18, fixture.board.y);
                assertEquals(0, fixture.board.getScore(), "아이템 효과와 강제 바닥 이동은 무점수");
                assertEquals(1, result[1][anchorX], "효과 시작 위치 위의 블록은 보존");
                for (int row : new int[] {6, 10, 17}) {
                    for (int column = anchorX; column < anchorX + anchor.width(); column++) {
                        assertEquals(0, result[row][column], "row=" + row + ", col=" + column);
                    }
                    assertEquals(1, result[row][outside], "효과 너비 밖의 블록은 보존");
                }
                assertAnchorAtBottom(result, anchorX);
                return null;
            }
        });
    }

    @ParameterizedTest(name = "level={0}")
    @ValueSource(ints = {1, 2, 19})
    @Timeout(10)
    @DisplayName("R2-12: 쌓인 블록 접촉 뒤 효과·최종 정지·다음 블록 전환은 한 번만 진행")
    void hardDropRemovesStackThenAdvancesAndScoresOnlyActualDropDistance(int level) throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                AnchorBlock anchor = new AnchorBlock();
                OBlock queued = new OBlock();
                int[][] grid = new int[20][10];
                grid[4][3] = 1;
                grid[7][4] = 1;
                grid[11][5] = 1;
                grid[16][6] = 1;
                grid[4][2] = 1;
                grid[11][8] = 1;
                fixture.prepare(anchor, queued, grid, 3, 0);
                ExistingFeatureFixture.set(fixture.board, "level", level);

                fixture.board.hardDrop();

                int[][] result = ExistingFeatureFixture.get(fixture.board, "board", int[][].class);
                Block current = ExistingFeatureFixture.get(fixture.board, "curr", Block.class);
                assertSame(queued, current);
                assertEquals(3, fixture.board.x);
                assertEquals(0, fixture.board.y);
                assertEquals(2 * level, fixture.board.getScore(),
                    "접촉까지 실제 하드드롭한 2칸만 기존 칸당 점수로 계산");
                assertEquals(0, result[4][3]);
                assertEquals(0, result[7][4]);
                assertEquals(0, result[11][5]);
                assertEquals(0, result[16][6]);
                assertEquals(1, result[4][2]);
                assertEquals(1, result[11][8]);
                assertAnchorAtBottom(result, 3);
                return null;
            }
        });
    }

    @Test
    @Timeout(10)
    @DisplayName("R2-12: 빈 보드의 하드드롭은 18칸 낙하만 계산하고 바닥에 정지")
    void emptyBoardDropKeepsNormalDropPointsWithoutAnEffectBonus() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = new ExistingFeatureFixture(Difficulty.NORMAL)) {
                OBlock queued = new OBlock();
                fixture.prepare(new AnchorBlock(), queued, new int[20][10], 3, 0);
                fixture.board.hardDrop();
                assertEquals(18, fixture.board.getScore());
                assertSame(queued, ExistingFeatureFixture.get(fixture.board, "curr", Block.class));
                assertAnchorAtBottom(ExistingFeatureFixture.get(fixture.board, "board", int[][].class), 3);
                return null;
            }
        });
    }

    private static List<String> rows(Block block) {
        java.util.ArrayList<String> rows = new java.util.ArrayList<>();
        for (int y = 0; y < block.height(); y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < block.width(); x++) row.append(block.getText(x, y));
            rows.add(row.toString());
        }
        return rows;
    }

    private static void assertAnchorAtBottom(int[][] grid, int x) {
        assertEquals(0, grid[18][x]);
        assertEquals(1, grid[18][x + 1]);
        assertEquals(1, grid[18][x + 2]);
        assertEquals(0, grid[18][x + 3]);
        for (int column = x; column < x + 4; column++) assertEquals(1, grid[19][column]);
    }
}
