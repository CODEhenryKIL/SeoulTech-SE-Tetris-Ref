package seoultech.se.tetris.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataPathsTest {
    private final String originalDataDir = System.getProperty("tetris.dataDir");
    private final String originalPackaged = System.getProperty("tetris.packaged");
    private final String originalUserHome = System.getProperty("user.home");
    private final String originalUserDir = System.getProperty("user.dir");

    @TempDir
    Path temporary;

    @AfterEach
    void restoreProperties() {
        restore("tetris.dataDir", originalDataDir);
        restore("tetris.packaged", originalPackaged);
        restore("user.home", originalUserHome);
        restore("user.dir", originalUserDir);
    }

    @Test
    void explicitDataDirectoryIsCreatedAndWritable() throws Exception {
        Path configured = temporary.resolve("explicit").resolve("nested");
        System.setProperty("tetris.dataDir", configured.toString());
        System.clearProperty("tetris.packaged");

        Path scoreFile = DataPaths.resolve("tetris_score.txt");
        assertEquals(configured.resolve("tetris_score.txt"), scoreFile);
        assertTrue(Files.isDirectory(configured));
        Files.writeString(scoreFile, "PLAYER:42\n");
        assertEquals("PLAYER:42\n", Files.readString(scoreFile));
    }

    @Test
    void packagedDataUsesUserHomeRegardlessOfWorkingDirectory() throws Exception {
        Path packagedHome = temporary.resolve("home");
        System.clearProperty("tetris.dataDir");
        System.setProperty("tetris.packaged", "true");
        System.setProperty("user.home", packagedHome.toString());
        System.setProperty("user.dir", temporary.resolve("unrelated-working-directory").toString());

        Path settingFile = DataPaths.resolve("setting.txt");
        Path expected = packagedHome.resolve(".seoultech-tetris").resolve("setting.txt");
        assertEquals(expected, settingFile);
        assertTrue(Files.isDirectory(expected.getParent()));
        Files.writeString(settingFile, "windowSize:600X500\n");
        assertEquals("windowSize:600X500\n", Files.readString(settingFile));
    }

    private static void restore(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
