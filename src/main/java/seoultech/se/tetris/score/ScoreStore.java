package seoultech.se.tetris.score;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import seoultech.se.tetris.storage.DataPaths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import seoultech.se.tetris.Difficulty;

/** Score-file operations; each read returns a fresh ranking for the caller to display. */
public final class ScoreStore {
    private static Path scoreFile() { return DataPaths.resolve("tetris_score.txt"); }

    private ScoreStore() {}

    public record Score(String username, int score, Difficulty difficulty) {
        public Score(String username, int score) {
            this(username, score, null);
        }

        public boolean hasDifficulty() {
            return difficulty != null;
        }
    }

    public static void append(String username, int score) {
        appendLine(username + ":" + score);
    }

    public static void append(String username, int score, Difficulty difficulty) {
        Difficulty selected = difficulty == null ? Difficulty.NORMAL : difficulty;
        appendLine(username + ":" + score + ":" + selected.displayName());
    }

    private static void appendLine(String line) {
        try {
            Path file = scoreFile();
            String prefix = "";
            if (Files.exists(file) && Files.size(file) > 0) {
                try (var channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
                    channel.position(channel.size() - 1);
                    java.nio.ByteBuffer last = java.nio.ByteBuffer.allocate(1);
                    channel.read(last);
                    byte value = last.array()[0];
                    if (value != '\n' && value != '\r') prefix = System.lineSeparator();
                }
            }
            Files.write(file, List.of(prefix + line),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Score> readRanked() {
        List<Score> scores = new ArrayList<>();
        if (Files.exists(scoreFile())) {
            try {
                List<String> lines = Files.readAllLines(scoreFile());
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    String[] tokens = line.split(":");
                    if (tokens.length == 2 || tokens.length == 3) {
                        try {
                            int score = Integer.parseInt(tokens[1].trim());
                            if (tokens.length == 2) {
                                scores.add(new Score(tokens[0].trim(), score));
                            } else {
                                Difficulty.parse(tokens[2]).ifPresent(difficulty ->
                                    scores.add(new Score(tokens[0].trim(), score, difficulty)));
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        scores.sort((first, second) -> Integer.compare(second.score(), first.score()));
        return scores;
    }

    public static void clear() {
        try {
            Files.write(scoreFile(), List.of());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
