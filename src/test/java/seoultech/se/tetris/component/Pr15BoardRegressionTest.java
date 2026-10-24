package seoultech.se.tetris.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import seoultech.se.tetris.Difficulty;
import seoultech.se.tetris.blocks.AnchorBlock;
import seoultech.se.tetris.blocks.Block;
import seoultech.se.tetris.blocks.ColumnBlock;
import seoultech.se.tetris.blocks.ItemBlock;
import seoultech.se.tetris.blocks.OBlock;
import seoultech.se.tetris.blocks.RowBlock;
import sun.misc.Unsafe;

/** Regression coverage for the four remaining PR15 requirement-2 board bugs. */
class Pr15BoardRegressionTest {
    private static final String DATA_DIR_PROPERTY = "tetris.dataDir";
    private static final String EMPTY_BOARD_TEXT =
        "XXXXXXXXXXXX\n" + "X          X\n".repeat(20) + "XXXXXXXXXXXX";

    @TempDir
    Path dataDir;

    private String previousDataDir;

    @BeforeEach
    void isolatePersistentData() {
        previousDataDir = System.getProperty(DATA_DIR_PROPERTY);
        System.setProperty(DATA_DIR_PROPERTY, dataDir.toString());
    }

    @AfterEach
    void restorePersistentDataLocation() {
        if (previousDataDir == null) {
            System.clearProperty(DATA_DIR_PROPERTY);
        } else {
            System.setProperty(DATA_DIR_PROPERTY, previousDataDir);
        }
    }

    @Test
    @Timeout(3)
    @DisplayName("BUG03-1: 행 아이템 애니메이션 도중 일시정지하면 완료 후에도 정지 상태 유지")
    void rowItemAnimationKeepsPauseRequestedDuringAnimation() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                prepareActiveBlock(fixture.board, new RowBlock(), new OBlock(),
                    new int[20][10], 3, 8);
                startSlowGameTimer(fixture.board);

                Timer pauseDuringAnimation = oneShot(25, fixture.board::togglePause);
                try {
                    fixture.board.useItem();
                } finally {
                    pauseDuringAnimation.stop();
                }

