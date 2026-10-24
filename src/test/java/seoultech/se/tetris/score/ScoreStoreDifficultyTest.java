package seoultech.se.tetris.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import seoultech.se.tetris.Difficulty;

class ScoreStoreDifficultyTest {
    private final String originalDataDir = System.getProperty("tetris.dataDir");

    @TempDir
    Path temporary;

    @AfterEach
    void restoreDataDirectory() {
        if (originalDataDir == null) System.clearProperty("tetris.dataDir");
        else System.setProperty("tetris.dataDir", originalDataDir);
    }

    @Test
    void legacyAndDifficultyRecordsRoundTripWithoutChangingTheLegacyFormat() throws Exception {
        System.setProperty("tetris.dataDir", temporary.toString());

        ScoreStore.append("Legacy", 10);
        ScoreStore.append("Ranked", 20, Difficulty.HARD);

        Path scoreFile = temporary.resolve("tetris_score.txt");
        assertEquals(List.of("Legacy:10", "Ranked:20:Hard"), Files.readAllLines(scoreFile));
        List<ScoreStore.Score> ranked = ScoreStore.readRanked();
        assertEquals("Ranked", ranked.get(0).username());
        assertEquals(Difficulty.HARD, ranked.get(0).difficulty());
        assertEquals("Legacy", ranked.get(1).username());
        assertNull(ranked.get(1).difficulty());
    }

    @Test
    void parserAcceptsOnlyKnownDifficultyMetadata() throws Exception {
        System.setProperty("tetris.dataDir", temporary.toString());
        Files.write(temporary.resolve("tetris_score.txt"), List.of(
            "Legacy:10",
            "EasyPlayer:30:Easy",
            "Invalid:99:Impossible"
        ));

        List<ScoreStore.Score> ranked = ScoreStore.readRanked();
        assertEquals(2, ranked.size());
        assertEquals("EasyPlayer", ranked.get(0).username());
        assertEquals(Difficulty.EASY, ranked.get(0).difficulty());
        assertEquals("Legacy", ranked.get(1).username());
        assertNull(ranked.get(1).difficulty());
    }

    @Test
    void difficultyAppendSeparatesAnUnterminatedLegacyRecord() throws Exception {
        System.setProperty("tetris.dataDir", temporary.toString());
        Path scoreFile = temporary.resolve("tetris_score.txt");
        Files.writeString(scoreFile, "Legacy:10");

        ScoreStore.append("Modern", 20, Difficulty.NORMAL);

        assertEquals("Legacy:10" + System.lineSeparator() + "Modern:20:Normal"
            + System.lineSeparator(), Files.readString(scoreFile));
    }
}
