package seoultech.se.tetris.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import seoultech.se.tetris.Difficulty;
import seoultech.se.tetris.GameMode;

class ScoreStoreModeTest {
    private final String originalDataDir = System.getProperty("tetris.dataDir");

    @TempDir
    Path temporary;

    @AfterEach
    void restoreDataDirectory() {
        if (originalDataDir == null) System.clearProperty("tetris.dataDir");
        else System.setProperty("tetris.dataDir", originalDataDir);
    }

    @Test
    void mixedLegacyAndModeRecordsReloadForAllAndFilterByMode() throws Exception {
        useTemporaryDataDirectory();
        Files.write(scoreFile(), List.of(
            "Legacy:90",
            "DifficultyOnly:80:Hard",
            "ClassicPlayer:70:Easy:Classic",
            "ItemPlayer:60:Normal:Item"
        ));

        List<ScoreStore.Score> all = ScoreStore.readRanked();
        assertEquals(List.of("Legacy", "DifficultyOnly", "ClassicPlayer", "ItemPlayer"),
            usernames(all));
        assertNull(all.get(0).mode());
        assertNull(all.get(1).mode());
        assertEquals(GameMode.CLASSIC, all.get(2).mode());
        assertEquals(GameMode.ITEM, all.get(3).mode());
        assertEquals(all, ScoreStore.readRanked(null));
        assertEquals(List.of("ClassicPlayer"), usernames(ScoreStore.readRanked(GameMode.CLASSIC)));
        assertEquals(List.of("ItemPlayer"), usernames(ScoreStore.readRanked(GameMode.ITEM)));
    }

    @Test
    void rankingKeepsFileOrderForEqualScoresWithinEachMode() throws Exception {
        useTemporaryDataDirectory();
        Files.write(scoreFile(), List.of(
            "ClassicFirst:100:Normal:Classic",
            "ItemFirst:100:Normal:Item",
            "ClassicSecond:100:Hard:Classic",
            "ItemSecond:100:Easy:Item"
        ));

        assertEquals(List.of("ClassicFirst", "ClassicSecond"),
            usernames(ScoreStore.readRanked(GameMode.CLASSIC)));
        assertEquals(List.of("ItemFirst", "ItemSecond"),
            usernames(ScoreStore.readRanked(GameMode.ITEM)));
    }

    @Test
    void invalidFourFieldRowsAreSkippedWithoutBlockingLegacySuffixesOrLaterRows()
            throws Exception {
        useTemporaryDataDirectory();
        Files.write(scoreFile(), List.of(
            "BadMode:500:Easy:Arcade",
            "BadDifficulty:400:Impossible:Classic",
            "EmptyDifficulty:350::Classic",
            "LegacySuffix:300::",
            "ExtraField:200:Easy:Classic:Unexpected",
            "ValidLater:100:Hard:Item"
        ));

        List<ScoreStore.Score> ranked = ScoreStore.readRanked();
        assertEquals(2, ranked.size());
        assertEquals("LegacySuffix", ranked.get(0).username());
        assertFalse(ranked.get(0).hasDifficulty());
        assertFalse(ranked.get(0).hasMode());
        assertEquals("ValidLater", ranked.get(1).username());
        assertEquals(Difficulty.HARD, ranked.get(1).difficulty());
        assertEquals(GameMode.ITEM, ranked.get(1).mode());
        assertEquals(List.of("ValidLater"), usernames(ScoreStore.readRanked(GameMode.ITEM)));
        assertTrue(ScoreStore.readRanked(GameMode.CLASSIC).isEmpty());
    }

    @Test
    void modeAppendUsesFourFieldsAndSeparatesAnUnterminatedLegacyRecord() throws Exception {
        useTemporaryDataDirectory();
        Files.writeString(scoreFile(), "Legacy:10");

        ScoreStore.append("Modern", 20, null, GameMode.CLASSIC);

        assertEquals("Legacy:10" + System.lineSeparator() + "Modern:20:Normal:Classic"
            + System.lineSeparator(), Files.readString(scoreFile()));
        assertThrows(NullPointerException.class,
            () -> ScoreStore.append("NoMode", 30, Difficulty.EASY, null));
    }

    @Test
    void clearRemovesEveryModeThenAllowsANewAppend() throws Exception {
        useTemporaryDataDirectory();
        ScoreStore.append("Classic", 30, Difficulty.NORMAL, GameMode.CLASSIC);
        ScoreStore.append("Item", 20, Difficulty.NORMAL, GameMode.ITEM);

        ScoreStore.clear();
        assertTrue(ScoreStore.readRanked().isEmpty());

        ScoreStore.append("Fresh", 10, Difficulty.EASY, GameMode.ITEM);
        assertEquals(List.of("Fresh"), usernames(ScoreStore.readRanked()));
        assertEquals(List.of("Fresh:10:Easy:Item"), Files.readAllLines(scoreFile()));
    }

    @Test
    void legacyConstructorsRemainCompatibleAndModeMetadataIsExplicit() {
        ScoreStore.Score legacy = new ScoreStore.Score("Legacy", 10);
        ScoreStore.Score difficultyOnly = new ScoreStore.Score("Difficulty", 20, Difficulty.HARD);
        ScoreStore.Score modern = new ScoreStore.Score(
            "Modern", 30, Difficulty.EASY, GameMode.CLASSIC);

        assertFalse(legacy.hasDifficulty());
        assertFalse(legacy.hasMode());
        assertTrue(difficultyOnly.hasDifficulty());
        assertFalse(difficultyOnly.hasMode());
        assertEquals(Difficulty.HARD, difficultyOnly.difficulty());
        assertTrue(modern.hasDifficulty());
        assertTrue(modern.hasMode());
        assertEquals(GameMode.CLASSIC, modern.mode());
    }

    @Test
    void gameModeParsingIsTrimmedCaseInsensitiveAndRejectsUnknownValues() {
        assertEquals(GameMode.CLASSIC, GameMode.parse("  cLaSsIc ").orElseThrow());
        assertEquals(GameMode.ITEM, GameMode.parse("ITEM").orElseThrow());
        assertTrue(GameMode.parse("Arcade").isEmpty());
        assertTrue(GameMode.parse(null).isEmpty());
    }

    private void useTemporaryDataDirectory() {
        System.setProperty("tetris.dataDir", temporary.toString());
    }

    private Path scoreFile() {
        return temporary.resolve("tetris_score.txt");
    }

    private static List<String> usernames(List<ScoreStore.Score> scores) {
        return scores.stream().map(ScoreStore.Score::username).toList();
    }
}