                assertTrue(fixture.board.getIsPaused());
                assertFalse(fixture.board.timer.isRunning(),
                    "아이템 애니메이션 종료가 사용자의 일시정지를 취소하면 안 됩니다.");
                return null;
            });
        }
    }

    @Test
    @Timeout(3)
    @DisplayName("BUG03-1: 열 아이템 애니메이션 도중 재개하면 완료 후 게임 타이머 실행")
    void columnItemAnimationKeepsResumeRequestedDuringAnimation() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                prepareActiveBlock(fixture.board, new ColumnBlock(), new OBlock(),
                    new int[20][10], 3, 8);
                startSlowGameTimer(fixture.board);
                fixture.board.togglePause();
                assertTrue(fixture.board.getIsPaused());

                Timer resumeDuringAnimation = oneShot(25, fixture.board::togglePause);
                try {
                    fixture.board.useItem();
                } finally {
                    resumeDuringAnimation.stop();
                }

                assertFalse(fixture.board.getIsPaused());
                assertTrue(fixture.board.timer.isRunning(),
                    "애니메이션 도중 재개한 게임은 완료 뒤에도 계속 실행되어야 합니다.");
                return null;
            });
        }
    }

    @ParameterizedTest(name = "{0} animation cancelled by menu")
    @EnumSource(AnimatedItem.class)
    @Timeout(5)
    @DisplayName("BUG03-1: 메뉴 이동은 진행 중 아이템 애니메이션과 후속 확정을 취소")
    void menuExitCancelsActiveItemAnimationWithoutLateAdvance(AnimatedItem item) throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                Block active = item.createBlock();
                Block queued = new OBlock();
                int landingY = 20 - active.height();
                prepareActiveBlock(fixture.board, active, queued,
                    new int[20][10], 3, landingY);
                startSlowGameTimer(fixture.board);
                AtomicReference<Throwable> callbackFailure = new AtomicReference<>();

                Timer exitDuringAnimation = oneShot(25,
                    () -> capture(callbackFailure, () -> fixture.menuButton().doClick()));
                try {
                    fixture.board.hardDrop();
                } finally {
                    exitDuringAnimation.stop();
                }

                assertNoCallbackFailure(callbackFailure);
                assertTrue(fixture.owner.exitCalled);
                assertNull(fixture.board.timer);
                assertSame(active, field(fixture.board, "curr", Block.class));
                assertSame(queued, field(fixture.board, "next", Block.class));
                assertEquals(0, fixture.board.getScore());
                assertEquals(0, intField(fixture.board, "erasedLineNum"));
                assertNull(field(fixture.board, "itemAnimationTimer", Timer.class));
                assertNull(field(fixture.board, "currentLoop", java.awt.SecondaryLoop.class));
                return null;
            });
        }
    }

    @Test
    @Timeout(5)
    @DisplayName("BUG03-1: 아이템 애니메이션 중 모든 블록 입력은 확정·점수·위치 변경 없음")
    void movementInputsAreIgnoredUntilItemAnimationFinishes() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                RowBlock active = new RowBlock();
                Block queued = new OBlock();
                int landingY = 20 - active.height();
                prepareActiveBlock(fixture.board, active, queued,
                    new int[20][10], 3, landingY);
                startSlowGameTimer(fixture.board);
                AtomicBoolean callbackRan = new AtomicBoolean();
                AtomicReference<Throwable> callbackFailure = new AtomicReference<>();

                Timer inputsDuringAnimation = oneShot(25, () -> capture(callbackFailure, () -> {
                    callbackRan.set(true);
                    assertAnimationState(fixture.board, active, queued, 3, landingY);
                    fixture.board.moveLeft();
                    assertAnimationState(fixture.board, active, queued, 3, landingY);
                    fixture.board.moveRight();
                    assertAnimationState(fixture.board, active, queued, 3, landingY);
                    assertFalse(fixture.board.moveDown());
                    assertAnimationState(fixture.board, active, queued, 3, landingY);
                    fixture.board.hardDrop();
                    assertAnimationState(fixture.board, active, queued, 3, landingY);
                    fixture.board.rotate();
                    assertAnimationState(fixture.board, active, queued, 3, landingY);
                }));
                try {
                    fixture.board.hardDrop();
                } finally {
                    inputsDuringAnimation.stop();
                }

                assertTrue(callbackRan.get(), "애니메이션 도중 입력 콜백이 실행되어야 합니다.");
                assertNoCallbackFailure(callbackFailure);
                assertSame(queued, field(fixture.board, "curr", Block.class),
                    "완료 후에는 정확히 한 번만 다음 블록으로 진행해야 합니다.");
                assertEquals(100, fixture.board.getScore());
                assertEquals(1, intField(fixture.board, "erasedLineNum"));
                return null;
            });
        }
    }

    @Test
    @Timeout(5)
    @DisplayName("BUG03-1: 같은 애니메이션 안에서 일시정지·재개해도 완료 전 낙하 금지")
    void pauseThenResumeDuringOneAnimationStartsDropOnlyAfterCompletion() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                prepareActiveBlock(fixture.board, new RowBlock(), new OBlock(),
                    new int[20][10], 3, 8);
                startSlowGameTimer(fixture.board);
                AtomicBoolean pauseRan = new AtomicBoolean();
                AtomicBoolean resumeRan = new AtomicBoolean();
                AtomicReference<Throwable> callbackFailure = new AtomicReference<>();

                Timer pause = oneShot(25, () -> capture(callbackFailure, () -> {
                    pauseRan.set(true);
                    fixture.board.togglePause();
                    assertTrue(fixture.board.getIsPaused());
                    assertFalse(fixture.board.timer.isRunning());
                }));
                Timer resume = oneShot(75, () -> capture(callbackFailure, () -> {
                    resumeRan.set(true);
                    fixture.board.togglePause();
                    assertFalse(fixture.board.getIsPaused());
                    assertFalse(fixture.board.timer.isRunning(),
                        "재개 입력이 애니메이션 완료 전에 낙하 타이머를 시작하면 안 됩니다.");
                    assertNotNull(field(fixture.board, "itemAnimationTimer", Timer.class));
                }));
                try {
                    assertTrue(fixture.board.useItem());
                } finally {
                    pause.stop();
                    resume.stop();
                }

                assertTrue(pauseRan.get());
                assertTrue(resumeRan.get());
                assertNoCallbackFailure(callbackFailure);
                assertFalse(fixture.board.getIsPaused());
                assertTrue(fixture.board.timer.isRunning(),
                    "애니메이션 완료 뒤에는 재개 요청에 따라 낙하가 시작되어야 합니다.");
                return null;
            });
        }
    }

    @Test
    @Timeout(5)
    @DisplayName("BUG03-1: 이전 일반 삭제 강조는 다음 행 아이템 애니메이션을 취소하지 않음")
    void normalClearOverlayDoesNotCancelImmediatelyFollowingRowItemAnimation() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                OBlock first = new OBlock();
                RowBlock second = new RowBlock();
                int[][] grid = new int[20][10];
                fillExceptActiveCells(grid[19], first, 1, 3);
                prepareActiveBlock(fixture.board, first, second, grid, 3, 18);
                startSlowGameTimer(fixture.board);

                fixture.board.hardDrop();

                assertEquals(1, intField(fixture.board, "erasedLineNum"));
                assertSame(second, field(fixture.board, "curr", Block.class));
                Timer clearOverlay = field(fixture.board, "clearAnimationTimer", Timer.class);
                assertNotNull(clearOverlay);
                assertTrue(clearOverlay.isRunning());
                Block queuedAfterFirstDrop = fixture.board.getNextBlock();

                fixture.board.hardDrop();

                assertEquals(2, intField(fixture.board, "erasedLineNum"));
                assertSame(queuedAfterFirstDrop, field(fixture.board, "curr", Block.class));
                assertTrue(fixture.board.getScore() >= 200,
                    "일반 행과 L행 점수 및 두 번째 하드드롭 점수가 반영되어야 합니다.");
                assertTrue(fixture.board.timer.isRunning());
                assertNull(field(fixture.board, "itemAnimationTimer", Timer.class));
                assertNull(field(fixture.board, "currentLoop", java.awt.SecondaryLoop.class));
                return null;
            });
        }
    }

    @Test
    @Timeout(3)
    @DisplayName("BUG03-4: L행과 별도 완성 행의 합집합을 한 번에 점수·개수 반영")
    void rowItemAndDifferentFullRowUseOneClearUnion() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                RowBlock rowBlock = new RowBlock();
                int landingY = 20 - rowBlock.height();
                int itemRow = landingY + rowBlock.getItemY();
                int[][] grid = new int[20][10];
                Arrays.fill(grid[5], 1);
                fillExceptActiveCells(grid[itemRow], rowBlock, rowBlock.getItemY(), 3);
                prepareActiveBlock(fixture.board, rowBlock, new OBlock(), grid, 3, landingY);

                fixture.board.hardDrop();

                assertEquals(300, fixture.board.getScore(),
                    "두 행의 단일 콤보 점수는 100 + 200이어야 합니다.");
                assertEquals(2, intField(fixture.board, "erasedLineNum"));
                return null;
            });
        }
    }

    @Test
    @Timeout(3)
    @DisplayName("BUG03-4: 완성 행과 겹친 L행은 한 번만 세고 10행 경계에서 다음 아이템 생성")
    void overlappingRowItemCountsOnceAndTriggersBoundaryItem() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                RowBlock rowBlock = new RowBlock();
                int landingY = 20 - rowBlock.height();
                int itemRow = landingY + rowBlock.getItemY();
                int[][] grid = new int[20][10];
                fillExceptActiveCells(grid[itemRow], rowBlock, rowBlock.getItemY(), 3);
                prepareActiveBlock(fixture.board, rowBlock, new OBlock(), grid, 3, landingY);
                set(fixture.board, "erasedLineNum", 9);

                fixture.board.hardDrop();

                assertEquals(100, fixture.board.getScore(),
                    "같은 행의 일반 완성과 L 효과가 중복 점수로 계산되면 안 됩니다.");
                assertEquals(10, intField(fixture.board, "erasedLineNum"));
                assertInstanceOf(ItemBlock.class, fixture.board.getNextBlock(),
                    "9행에서 10행으로 넘어가면 아이템 모드의 다음 블록은 아이템이어야 합니다.");
                return null;
            });
        }
    }

    @ParameterizedTest(name = "{0} level-up interval")
    @EnumSource(Difficulty.class)
    @Timeout(3)
    @DisplayName("BUG03-5: 실제 레벨업은 난이도별 20% 속도 계수를 적용")
    void actualLevelUpUsesDifficultySpeedFactor(Difficulty difficulty) throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(difficulty, "Classic"));
        try (fixture) {
            onEdt(() -> {
                assertEquals(1_000, fixture.board.timer.getDelay(),
                    "모든 난이도의 레벨 1 간격은 1초여야 합니다.");

                invoke(fixture.board, "levelUp");
                assertEquals(expectedDelay(difficulty, 2), fixture.board.timer.getDelay());
                assertEquals(2, intField(fixture.board, "level"));

                invoke(fixture.board, "levelUp");
                assertEquals(expectedDelay(difficulty, 3), fixture.board.timer.getDelay());
                assertEquals(3, intField(fixture.board, "level"));
                return null;
            });
        }
    }

    @Test
    @DisplayName("BUG03-6: 바닥에 접촉한 앵커는 좌우 이동과 회전 불가")
    void anchorCannotMoveOrRotateAfterFloorContact() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                AnchorBlock anchor = new AnchorBlock();
                prepareActiveBlock(fixture.board, anchor, new OBlock(),
                    new int[20][10], 3, 18);

                fixture.board.moveLeft();
                assertPosition(fixture.board, 3, 18);
                fixture.board.moveRight();
                assertPosition(fixture.board, 3, 18);
                fixture.board.rotate();
                assertPosition(fixture.board, 3, 18);
                return null;
            });
        }
    }

    @Test
    @DisplayName("BUG03-6: 블록에 접촉한 공중 앵커는 반복 입력에도 위치 고정")
    void anchorCannotEscapeBlockContactWithRepeatedInput() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                int[][] grid = new int[20][10];
                grid[10][3] = 1;
                prepareActiveBlock(fixture.board, new AnchorBlock(), new OBlock(), grid, 3, 8);

                fixture.board.moveLeft();
                assertPosition(fixture.board, 3, 8);
                fixture.board.moveRight();
                assertPosition(fixture.board, 3, 8);
                fixture.board.moveLeft();
                assertPosition(fixture.board, 3, 8);
                fixture.board.rotate();
                assertPosition(fixture.board, 3, 8);
                return null;
            });
        }
    }

    @Test
    @DisplayName("BUG03-6: 접촉 전 앵커는 좌우 이동 가능")
    void anchorCanMoveBeforeContact() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                prepareActiveBlock(fixture.board, new AnchorBlock(), new OBlock(),
                    new int[20][10], 3, 5);

                fixture.board.moveLeft();
                assertPosition(fixture.board, 2, 5);
                fixture.board.moveRight();
                assertPosition(fixture.board, 3, 5);
                return null;
            });
        }
    }

    @Test
    @Timeout(3)
    @DisplayName("BUG03-6: 앵커 확정 후 다음 블록은 접촉 잠금 없이 이동")
    void advancingFromAnchorResetsContactForNextBlock() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                int[][] grid = new int[20][10];
                grid[10][3] = 1;
                prepareActiveBlock(fixture.board, new AnchorBlock(), new OBlock(), grid, 3, 8);

                assertFalse(fixture.board.moveDown(), "접촉한 앵커는 실제 확정 경로로 진행해야 합니다.");
                assertInstanceOf(OBlock.class, field(fixture.board, "curr", Block.class));
                assertPosition(fixture.board, 3, 0);

                fixture.board.moveLeft();
                assertPosition(fixture.board, 2, 0);
                return null;
            });
        }
    }

    @Test
    @Timeout(3)
    @DisplayName("BUG03-6: 앵커 접촉 잠금은 장애물이 사라져도 유지되고 다음 블록에서 해제")
    void anchorContactLatchPersistsAfterObstacleRemovalAndClearsOnAdvance() throws Exception {
        GameFixture fixture = onEdt(() -> new GameFixture(Difficulty.NORMAL, "Item"));
        try (fixture) {
            onEdt(() -> {
                int[][] grid = new int[20][10];
                grid[10][3] = 1;
                AnchorBlock anchor = new AnchorBlock();
                Block queued = new OBlock();
                prepareActiveBlock(fixture.board, anchor, queued, grid, 3, 8);

                fixture.board.moveLeft();
                assertPosition(fixture.board, 3, 8);
                grid[10][3] = 0;

                fixture.board.moveLeft();
                assertPosition(fixture.board, 3, 8);
                fixture.board.moveRight();
                assertPosition(fixture.board, 3, 8);

                fixture.board.hardDrop();
                assertSame(queued, field(fixture.board, "curr", Block.class));
                assertPosition(fixture.board, 3, 0);
                fixture.board.moveLeft();
                assertPosition(fixture.board, 2, 0);
                return null;
            });
        }
    }

    private static void startSlowGameTimer(Board board) {
        board.timer.stop();
        board.timer.setInitialDelay(10_000);
        board.timer.setDelay(10_000);
        board.timer.start();
    }

    private static Timer oneShot(int delay, Runnable action) {
        Timer timer = new Timer(delay, event -> action.run());
        timer.setRepeats(false);
        timer.start();
        return timer;
    }

    private static void assertAnimationState(Board board, Block active, Block queued,
            int expectedX, int expectedY) throws Exception {
        assertSame(active, field(board, "curr", Block.class));
        assertSame(queued, field(board, "next", Block.class));
        assertPosition(board, expectedX, expectedY);
        assertEquals(0, board.getScore());
        assertEquals(0, intField(board, "erasedLineNum"));
        assertNotNull(field(board, "itemAnimationTimer", Timer.class));
    }

    private static void capture(AtomicReference<Throwable> failure, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable throwable) {
            failure.compareAndSet(null, throwable);
        }
    }

    private static void assertNoCallbackFailure(AtomicReference<Throwable> failure) {
        if (failure.get() != null) {
            throw new AssertionError("애니메이션 콜백 검사 실패", failure.get());
        }
    }

    private static void fillExceptActiveCells(int[] row, Block block, int blockRow, int x) {
        Arrays.fill(row, 1);
        for (int column = 0; column < block.width(); column++) {
            if (block.getShape(column, blockRow) != 0) row[x + column] = 0;
        }
    }

    private static int expectedDelay(Difficulty difficulty, int level) {
        return switch (difficulty) {
            case EASY -> level == 2 ? 827 : 669;
            case NORMAL -> level == 2 ? 793 : 618;
            case HARD -> level == 2 ? 761 : 574;
        };
    }

    private static void assertPosition(Board board, int expectedX, int expectedY) {
        assertEquals(expectedX, board.x);
        assertEquals(expectedY, board.y);
    }

    private static void prepareActiveBlock(Board board, Block current, Block next,
            int[][] grid, int x, int y) throws Exception {
        board.timer.stop();
        set(board, "board", grid);
        set(board, "curr", current);
        set(board, "next", next);
        set(board, "score", 0);
        set(board, "erasedLineNum", 0);
        set(board, "level", 1);
        set(board, "isPaused", false);
        set(board, "anchorTouched", false);
        board.x = x;
        board.y = y;
        board.setText("");
        board.setText(EMPTY_BOARD_TEXT);
        invoke(board, "placeBlock");
    }

    private static void invoke(Object target, String name) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static <T> T field(Object target, String name, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }

    private static int intField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(target);
    }

    private static <T> T onEdt(Callable<T> action) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                result.set(action.call());
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });
        if (failure.get() != null) throw new AssertionError("EDT 검사 실패", failure.get());
        return result.get();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private enum AnimatedItem {
        ROW {
            @Override Block createBlock() { return new RowBlock(); }
        },
        COLUMN {
            @Override Block createBlock() { return new ColumnBlock(); }
        };

        abstract Block createBlock();
    }

    private static final class GameFixture implements AutoCloseable {
        final TestMaincontainer owner;
        final Gamepanel game;
        final Board board;
        final Timer initialTimer;

        GameFixture(Difficulty difficulty, String gameMode) throws Exception {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            owner = (TestMaincontainer) ((Unsafe) unsafeField.get(null))
                .allocateInstance(TestMaincontainer.class);
            owner.setting = owner.new Setting();
            owner.setDifficulty(difficulty);
            game = new Gamepanel(owner, gameMode);
            board = field(game, "board", Board.class);
            initialTimer = board.timer;
            initialTimer.stop();
        }

        JButton menuButton() {
            return Arrays.stream(game.getComponents())
                .filter(JButton.class::isInstance)
                .map(JButton.class::cast)
                .filter(button -> "MENU".equals(button.getText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("MENU 버튼을 찾을 수 없습니다."));
        }

        @Override
        public void close() {
            try {
                onEdt(() -> {
                    board.stopAnimations();
                    initialTimer.stop();
                    if (board.timer != null) board.timer.stop();
                    return null;
                });
            } catch (Exception failure) {
                throw new AssertionError("EDT 정리 실패", failure);
            }
        }
    }

    private static final class TestMaincontainer extends Maincontainer {
        private static final long serialVersionUID = 1L;
        boolean exitCalled;

        @Override
        protected void exitGameEnterStart() {
            exitCalled = true;
        }
    }
}
