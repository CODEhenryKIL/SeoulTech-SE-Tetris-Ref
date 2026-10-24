package seoultech.se.tetris.component;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import seoultech.se.tetris.score.ScoreStore;

/** Real JFrame lifecycle with synthetic listener input; no OS installation/physical-key claim. */
public final class DesktopSmoke {
    private static Object field(Object object, String name) throws ReflectiveOperationException {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private static void key(Component component, int code) {
        KeyEvent event = new KeyEvent(component, KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(), 0, code, KeyEvent.CHAR_UNDEFINED);
        for (var listener : component.getKeyListeners()) listener.keyPressed(event);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        Path data = Files.createTempDirectory("tetris-desktop-data-");
        Path evidence = Path.of("build/reports/desktop");
        Files.createDirectories(evidence);
        System.setProperty("tetris.dataDir", data.toString());
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            Maincontainer frame = null;
            try {
                frame = new Maincontainer();
                frame.enterStart();
                check(frame.isShowing(), "real game window must be visible");
                check(frame.getContentPane().getWidth() > 0, "window must have a content area");
                JPanel start = (JPanel) field(frame, "startPanel");
                for (int turn = 0; turn < 40; turn++) key(start, KeyEvent.VK_DOWN);
                key(start, KeyEvent.VK_ENTER); // 40 is a multiple of the four menu choices.
                Gamepanel game = (Gamepanel) field(frame, "gamePanel");
                Board board = (Board) field(game, "board");
                board.timer.stop();
                int originalY = board.y;
                for (int turn = 0; turn < 5; turn++) key(game, Maincontainer.DOWN);
                check(board.y == originalY + 5 && board.getScore() == 5,
                    "repeated down input must move and score once per event");
                key(game, Maincontainer.PAUSE);
                int pausedY = board.y;
                for (int turn = 0; turn < 20; turn++) key(game, Maincontainer.DOWN);
                check(board.y == pausedY && board.getIsPaused(), "pause must block repeated movement");
                key(game, Maincontainer.PAUSE);
                board.timer.stop();
                check(!board.getIsPaused(), "pause key must resume the game");
                key(game, KeyEvent.VK_ESCAPE);
                check(game.getParent() == null && board.timer == null, "active game exit must remove game and timer");

                frame.exitStartEnterSetting();
                frame.setting.nextWindowSize();
                frame.setting.nextColorPalette();
                frame.setting.exportSetting();
                frame.exitSettingEnterStart();
                check(frame.getConfiguredHeight() == 750 && frame.getConfiguredWidth() == 625,
                    "changed window size must apply to the actual frame");
                check(frame.new Setting().getColorPalette().equals(frame.setting.getColorPalette()),
                    "saved settings must reload");
                ScoreStore.append("DESKTOP", 123);
                frame.exitStartEnterScoreboard();
                Scoreboardpanel scoreboard = (Scoreboardpanel) field(frame, "scoreboardPanel");
                check(((javax.swing.JTextPane) field(scoreboard, "scorepanel")).getText().contains("DESKTOP: 123"),
                    "saved score must be displayed");
                frame.validate();
                BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                frame.paint(graphics);
                graphics.dispose();
                ImageIO.write(image, "png", evidence.resolve("scoreboard.png").toFile());
                key(scoreboard, KeyEvent.VK_ENTER);
                check(scoreboard.getParent() == null, "scoreboard must return to menu");
                frame.exitStartEnterGame();
                game = (Gamepanel) field(frame, "gamePanel");
                board = (Board) field(game, "board");
                board.timer.stop();
                board.timer = null;
                board.addScore(321);
                game.gameOver();
                key(game, KeyEvent.VK_ENTER);
                scoreboard = (Scoreboardpanel) field(frame, "scoreboardPanel");
                check(scoreboard.isShowing(), "game-over name entry must open the actual scoreboard");
                check(((javax.swing.JTextPane) field(scoreboard, "scorepanel")).getText().contains("PLAYER: 321 [Normal]"),
                    "new score must include its difficulty");
                key(scoreboard, KeyEvent.VK_ENTER);
                ScoreStore.clear();
                for (int rank = 1; rank <= 12; rank++) ScoreStore.append("RANK" + rank, rank * 1000);
                frame.exitStartEnterGame();
                game = (Gamepanel) field(frame, "gamePanel");
                board = (Board) field(game, "board");
                board.timer.stop();
                board.timer = null;
                game.gameOver();
                scoreboard = (Scoreboardpanel) field(frame, "scoreboardPanel");
                check(scoreboard.isShowing() && game.getParent() == null,
                    "nonqualifying score must open rankings without name entry");
                check(ScoreStore.readRanked().size() == 12, "nonqualifying score must not append a record");
                key(scoreboard, KeyEvent.VK_ENTER);
                frame.setting.clearSetting();
                frame.setting.exportSetting();
                ScoreStore.clear();
                check(ScoreStore.readRanked().isEmpty(), "reset must remove saved scores");
                Files.writeString(evidence.resolve("result.txt"),
                    "PASS real JFrame, menu loop, repeated input, pause/resume, active exit, settings save/reload, ranking/reset, game-over to scoreboard\n"
                    + "OS=" + System.getProperty("os.name") + " " + System.getProperty("os.version")
                    + " arch=" + System.getProperty("os.arch") + " Java=" + System.getProperty("java.version") + "\n"
                    + "Input=synthetic KeyListener events; physical keyboard and clean target installation remain separate\n");
                System.out.println("DESKTOP SMOKE PASSED");
            } catch (Throwable problem) { failure.set(problem); }
            finally {
                for (java.awt.Window window : JFrame.getWindows()) window.dispose();
                if (frame != null) {
                    try {
                        Gamepanel game = (Gamepanel) field(frame, "gamePanel");
                        if (game != null) {
                            Board board = (Board) field(game, "board");
                            if (board.timer != null) board.timer.stop();
                            board.stopAnimations();
                        }
                    } catch (ReflectiveOperationException ignored) { }
                }
            }
        });
        if (failure.get() != null) throw new AssertionError("Desktop smoke failed", failure.get());
    }
}
