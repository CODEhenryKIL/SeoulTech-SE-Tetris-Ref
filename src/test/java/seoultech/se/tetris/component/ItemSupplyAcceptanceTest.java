package seoultech.se.tetris.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import seoultech.se.tetris.Difficulty;
import seoultech.se.tetris.GameMode;
import seoultech.se.tetris.blocks.Block;
import seoultech.se.tetris.blocks.ItemBlock;
import seoultech.se.tetris.blocks.OBlock;

/** R2-10 acceptance boundaries for line-count based item supply. */
class ItemSupplyAcceptanceTest {
    private final String originalDataDir = System.getProperty("tetris.dataDir");

    @TempDir
    Path temporary;

    @BeforeEach
    void isolatePersistentSettings() {
        System.setProperty("tetris.dataDir", temporary.toString());
    }

    @AfterEach
    void restoreGlobalSettings() {
        if (originalDataDir == null) System.clearProperty("tetris.dataDir");
        else System.setProperty("tetris.dataDir", originalDataDir);
    }

    @ParameterizedTest(name = "clearedBefore={0}")
    @ValueSource(ints = {9, 19})
    @Timeout(15)
    @DisplayName("R2-10: 아이템 모드는 10줄 경계를 넘긴 고정에서 다음 미리보기에 아이템 하나를 공급")
    void crossingEachTenLineBoundarySuppliesOneNextItem(int clearedBefore) throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = fixture(GameMode.ITEM)) {
                OBlock queued = prepareSingleLineClear(fixture, clearedBefore);

                fixture.board.hardDrop();

                assertEquals(clearedBefore + 1, intField(fixture.board, "erasedLineNum"));
                assertSame(queued, current(fixture), "기존 미리보기는 현재 블록으로 승계되어야 합니다.");
                Block awarded = next(fixture);
                assertInstanceOf(ItemBlock.class, awarded);
                assertEquals(0, intField(fixture.board, "pendingItems"));
                assertPreviewMatches(fixture, awarded);
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-10: 복수 공급 권리는 일반 삭제 강조 종료 후에도 남아 다음 고정에서 순차 소비")
    void multipleEntitlementsAreQueuedAndConsumedOnePerLock() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = fixture(GameMode.ITEM)) {
                OBlock originalPreview = new OBlock();
                fixture.prepare(new OBlock(), originalPreview, twelveFullRows(), 3, 18);
                ExistingFeatureFixture.set(fixture.board, "erasedLineNum", 9);

                fixture.board.hardDrop();

                assertEquals(21, intField(fixture.board, "erasedLineNum"));
                assertSame(originalPreview, current(fixture));
                Block firstAwardedPreview = next(fixture);
                assertInstanceOf(ItemBlock.class, firstAwardedPreview);
                assertEquals(1, intField(fixture.board, "pendingItems"));
                Timer overlay = ExistingFeatureFixture.get(
                    fixture.board, "clearAnimationTimer", Timer.class);
                assertTrue(overlay != null && overlay.isRunning(),
                    "복수 행 삭제 직후에는 실제 일반 삭제 강조가 실행 중이어야 합니다.");

                awaitClearOverlayEnds(fixture.board);

                assertEquals(1, intField(fixture.board, "pendingItems"),
                    "일반 삭제 강조의 자연 종료는 대기 중인 공급 권리를 지우면 안 됩니다.");

                fixture.board.hardDrop();

                assertSame(firstAwardedPreview, current(fixture),
                    "첫 번째 아이템 미리보기는 다음 고정에서 현재 블록이 되어야 합니다.");
                assertInstanceOf(ItemBlock.class, next(fixture),
                    "남은 권리 하나는 줄 삭제가 없어도 다음 미리보기에 공급되어야 합니다.");
                assertEquals(0, intField(fixture.board, "pendingItems"));
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-10: 일반 이동 타이머 틱은 이미 공급한 아이템 미리보기를 중복 교체하지 않음")
    void movementTickDoesNotReplaceAwardedPreview() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = fixture(GameMode.ITEM)) {
                prepareSingleLineClear(fixture, 9);
                fixture.board.hardDrop();
                Block awarded = next(fixture);

                assertTrue(fixture.board.moveDown());

                assertSame(awarded, next(fixture));
                assertEquals(0, intField(fixture.board, "pendingItems"));
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-10: 클래식 모드는 10줄 경계를 넘어도 일반 블록만 공급")
    void classicModeNeverSuppliesAnItemAtThreshold() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = fixture(GameMode.CLASSIC)) {
                OBlock queued = prepareSingleLineClear(fixture, 9);

                fixture.board.hardDrop();

                assertSame(queued, current(fixture));
                assertFalse(next(fixture) instanceof ItemBlock);
                assertEquals(0, intField(fixture.board, "pendingItems"));
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-10: 새 게임 격자 초기화는 이전 게임의 미공급 아이템 권리를 제거")
    void resetClearsPendingEntitlements() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = fixture(GameMode.ITEM)) {
                ExistingFeatureFixture.set(fixture.board, "pendingItems", 2);

                ExistingFeatureFixture.invoke(fixture.board, "reset");

                assertEquals(0, intField(fixture.board, "pendingItems"));
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-10: 게임 종료용 애니메이션 정지는 이전 게임의 미공급 아이템 권리를 제거")
    void stoppingGameClearsPendingEntitlements() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = fixture(GameMode.ITEM)) {
                ExistingFeatureFixture.set(fixture.board, "pendingItems", 2);

                fixture.board.stopAnimations();

                assertEquals(0, intField(fixture.board, "pendingItems"));
            }
            return null;
        });
    }

    @Test
    @Timeout(15)
    @DisplayName("R2-10: 실제 생성 충돌 게임오버에서 지급 권리를 버리고 새 게임은 누계 없이 시작")
    void spawnCollisionDiscardsEntitlementsAndNewGameStartsClean() throws Exception {
        ExistingFeatureFixture.onEdt(() -> {
            try (ExistingFeatureFixture fixture = fixture(GameMode.ITEM)) {
                int[][] grid = new int[20][10];
                grid[0][3] = 1;
                OBlock active = new OBlock();
                OBlock queued = new OBlock();
                fixture.prepare(active, queued, grid, 3, 18);
                ExistingFeatureFixture.set(fixture.board, "pendingItems", 2);
                ExistingFeatureFixture.set(fixture.board, "erasedLineNum", 21);

                fixture.board.hardDrop();

                assertNull(fixture.board.timer);
                assertEquals(0, intField(fixture.board, "pendingItems"));
                assertSame(active, current(fixture), "게임오버 뒤에는 다음 아이템을 공급하지 않습니다.");
                assertSame(queued, next(fixture));
                assertTrue(ExistingFeatureFixture.get(fixture.game, "gameOverPanel", javax.swing.JPanel.class)
                    .getParent() == fixture.game);
            }
            try (ExistingFeatureFixture fresh = fixture(GameMode.ITEM)) {
                assertEquals(0, intField(fresh.board, "pendingItems"));
                assertEquals(0, intField(fresh.board, "erasedLineNum"));
                assertFalse(current(fresh) instanceof ItemBlock);
                assertFalse(next(fresh) instanceof ItemBlock);
            }
            return null;
        });
    }

    private static ExistingFeatureFixture fixture(GameMode mode) throws Exception {
        return new ExistingFeatureFixture(Difficulty.NORMAL, mode);
    }

    private static OBlock prepareSingleLineClear(ExistingFeatureFixture fixture, int clearedBefore)
            throws Exception {
        OBlock queued = new OBlock();
        int[][] grid = new int[20][10];
        Arrays.fill(grid[19], 1);
        grid[19][3] = 0;
        grid[19][4] = 0;
        fixture.prepare(new OBlock(), queued, grid, 3, 18);
        ExistingFeatureFixture.set(fixture.board, "erasedLineNum", clearedBefore);
        return queued;
    }

    private static int[][] twelveFullRows() {
        int[][] grid = new int[20][10];
        for (int row = 8; row < 20; row++) Arrays.fill(grid[row], 1);
        for (int row = 18; row < 20; row++) {
            grid[row][3] = 0;
            grid[row][4] = 0;
        }
        return grid;
    }

    private static Block current(ExistingFeatureFixture fixture) throws Exception {
        return ExistingFeatureFixture.get(fixture.board, "curr", Block.class);
    }

    private static Block next(ExistingFeatureFixture fixture) throws Exception {
        return ExistingFeatureFixture.get(fixture.board, "next", Block.class);
    }

    private static int intField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(target);
    }

    private static void awaitClearOverlayEnds(Board board) throws Exception {
        SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
        AtomicReference<Throwable> callbackFailure = new AtomicReference<>();
        boolean[] completed = {false};
        Timer[] pollHolder = new Timer[1];
        Timer[] timeoutHolder = new Timer[1];

        Timer poll = new Timer(15, event -> {
            try {
                Timer animation = ExistingFeatureFixture.get(
                    board, "clearAnimationTimer", Timer.class);
                BitSet rows = ExistingFeatureFixture.get(board, "clearingRows", BitSet.class);
                if (animation == null && rows.isEmpty()) {
                    completed[0] = true;
                    pollHolder[0].stop();
                    timeoutHolder[0].stop();
                    loop.exit();
                }
            } catch (Throwable failure) {
                callbackFailure.compareAndSet(null, failure);
                pollHolder[0].stop();
                timeoutHolder[0].stop();
                loop.exit();
            }
        });
        Timer timeout = new Timer(2_000, event -> {
            callbackFailure.compareAndSet(null,
                new AssertionError("일반 삭제 강조가 제한 시간 안에 자연 종료되어야 합니다."));
            pollHolder[0].stop();
            ((Timer) event.getSource()).stop();
            loop.exit();
        });
        timeout.setRepeats(false);
        pollHolder[0] = poll;
        timeoutHolder[0] = timeout;

        poll.start();
        timeout.start();
        try {
            if (!loop.enter()) throw new AssertionError("Swing 보조 이벤트 루프를 시작하지 못했습니다.");
        } finally {
            poll.stop();
            timeout.stop();
        }
        if (callbackFailure.get() != null) {
            throw new AssertionError("줄 삭제 강조 대기 중 Swing 콜백이 실패했습니다.",
                callbackFailure.get());
        }
        assertTrue(completed[0], "일반 삭제 강조 종료 상태를 관찰해야 합니다.");
    }

    private static void assertPreviewMatches(ExistingFeatureFixture fixture, Block expected) throws Exception {
        Object panel = ExistingFeatureFixture.get(fixture.game, "nextblockpanel", Nextblockpanel.class);
        List<?> chunks = ExistingFeatureFixture.get(panel, "textChunks", ArrayList.class);
        int renderedCells = 0;
        for (Object chunk : chunks) {
            Field text = chunk.getClass().getDeclaredField("text");
            text.setAccessible(true);
            String value = (String) text.get(chunk);
            if (!value.isBlank()) renderedCells++;
        }
        int occupiedCells = 0;
        for (int row = 0; row < expected.height(); row++) {
            for (int column = 0; column < expected.width(); column++) {
                if (expected.getShape(column, row) != 0) occupiedCells++;
            }
        }
        assertEquals(occupiedCells, renderedCells,
            "다음 블록 패널은 공급된 아이템의 점유 칸을 그대로 표시해야 합니다.");
    }
}
