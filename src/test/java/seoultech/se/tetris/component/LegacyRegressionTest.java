package seoultech.se.tetris.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Runs the existing standalone suite without letting System.exit stop JUnit. */
class LegacyRegressionTest {
    @TempDir
    Path workspace;

    @Test
    @DisplayName("기존 독립 실행 회귀 검사")
    void existingRegressionChecks() throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin",
            File.separatorChar == '\\' ? "java.exe" : "java").toString();
        String classpath = System.getProperty("tetris.regression.classpath");
        assertTrue(classpath != null && !classpath.isBlank(),
            "Gradle test task must provide tetris.regression.classpath.");
        Path log = workspace.resolve("regression.log");
        Process process = new ProcessBuilder(java, "-Dfile.encoding=UTF-8", "-cp", classpath,
            RefactoringRegressionTest.class.getName())
            .directory(workspace.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            boolean completed = process.waitFor(5, TimeUnit.MINUTES);
            if (!completed) {
                terminate(process);
            }
            String output = Files.readString(log);
            System.out.print(output);
            assertTrue(completed, "회귀 검사 실행이 5분을 초과했습니다.\n" + output);
            assertEquals(0, process.exitValue(), output);
        } finally {
            if (process.isAlive()) {
                terminate(process);
            }
        }
    }

    private static void terminate(Process process) throws Exception {
        var descendants = process.descendants().toList();
        descendants.forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
        assertTrue(process.waitFor(10, TimeUnit.SECONDS), "회귀 검사 프로세스 종료 실패");
        for (ProcessHandle child : descendants) {
            child.onExit().get(10, TimeUnit.SECONDS);
        }
    }
}
