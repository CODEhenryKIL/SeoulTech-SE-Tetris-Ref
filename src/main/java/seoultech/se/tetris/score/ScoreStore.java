package seoultech.se.tetris.score;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import seoultech.se.tetris.storage.DataPaths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import seoultech.se.tetris.Difficulty;
import seoultech.se.tetris.GameMode;

/** Score-file operations; each read returns a fresh ranking for the caller to display. */
public final class ScoreStore {
    private static Path scoreFile() { return DataPaths.resolve("tetris_score.txt"); }

    private ScoreStore() {}

    public record Score(String username, int score, Difficulty difficulty, GameMode mode) {
        public Score(String username, int score) {
            this(username, score, null, null);
        }

        public Score(String username, int score, Difficulty difficulty) {
            this(username, score, difficulty, null);
        }

        public boolean hasDifficulty() {
            return difficulty != null;
        }

        public boolean hasMode() {
            return mode != null;
        }
    }

    public static void append(String username, int score) {
        appendLine(username + ":" + score);
    }

    public static void append(String username, int score, Difficulty difficulty) {
        Difficulty selected = difficulty == null ? Difficulty.NORMAL : difficulty;
        appendLine(username + ":" + score + ":" + selected.displayName());
    }

    public static void append(String username, int score, Difficulty difficulty, GameMode mode) {
        Difficulty selected = difficulty == null ? Difficulty.NORMAL : difficulty;
        GameMode selectedMode = Objects.requireNonNull(mode, "mode");
        appendLine(username + ":" + score + ":" + selected.displayName() + ":"
            + selectedMode.displayName());
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
        return readRanked(null);
    }

    public static List<Score> readRanked(GameMode mode) {
        List<Score> scores = new ArrayList<>();
        if (Files.exists(scoreFile())) {
            try {
                List<String> lines = Files.readAllLines(scoreFile());
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    String[] tokens = line.split(":");
                    if (tokens.length >= 2 && tokens.length <= 4) {
                        try {
                            int score = Integer.parseInt(tokens[1].trim());
                            if (tokens.length == 2) {
                                addIfModeMatches(scores,
                                    new Score(tokens[0].trim(), score), mode);
                            } else if (tokens.length == 3) {
                                Difficulty.parse(tokens[2]).ifPresent(difficulty ->
                                    addIfModeMatches(scores,
                                        new Score(tokens[0].trim(), score, difficulty), mode));
                            } else {
                                Difficulty.parse(tokens[2]).ifPresent(difficulty ->
                                    GameMode.parse(tokens[3]).ifPresent(recordMode ->
                                        addIfModeMatches(scores, new Score(tokens[0].trim(),
                                            score, difficulty, recordMode), mode)));
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

    private static void addIfModeMatches(List<Score> scores, Score score, GameMode mode) {
        if (mode == null || score.mode() == mode) scores.add(score);
    }

    public static void clear() {
        try {
            Files.write(scoreFile(), List.of());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
