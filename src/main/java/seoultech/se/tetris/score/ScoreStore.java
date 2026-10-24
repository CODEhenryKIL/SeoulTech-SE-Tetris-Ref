package seoultech.se.tetris.score;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Score-file operations; each read returns a fresh ranking for the caller to display. */
public final class ScoreStore {
    private static final Path SCORE_FILE = Path.of("tetris_score.txt");

    private ScoreStore() {}

    public record Score(String username, int score) {}

    public static void append(String username, int score) {
        try {
            Files.write(SCORE_FILE, List.of(username + ":" + score),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Score> readRanked() {
        List<Score> scores = new ArrayList<>();
        if (Files.exists(SCORE_FILE)) {
            try {
                List<String> lines = Files.readAllLines(SCORE_FILE);
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    String[] tokens = line.split(":");
                    if (tokens.length == 2) {
                        try {
                            scores.add(new Score(tokens[0].trim(), Integer.parseInt(tokens[1].trim())));
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
            Files.write(SCORE_FILE, List.of());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
