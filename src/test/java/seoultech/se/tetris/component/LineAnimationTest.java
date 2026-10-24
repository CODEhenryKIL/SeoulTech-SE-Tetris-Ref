package seoultech.se.tetris.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import sun.misc.Unsafe;

class LineAnimationTest {
    @Test
    @Timeout(5)
    @DisplayName("완성 행은 즉시 삭제되고 강조 표시는 210ms 뒤 종료")
    void completedLineIsRemovedBeforeAnimationEnds() throws Exception {
        GameFixture fixture = onEdt(GameFixture::new);
        try (fixture) {
            onEdt(() -> {
                prepareFullBottomRow(fixture.board);
                fixture.board.eraseLine();

                int[][] grid = field(fixture.board, "board", int[][].class);
                assertEquals(1, Arrays.stream(grid).flatMapToInt(Arrays::stream).sum());
                assertEquals(1, grid[19][2], "삭제 직후 윗행의 칸이 즉시 내려와야 합니다.");
                assertTrue(field(fixture.board, "clearingRows", BitSet.class).get(19));
                Timer animation = field(fixture.board, "clearAnimationTimer", Timer.class);
                assertTrue(animation.isRunning());
                assertGameTimerUnchanged(fixture);
                return null;
            });

            waitUntilAnimationEnds(fixture.board, Duration.ofSeconds(2));
            onEdt(() -> {
                assertTrue(field(fixture.board, "clearingRows", BitSet.class).isEmpty());
                assertNull(field(fixture.board, "clearAnimationTimer", Timer.class));
                assertGameTimerUnchanged(fixture);
                return null;
            });
        }
    }

    @Test
    @DisplayName("애니메이션 중단은 강조 상태만 즉시 정리")
    void stopAnimationsClearsOnlyAnimationState() throws Exception {
        GameFixture fixture = onEdt(GameFixture::new);
        try (fixture) {
            onEdt(() -> {
                prepareFullBottomRow(fixture.board);
                fixture.board.eraseLine();
                assertFalse(field(fixture.board, "clearingRows", BitSet.class).isEmpty());

                fixture.board.stopAnimations();

                assertTrue(field(fixture.board, "clearingRows", BitSet.class).isEmpty());
                assertNull(field(fixture.board, "clearAnimationTimer", Timer.class));
                assertGameTimerUnchanged(fixture);
                return null;
            });
        }
    }

    private static void prepareFullBottomRow(Board board) throws Exception {
        int[][] grid = new int[20][10];
        grid[18][2] = 1;
        Arrays.fill(grid[19], 1);
        set(board, "board", grid);
        set(board, "erasedLineNum", 0);
        set(board, "level", 1);
        set(board, "isPaused", false);
        board.timer.stop();
        board.timer.setDelay(777);
    }

    private static void assertGameTimerUnchanged(GameFixture fixture) throws Exception {
        assertSame(fixture.gameTimer, fixture.board.timer);
        assertEquals(777, fixture.board.timer.getDelay());
        assertFalse(fixture.board.timer.isRunning());
        assertEquals(1, intField(fixture.board, "level"));
        assertFalse(booleanField(fixture.board, "isPaused"));
    }

    private static void waitUntilAnimationEnds(Board board, Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (onEdt(() -> field(board, "clearingRows", BitSet.class).isEmpty()
                    && field(board, "clearAnimationTimer", Timer.class) == null)) return;
            Thread.sleep(20);
        }
        assertTrue(onEdt(() -> field(board, "clearingRows", BitSet.class).isEmpty()),
            "줄 삭제 강조가 제한 시간 안에 끝나야 합니다.");
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

    private static boolean booleanField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.getBoolean(target);
    }

    private static final class GameFixture implements AutoCloseable {
        final Board board;
        final Timer gameTimer;

        GameFixture() throws Exception {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Maincontainer owner = (Maincontainer) ((Unsafe) unsafeField.get(null))
                .allocateInstance(Maincontainer.class);
            owner.setting = owner.new Setting();
            Gamepanel game = new Gamepanel(owner);
            board = field(game, "board", Board.class);
            gameTimer = board.timer;
            gameTimer.stop();
        }

        @Override
        public void close() {
            try {
                onEdt(() -> {
                    board.stopAnimations();
                    gameTimer.stop();
                    if (board.timer != null) board.timer.stop();
                    return null;
                });
            } catch (Exception failure) {
                throw new AssertionError("EDT 정리 실패", failure);
            }
        }
    }
}
