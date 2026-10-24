package seoultech.se.tetris;

import java.util.Locale;
import java.util.Optional;

public enum Difficulty {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard");

    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
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
