package seoultech.se.tetris.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seoultech.se.tetris.blocks.OBlock;
import seoultech.se.tetris.blocks.TBlock;
import sun.misc.Unsafe;

/** Requirement-focused boundary checks that do not duplicate the standalone suite. */
class RequirementsRegressionTest {
    private Board board;

    @AfterEach
    void stopTimer() {
        if (board != null && board.timer != null) {
            board.timer.stop();
        }
    }

    @Test
    @DisplayName("R13: 초기 자동 낙하 간격은 1초")
    void initialDropIntervalIsOneSecond() {
        board = new Board(null);

        assertEquals(1_000, board.timer.getInitialDelay());
        assertEquals(1_000, board.timer.getDelay());
        assertFalse(board.timer.isRunning(), "게임 시작 전에는 타이머가 실행되지 않아야 합니다.");
    }

    @Test
    @DisplayName("R14: 낙하 속도는 레벨 19까지 증가")
    void speedIncreasesThroughLevelNineteenAndThenStops() throws Exception {
        board = new Board(null);
        Method levelUp = Board.class.getDeclaredMethod("levelUp");
        levelUp.setAccessible(true);
        Field level = Board.class.getDeclaredField("level");
        level.setAccessible(true);

        int previousDelay = board.timer.getDelay();
        for (int expectedLevel = 2; expectedLevel <= 19; expectedLevel++) {
            levelUp.invoke(board);
            int currentDelay = board.timer.getDelay();
            assertEquals(expectedLevel, level.getInt(board));
            assertTrue(currentDelay <= previousDelay,
                "레벨 " + expectedLevel + "의 낙하 간격은 직전 레벨보다 길어지면 안 됩니다.");
            previousDelay = currentDelay;
        }

        levelUp.invoke(board);
        assertEquals(19, level.getInt(board));
        assertEquals(previousDelay, board.timer.getDelay(),
            "최대 레벨에서 추가 호출이 속도를 다시 바꾸면 안 됩니다.");
    }

    @Test
    @DisplayName("R24·R25: 자동·수동·하드드롭은 레벨별 칸당 점수가 같음")
    void everyDropInputUsesTheSamePerCellScoreAtEachSpeed() throws Exception {
        for (int level : new int[] {1, 2, 19}) {
            assertEquals(level, scoreOneCell(level, DropInput.AUTO), "자동 낙하 레벨 " + level);
            assertEquals(level, scoreOneCell(level, DropInput.MANUAL), "수동 낙하 레벨 " + level);
            assertEquals(level, scoreOneCell(level, DropInput.HARD_DROP), "하드드롭 레벨 " + level);
        }
    }

    private static int scoreOneCell(int level, DropInput input) throws Exception {
        AtomicInteger score = new AtomicInteger();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try (GameFixture fixture = new GameFixture()) {
                prepare(fixture.board, level);
                switch (input) {
                    case AUTO -> assertTrue(fixture.board.moveDown());
                    case MANUAL -> {
                        KeyEvent event = new KeyEvent(fixture.game, KeyEvent.KEY_PRESSED,
                            System.currentTimeMillis(), 0, Maincontainer.DOWN, KeyEvent.CHAR_UNDEFINED);
                        for (var listener : fixture.game.getKeyListeners()) {
                            listener.keyPressed(event);
                        }
                    }
                    case HARD_DROP -> fixture.board.hardDrop();
                }
                score.set(fixture.board.getScore());
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });
        if (failure.get() != null) {
            throw new AssertionError("낙하 입력 검사 준비 또는 실행 실패", failure.get());
        }
        return score.get();
    }

    private static void prepare(Board board, int level) throws Exception {
        set(board, "board", new int[20][10]);
        set(board, "curr", new OBlock());
        set(board, "next", new TBlock());
        set(board, "score", 0);
        set(board, "erasedLineNum", 0);
        set(board, "level", level);
        board.x = 3;
        board.y = 17;
        board.setText("");
        board.setText("XXXXXXXXXXXX\n" + "X          X\n".repeat(20) + "XXXXXXXXXXXX");
        Method placeBlock = Board.class.getDeclaredMethod("placeBlock");
        placeBlock.setAccessible(true);
        placeBlock.invoke(board);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private enum DropInput { AUTO, MANUAL, HARD_DROP }

    private static final class GameFixture implements AutoCloseable {
        final Gamepanel game;
        final Board board;
        final Timer initialTimer;

        GameFixture() throws Exception {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Maincontainer owner = (Maincontainer) ((Unsafe) unsafeField.get(null))
                .allocateInstance(Maincontainer.class);
            owner.setting = owner.new Setting();
            game = new Gamepanel(owner);
            Field boardField = Gamepanel.class.getDeclaredField("board");
            boardField.setAccessible(true);
            board = (Board) boardField.get(game);
            initialTimer = board.timer;
            initialTimer.stop();
        }

        @Override
        public void close() {
            initialTimer.stop();
            if (board.timer != null) {
                board.timer.stop();
            }
        }
    }
}
