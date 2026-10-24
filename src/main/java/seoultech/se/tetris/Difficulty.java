package seoultech.se.tetris;

import java.util.Locale;
import java.util.Optional;

public enum Difficulty {
    EASY("Easy", 0.8),
    NORMAL("Normal", 1.0),
    HARD("Hard", 1.2);

    private final String displayName;
    private final double speedIncreaseFactor;

    Difficulty(String displayName, double speedIncreaseFactor) {
        this.displayName = displayName;
        this.speedIncreaseFactor = speedIncreaseFactor;
    }

    public String displayName() {
        return displayName;
    }

    /** Scale the increase in drops/second, preserving the initial one-second interval. */
    public int dropIntervalMillis(int level) {
        int cappedLevel = Math.max(1, Math.min(19, level));
        double normalSeconds = Math.pow(0.8 - (cappedLevel - 1) * 0.007, cappedLevel - 1);
        double speed = 1.0 + speedIncreaseFactor * (1.0 / normalSeconds - 1.0);
        return Math.max(1, (int) Math.round(1000.0 / speed));
    }

    public Difficulty previous() {
        Difficulty[] all = values();
        return all[(ordinal() + all.length - 1) % all.length];
    }

    public Difficulty next() {
        Difficulty[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public static Optional<Difficulty> parse(String value) {
        if (value == null) return Optional.empty();
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (Difficulty difficulty : values()) {
            if (difficulty.displayName.toLowerCase(Locale.ROOT).equals(normalized)) {
                return Optional.of(difficulty);
            }
        }
        return Optional.empty();
    }
}
