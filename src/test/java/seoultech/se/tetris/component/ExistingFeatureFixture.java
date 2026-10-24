package seoultech.se.tetris.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import seoultech.se.tetris.Difficulty;
import seoultech.se.tetris.blocks.Block;
import sun.misc.Unsafe;

/** Real headless game components, with board state arranged for acceptance boundaries. */
final class ExistingFeatureFixture implements AutoCloseable {
    final Maincontainer owner;
    final Gamepanel game;
    final Board board;
    private final Timer initialTimer;

    ExistingFeatureFixture(Difficulty difficulty) throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        owner = (Maincontainer) ((Unsafe) unsafeField.get(null)).allocateInstance(Maincontainer.class);
        owner.setting = owner.new Setting();
        owner.setDifficulty(difficulty);
        game = new Gamepanel(owner, "Item");
        board = get(game, "board", Board.class);
        initialTimer = board.timer;
        initialTimer.stop();
    }

    void prepare(Block current, Block next, int[][] grid, int x, int y) throws Exception {
        board.stopAnimations();
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
        board.setText(("X".repeat(12) + "\n")
            + ("X" + " ".repeat(10) + "X\n").repeat(20) + "X".repeat(12));
        invoke(board, "placeBlock");
    }

    BufferedImage render(String filename) throws Exception {
        board.setSize(360, 640);
        BufferedImage image = new BufferedImage(360, 640, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            board.paint(graphics);
        } finally {
            graphics.dispose();
        }
        if (filename != null) {
            Path directory = Path.of("build", "reports", "req2", "rendering");
            Files.createDirectories(directory);
            ImageIO.write(image, "png", directory.resolve(filename).toFile());
        }
        return image;
    }

    // Pixel threshold is an automated visibility guard, not a human accessibility assessment.
    int contrastingPixelsInCell(BufferedImage image, int textColumn, int textRow) {
        FontMetrics metrics = board.getFontMetrics(new Font("Monospaced", Font.BOLD, 100));
        Insets insets = board.getInsets();
        int lineHeight = (int) (metrics.getHeight() * 0.8);
        double scaleX = (double) (board.getWidth() - insets.left - insets.right)
            / metrics.stringWidth(" ".repeat(12));
        double scaleY = (double) (board.getHeight() - insets.top - insets.bottom) / (lineHeight * 22);
        int left = insets.left + (int) Math.floor(textColumn * metrics.stringWidth(" ") * scaleX);
        int right = insets.left + (int) Math.ceil((textColumn + 1) * metrics.stringWidth(" ") * scaleX);
        int top = insets.top + (int) Math.floor(textRow * lineHeight * scaleY);
        int bottom = insets.top + (int) Math.ceil((textRow + 1) * lineHeight * scaleY);
        Color background = Maincontainer.changeColor(Color.BLACK);
        int count = 0;
        for (int y = Math.max(0, top); y < Math.min(image.getHeight(), bottom); y++) {
            for (int x = Math.max(0, left); x < Math.min(image.getWidth(), right); x++) {
                Color pixel = new Color(image.getRGB(x, y), true);
                int difference = Math.max(Math.abs(pixel.getRed() - background.getRed()),
                    Math.max(Math.abs(pixel.getGreen() - background.getGreen()),
                        Math.abs(pixel.getBlue() - background.getBlue())));
                if (difference >= 80) count++;
            }
        }
        return count;
    }

    static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    static <T> T get(Object target, String name, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }

    static Object invoke(Object target, String name) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(target);
    }

    static <T> T onEdt(Callable<T> action) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) return action.call();
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                result.set(action.call());
            } catch (Throwable thrown) {
                failure.set(thrown);
            }
        });
        if (failure.get() != null) throw new AssertionError("EDT acceptance check failed", failure.get());
        return result.get();
    }

    @Override
    public void close() {
        board.stopAnimations();
        initialTimer.stop();
        if (board.timer != null) board.timer.stop();
    }
}
