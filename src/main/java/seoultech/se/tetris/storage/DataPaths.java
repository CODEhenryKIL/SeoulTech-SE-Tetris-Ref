package seoultech.se.tetris.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Packaged launchers keep writable data outside the application bundle. */
public final class DataPaths {
    private DataPaths() {}

    public static Path resolve(String fileName) {
        String configured = System.getProperty("tetris.dataDir");
        Path directory = configured != null && !configured.isBlank() ? Path.of(configured)
            : Boolean.getBoolean("tetris.packaged")
                ? Path.of(System.getProperty("user.home"), ".seoultech-tetris") : Path.of("");
        if (!directory.toString().isEmpty()) {
            try { Files.createDirectories(directory); }
            catch (IOException e) { throw new IllegalStateException("Cannot create data directory: " + directory, e); }
        }
        return directory.resolve(fileName);
    }
}
