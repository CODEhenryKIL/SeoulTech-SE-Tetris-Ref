package seoultech.se.tetris.component;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import seoultech.se.tetris.blocks.*;
import sun.misc.Unsafe;

/**
 * 리팩토링 전후의 기존 동작을 확인하는 독립 실행 회귀 테스트. JDK 21 이상 필요.
 * 저장소 루트에서 실행(macOS/Linux):
 *
 *   javac --release 21 -d /tmp/tetris-tests $(find src -name '*.java') tests/RefactoringRegressionTest.java
 *   java -cp /tmp/tetris-tests seoultech.se.tetris.component.RefactoringRegressionTest
 *
 * 새 테스트 라이브러리 없이 실행하며, 각 검사는 임시 디렉터리/별도 JVM을 사용한다.
 * 실제 setting.txt와 tetris_score.txt는 읽거나 변경하지 않는다.
 *
 * 현재 UI와 게임 규칙이 결합되어 있어 private 상태 준비에는 reflection을 사용한다.
 * 창 없이 패널을 검사하기 위해 Maincontainer만 생성자를 우회한다(Unsafe).
 * 리팩토링으로 모델이 분리되면 아래 어댑터를 일반 생성자/공개 동작 호출로 교체한다.
 * 실제 창의 크기·포커스·페인트 결과, Windows 실행, 커버리지는 이 파일의 검증 범위 밖이다.
 */
public final class RefactoringRegressionTest {
    private static final String COMPONENT = "seoultech.se.tetris.component.RefactoringRegressionTest";
    private static final String MARKER = ".refactoring-test-sandbox";
    private static final String DEFAULT_KEYS = "ROTATE=38,DOWN=40,LEFT=37,RIGHT=39,PAUSE=80,HARD_DROP=32";
    private static final String CUSTOM_KEYS = "ROTATE=87,DOWN=40,LEFT=37,RIGHT=39,PAUSE=80,HARD_DROP=32";

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }

    private record Check(String id, String description, CheckedRunnable action) {}

    private static final List<Check> CHECKS = List.of(
        new Check("frame-size", "실제 창 크기와 게임 설정 크기의 독립성", RefactoringRegressionTest::frameSize),
        new Check("start-return", "게임·점수판 왕복 시 이전 패널 제거와 시작 메뉴 초기화", RefactoringRegressionTest::startReturn),
        new Check("key-change", "중복 키 거부 → 키 변경 → 저장·재로드", RefactoringRegressionTest::keyChange),
        new Check("reset-settings", "사용자 설정을 기본값으로 복원·저장", RefactoringRegressionTest::resetSettings),
        new Check("unknown-setting", "모르는 설정을 무시하고 정상 설정 유지", RefactoringRegressionTest::unknownSetting),
        new Check("settings-isolation", "설정 인스턴스와 기본값 간 독립성", RefactoringRegressionTest::settingsIsolation),
        new Check("block-rotation", "7종 블록의 칸 수·색·시계 방향 회전", RefactoringRegressionTest::blockRotation),
        new Check("block-generation", "실제 생성기의 7종 생성·시드 재현", RefactoringRegressionTest::blockGeneration),
        new Check("collision", "양쪽 벽·고정 블록·바닥 충돌", RefactoringRegressionTest::collision),
        new Check("blocked-rotation", "막힌 회전의 좌표·모양 유지", RefactoringRegressionTest::blockedRotation),
        new Check("hard-drop", "하드드롭 위치·점수·다음 블록 승계", RefactoringRegressionTest::hardDrop),
        new Check("line-clear", "한 줄·여러 줄 삭제와 기존 점수 규칙", RefactoringRegressionTest::lineClear),
        new Check("level-up", "누적 10줄 삭제 후 레벨·속도 변경", RefactoringRegressionTest::levelUp),
        new Check("pause-resume", "일시정지 중 입력 차단·타이머 재개", RefactoringRegressionTest::pauseResume),
        new Check("game-over", "생성 위치가 막히면 타이머 종료·중첩 방지", RefactoringRegressionTest::gameOver),
        new Check("score-save", "점수 저장 후 점수판에서 다시 읽기", RefactoringRegressionTest::scoreSave),
        new Check("score-ranking", "손상된 기록 무시·내림차순·상위 10개", RefactoringRegressionTest::scoreRanking),
        new Check("score-reset", "설정 메뉴의 기록 초기화·다시 읽기", RefactoringRegressionTest::scoreReset)
    );

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.exit(runAll());
        } else if (args.length == 3 && args[0].equals("--case")) {
            Path sandbox = Path.of(args[2]).toRealPath();
            require(sandbox.equals(Path.of("").toRealPath()) && Files.isRegularFile(sandbox.resolve(MARKER)),
                "개별 검사는 실행기가 만든 임시 폴더에서만 실행할 수 있습니다.");
            Check check = CHECKS.stream().filter(item -> item.id().equals(args[1])).findFirst().orElseThrow();
            try {
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        check.action().run();
                    } catch (Exception failure) {
                        throw new RuntimeException(failure);
                    }
                });
                System.exit(0);
            } catch (Throwable failure) {
                while (failure.getCause() != null &&
                        (failure instanceof InvocationTargetException || failure instanceof RuntimeException)) {
                    failure = failure.getCause();
                }
                failure.printStackTrace();
                System.exit(1);
            }
        } else {
            throw new IllegalArgumentException("인수 없이 실행하세요. --case는 내부 실행용입니다.");
        }
    }

    private static int runAll() throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin",
            File.separatorChar == '\\' ? "java.exe" : "java").toString();
        String classpath = String.join(File.pathSeparator, Arrays.stream(
            System.getProperty("java.class.path").split(Pattern.quote(File.pathSeparator)))
            .map(entry -> Path.of(entry).toAbsolutePath().toString()).toList());
        Path temporary = Files.createTempDirectory("tetris-refactoring-tests-");
        int failures = 0;
        try {
            for (Check check : CHECKS) {
                Path sandbox = Files.createDirectory(temporary.resolve(check.id()));
                Files.createFile(sandbox.resolve(MARKER));
                Path log = sandbox.resolve("result.log");
                Process child = new ProcessBuilder(java, "-Djava.awt.headless=true", "-cp", classpath,
                    COMPONENT, "--case", check.id(), sandbox.toString())
                    .directory(sandbox.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
                try {
                    if (!child.waitFor(20, TimeUnit.SECONDS)) {
                        child.destroyForcibly().waitFor();
                        failures++;
                        System.out.println("FAIL " + check.description() + " (20초 제한 초과)");
                    } else if (child.exitValue() != 0) {
                        failures++;
                        System.out.println("FAIL " + check.description());
                        System.out.print(Files.readString(log));
                    } else {
                        System.out.println("PASS " + check.description());
                    }
                } finally {
                    if (child.isAlive()) child.destroyForcibly().waitFor();
                }
            }
        } finally {
            try (var paths = Files.walk(temporary)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
        System.out.printf("RESULT %d/%d passed%n", CHECKS.size() - failures, CHECKS.size());
        return failures == 0 ? 0 : 1;
    }

    private static void frameSize() throws Exception {
        Maincontainer owner = owner("");
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);
        long widthOffset = unsafe.objectFieldOffset(java.awt.Component.class.getDeclaredField("width"));
        long heightOffset = unsafe.objectFieldOffset(java.awt.Component.class.getDeclaredField("height"));
        // 실제 창 없이 AWT의 실제 크기 필드에 테두리·제목 표시줄을 포함한 값을 준비한다.
        // 픽셀 도색은 별도의 실제 GUI 검증 대상이다.
        for (int[] size : new int[][] {{500, 600}, {625, 750}, {750, 900}}) {
            set(owner, "WIDTH", size[0]);
            set(owner, "HEIGHT", size[1]);
            unsafe.putInt(owner, widthOffset, size[0] + 8);
            unsafe.putInt(owner, heightOffset, size[1] + 28);
            require(owner.getWidth() == size[0] + 8, "창 너비는 설정값이 아닌 AWT의 실제 크기여야 합니다.");
            require(owner.getHeight() == size[1] + 28, "창 높이는 제목 표시줄을 포함한 실제 크기여야 합니다.");
            require(new Startpanel(owner).getPreferredSize().equals(new java.awt.Dimension(size[0], size[1])),
                "게임 화면의 설정 크기에 창 테두리·제목 표시줄 크기가 섞이면 안 됩니다.");
        }
    }

    private static void startReturn() throws Exception {
        Maincontainer owner = owner("");
        var layout = new java.awt.CardLayout();
        JPanel root = new JPanel(layout);
        set(owner, "cardLayout", layout);
        set(owner, "mainPanel", root);
        owner.enterStart();
        for (int i = 0; i < 3; i++) {
            Startpanel previous = (Startpanel) get(owner, "startPanel");
            press(previous, KeyEvent.VK_DOWN); // 복귀 시 이전 MENU 선택이 남으면 안 된다.
            owner.exitStartEnterScoreboard();
            JPanel scores = (JPanel) get(owner, "scoreboardPanel");
            owner.exitScoreboardEnterStart();
            require(root.getComponentCount() == 1 && scores.getParent() == null,
                "점수판 복귀 후 이전 패널이 남으면 안 됩니다.");
            require(get(owner, "startPanel") != previous, "시작 화면은 현재 정책대로 다시 생성해야 합니다.");

            Startpanel start = (Startpanel) get(owner, "startPanel");
            press(start, KeyEvent.VK_ENTER); // 새 메뉴의 기본 선택 START가 실제 게임을 열어야 한다.
            Gamepanel game = (Gamepanel) get(owner, "gamePanel");
            require(game != null && game.getParent() == root, "복귀한 메뉴의 START 입력으로 게임에 진입해야 합니다.");
            Board board = (Board) get(game, "board");
            board.timer.stop();
            owner.exitGameEnterStart();
            require(root.getComponentCount() == 1 && game.getParent() == null,
                "게임 복귀 후 이전 패널이 남으면 안 됩니다.");
            require(((JPanel) get(owner, "startPanel")).isVisible(), "복귀한 시작 화면이 표시돼야 합니다.");
        }
    }

    private static void keyChange() throws Exception {
        Maincontainer owner = owner("");
        Settingpanel panel = new Settingpanel(owner);
        panel.setSize(500, 600);
        call(panel, "selectDown");
        call(panel, "selectEnter");
        JPanel keyPanel = Arrays.stream(panel.getComponents())
            .filter(item -> item.getClass().getSimpleName().equals("ChangeKeyPanel"))
            .map(JPanel.class::cast).findFirst().orElseThrow();
        call(keyPanel, "selectEnter");
        press(keyPanel, KeyEvent.VK_DOWN); // 기존 DOWN 키와 중복되는 입력은 거부해야 한다.
        press(keyPanel, KeyEvent.VK_A);
        call(keyPanel, "selectUp"); // 첫 항목에서 Save and Exit으로 순환.
        call(keyPanel, "selectEnter");
        String expected = DEFAULT_KEYS.replace("ROTATE=38", "ROTATE=65");
        require(value(owner.setting, "keySetting").equals(expected), "변경하지 않은 키를 포함해 전체 저장 값이 일치해야 합니다.");
        owner.setting.exportSetting();
        require(value(owner.new Setting(), "keySetting").equals(expected), "다시 읽어도 키 설정이 유지되어야 합니다.");
        require(panel.getKeyListeners().length == 1, "키 설정 복귀 후 입력 리스너가 중복되지 않아야 합니다.");
    }

    private static void resetSettings() throws Exception {
        Maincontainer owner = owner("keySetting:" + CUSTOM_KEYS + "\nwindowSize:900X750\ncolorBlind:Tritanopia\n");
        require(value(owner.setting, "keySetting").equals(CUSTOM_KEYS), "초기화 전 사용자 키가 로드되어야 합니다.");
        Settingpanel panel = new Settingpanel(owner);
        for (int i = 0; i < 4; i++) call(panel, "selectDown");
        call(panel, "selectEnter"); // 실제 Reset setting 메뉴 실행.
        owner.setting.exportSetting();
        Maincontainer.Setting loaded = owner.new Setting();
        require(value(loaded, "keySetting").equals(DEFAULT_KEYS), "기본 키 복원 실패");
        require(value(loaded, "windowSize").equals("600X500"), "기본 창 크기 복원 실패");
        require(value(loaded, "colorBlind").equals("Normal"), "기본 색각 모드 복원 실패");
    }

    private static void unknownSetting() throws Exception {
        Maincontainer owner = owner("futureOption:anything\nwindowSize:900X750\ncolorBlind:Protanopia\nkeySetting:"
            + CUSTOM_KEYS + "\n");
        require(value(owner.setting, "windowSize").equals("900X750"), "정상 창 크기를 유지해야 합니다.");
        require(value(owner.setting, "colorBlind").equals("Protanopia"), "정상 색각 모드를 유지해야 합니다.");
        require(value(owner.setting, "keySetting").equals(CUSTOM_KEYS), "정상 키 설정을 유지해야 합니다.");
    }

    private static void settingsIsolation() throws Exception {
        Maincontainer owner = owner("");
        Maincontainer.Setting original = owner.setting;
        original.currentSettingMap.get("keySetting").modifySetting(CUSTOM_KEYS);
        Maincontainer.Setting other = owner.new Setting();
        require(value(other, "keySetting").equals(DEFAULT_KEYS), "다른 설정 인스턴스로 사용자 키가 전파되면 안 됩니다.");
        require(value(original, "keySetting").equals(CUSTOM_KEYS), "새 인스턴스가 기존 사용자 키를 바꾸면 안 됩니다.");
        original.clearSetting();
        require(value(original, "keySetting").equals(DEFAULT_KEYS), "기본값 목록이 오염되면 안 됩니다.");
    }

    private static void blockRotation() {
        for (Block block : blocks()) {
            String initial = shape(block);
            var color = block.getColor();
            for (int rotation = 0; rotation < 4; rotation++) {
                int occupied = 0;
                for (int y = 0; y < block.height(); y++) {
                    for (int x = 0; x < block.width(); x++) occupied += block.getShape(x, y);
                }
                require(occupied == 4, "각 블록은 회전 후에도 네 칸이어야 합니다.");
                require(color.equals(block.getColor()), "회전으로 색이 바뀌면 안 됩니다.");
                block.rotate();
            }
            require(shape(block).equals(initial), "네 번 회전하면 원래 모양으로 돌아와야 합니다.");
        }
        Block t = new TBlock();
        t.rotate();
        require(shape(t).equals("[[1, 0], [1, 1], [1, 0]]"), "T블록이 시계 방향으로 회전해야 합니다.");
    }

    private static void blockGeneration() throws Exception {
        try (GameFixture fixture = new GameFixture()) {
            Random random = (Random) get(fixture.board, "random");
            List<String> first = generatedTypes(fixture.board, random, 12345L);
            List<String> second = generatedTypes(fixture.board, random, 12345L);
            require(first.equals(second), "같은 시드에서 생성 순서가 재현되어야 합니다.");
            require(new HashSet<>(first).equals(Set.of("IBlock", "JBlock", "LBlock", "OBlock", "SBlock", "TBlock", "ZBlock")),
                "실제 생성기가 일곱 종류를 생성해야 합니다.");
        }
    }

    private static List<String> generatedTypes(Board board, Random random, long seed) throws Exception {
        random.setSeed(seed);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 210; i++) result.add(call(board, "getRandomBlock").getClass().getSimpleName());
        return result; // 통계적 분포/2차 난이도 가중치 테스트가 아니라 재현 가능한 생성 경로 검사.
    }

    private static void collision() throws Exception {
        try (GameFixture fixture = new GameFixture()) {
            Board board = fixture.board;
            prepare(board, new OBlock(), 0, 5);
            board.moveLeft();
            require(board.x == 0, "왼쪽 벽을 통과하면 안 됩니다.");
            prepare(board, new OBlock(), 8, 5);
            board.moveRight();
            require(board.x == 8, "오른쪽 벽을 통과하면 안 됩니다.");
            prepare(board, new OBlock(), 3, 5);
            fixedCell(board, 5, 5);
            board.moveRight();
            require(board.x == 3 && cells(board)[5][5] == 1, "고정 블록과 겹치면 안 됩니다.");
            prepare(board, new OBlock(), 3, 18);
            call(board, "eraseCurr");
            require(!board.canDown(3, 18), "바닥을 통과하면 안 됩니다.");
        }
    }

    private static void blockedRotation() throws Exception {
        try (GameFixture fixture = new GameFixture()) {
            Board board = fixture.board;
            Block current = new IBlock();
            prepare(board, current, 3, 4);
            for (int y = 0; y <= 8; y++) if (y != 4) fixedCell(board, 3, y);
            String before = shape(current);
            board.rotate();
            require(board.x == 3 && board.y == 4 && shape(current).equals(before), "막힌 회전은 좌표·모양을 유지해야 합니다.");
            for (int[] row : cells(board)) for (int cell : row) require(cell <= 1, "회전 후 블록 중첩이 없어야 합니다.");
        }
    }

    private static void hardDrop() throws Exception {
        try (GameFixture fixture = new GameFixture()) {
            Board board = fixture.board;
            prepare(board, new OBlock(), 3, 0);
            Block next = board.getNextBlock();
            board.hardDrop();
            require(board.getScore() == 36, "기존 하드드롭 규칙: 18칸 × 2점 = 36점");
            for (int y = 18; y < 20; y++) for (int x = 3; x < 5; x++) {
                require(cells(board)[y][x] == 1, "O블록이 최하단에 고정되어야 합니다.");
            }
            require(get(board, "curr") == next && board.x == 3 && board.y == 0, "미리보기 블록이 다음 현재 블록이 되어야 합니다.");
            require(board.getNextBlock() != null, "새 미리보기 블록이 생성되어야 합니다.");
        }
    }

    private static void lineClear() throws Exception {
        try (GameFixture fixture = new GameFixture()) {
            Board board = fixture.board;
            for (int count : new int[] {1, 2}) {
                prepare(board, new OBlock(), 3, 0);
                call(board, "eraseCurr");
                fixedCell(board, 1, 19 - count);
                for (int y = 20 - count; y < 20; y++) for (int x = 0; x < 10; x++) fixedCell(board, x, y);
                board.eraseLine();
                require(board.getScore() == (count == 1 ? 100 : 300), "기존 행 삭제·combo 점수를 유지해야 합니다.");
                require(cells(board)[19][1] == 1, "삭제한 행 위의 블록이 아래로 이동해야 합니다.");
                int occupied = Arrays.stream(cells(board)).flatMapToInt(Arrays::stream).sum();
                require(occupied == 1, "완성된 행이 모두 삭제되어야 합니다.");
                require((int) get(board, "erasedLineNum") == count, "삭제 행 누계가 정확해야 합니다.");
            }
        }
    }

    private static void levelUp() throws Exception {
        try (GameFixture fixture = new GameFixture()) {
            Board board = fixture.board;
            prepare(board, new OBlock(), 3, 0);
            call(board, "eraseCurr");
            set(board, "erasedLineNum", 9);
            for (int x = 0; x < 10; x++) fixedCell(board, x, 19);
            board.eraseLine();
            require((int) get(board, "level") == 2 && (int) get(board, "erasedLineNum") == 10, "10줄 경계에서 레벨이 한 번 올라야 합니다.");
            require(board.timer.getDelay() == 793, "현재 레벨 2의 낙하 간격 793ms를 유지해야 합니다.");
        }
    }

    private static void pauseResume() throws Exception {
        try (GameFixture fixture = new GameFixture()) {
            Board board = fixture.board;
            prepare(board, new OBlock(), 3, 5);
            fixture.initialTimer.start();
            press(fixture.game, Maincontainer.PAUSE);
            require(board.getIsPaused() && !board.timer.isRunning(), "일시정지 시 타이머가 멈춰야 합니다.");
            String before = Arrays.deepToString(cells(board));
            for (int key : new int[] {Maincontainer.DOWN, Maincontainer.LEFT, Maincontainer.RIGHT, Maincontainer.ROTATE, Maincontainer.HARD_DROP}) {
                press(fixture.game, key);
            }
            require(board.x == 3 && board.y == 5 && board.getScore() == 0 && Arrays.deepToString(cells(board)).equals(before),
                "일시정지 중 조작키로 게임 상태가 바뀌면 안 됩니다.");
            press(fixture.game, Maincontainer.PAUSE);
            require(!board.getIsPaused() && board.timer.isRunning(), "재개 시 타이머가 다시 실행되어야 합니다.");
        }
    }

    private static void gameOver() throws Exception {
        try (GameFixture fixture = new GameFixture()) {
            Board board = fixture.board;
            prepare(board, new OBlock(), 0, 18);
            set(board, "next", new OBlock());
            fixedCell(board, 3, 0);
            fixture.initialTimer.start();
            require(!board.moveDown(), "바닥에서 이동을 멈춰야 합니다.");
            require(board.timer == null && !fixture.initialTimer.isRunning() && get(fixture.game, "gameOverPanel") != null,
                "생성 위치가 막히면 실행 중인 타이머를 멈추고 게임 오버를 표시해야 합니다.");
            require(cells(board)[0][3] == 1, "게임 오버 후 다음 블록을 겹쳐 배치하면 안 됩니다.");
        }
    }

    private static void scoreSave() throws Exception {
        try (GameFixture fixture = new GameFixture()) {
            set(fixture.game, "usrname", "TESTPLAYER");
            fixture.board.addScore(321);
            call(fixture.game, "saveScore");
            require(Files.readString(Path.of("tetris_score.txt")).contains("TESTPLAYER:321"), "게임에서 점수를 저장해야 합니다.");
            String ranking = scoreText(new Scoreboardpanel(fixture.owner));
            require(ranking.contains("TESTPLAYER: 321"), "저장한 점수를 점수판에서 다시 읽어야 합니다.");
        }
    }

    private static void scoreRanking() throws Exception {
        StringBuilder content = new StringBuilder("Broken:no-number\n");
        for (int i = 0; i < 12; i++) content.append("P").append(i).append(":").append(i * 10).append('\n');
        Maincontainer owner = owner("");
        Files.writeString(Path.of("tetris_score.txt"), content);
        Scoreboardpanel panel = new Scoreboardpanel(owner);
        String text = scoreText(panel);
        require(text.lines().count() == 10, "상위 열 개 기록을 표시해야 합니다.");
        for (int i = 0; i < 10; i++) {
            String row = text.lines().toList().get(i);
            int score = 11 - i;
            require(row.equals((i + 1) + ".\tP" + score + ": " + score * 10), "내림차순 순위가 맞아야 합니다: " + row);
        }
        panel.drawScore();
        require(scoreText(panel).equals(text), "손상된 기록이 있어도 재조회 결과가 같아야 합니다.");
    }

    private static void scoreReset() throws Exception {
        Maincontainer owner = owner("");
        Files.writeString(Path.of("tetris_score.txt"), "Alice:200\nBob:100\n");
        Settingpanel panel = new Settingpanel(owner);
        call(panel, "selectDown");
        call(panel, "selectDown");
        call(panel, "selectEnter");
        require(Files.size(Path.of("tetris_score.txt")) == 0, "초기화한 점수 파일은 비어 있어야 합니다.");
        String text = scoreText(new Scoreboardpanel(owner));
        require(!text.contains("Alice") && !text.contains("Bob") && text.lines().count() == 10, "초기화 후 재조회하면 기존 기록 없이 빈 순위를 표시해야 합니다.");
    }

    // 아래는 현재 결합된 UI를 테스트하기 위한 준비/호출 어댑터다.
    private static Maincontainer owner(String settings) throws Exception {
        Files.writeString(Path.of("setting.txt"), settings);
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Maincontainer owner = (Maincontainer) ((Unsafe) field.get(null)).allocateInstance(Maincontainer.class);
        owner.setting = owner.new Setting();
        return owner;
    }

    private static final class GameFixture implements AutoCloseable {
        final Maincontainer owner;
        final Gamepanel game;
        final Board board;
        final Timer initialTimer;

        GameFixture() throws Exception {
            owner = owner("");
            game = new Gamepanel(owner);
            board = (Board) get(game, "board");
            initialTimer = board.timer;
            initialTimer.stop(); // EDT에서 생성 직후 중지: 실제 시간 경과에 의존하지 않음.
        }

        @Override public void close() {
            initialTimer.stop();
            if (board.timer != null) board.timer.stop();
        }
    }

    private static void prepare(Board board, Block current, int x, int y) throws Exception {
        set(board, "board", new int[20][10]);
        set(board, "curr", current);
        set(board, "next", new TBlock());
        set(board, "score", 0);
        set(board, "erasedLineNum", 0);
        set(board, "level", 1);
        board.x = x;
        board.y = y;
        board.setText("");
        board.setText("XXXXXXXXXXXX\n" + "X          X\n".repeat(20) + "XXXXXXXXXXXX");
        call(board, "placeBlock");
    }

    @SuppressWarnings("unchecked")
    private static void fixedCell(Board board, int x, int y) throws Exception {
        cells(board)[y][x] = 1;
        List<Board.TextChunk> chunks = (List<Board.TextChunk>) get(board, "textChunks");
        chunks.set((y + 1) * 13 + x + 1, board.new TextChunk("O", java.awt.Color.RED));
    }

    private static int[][] cells(Board board) throws Exception { return (int[][]) get(board, "board"); }
    private static String value(Maincontainer.Setting setting, String name) { return setting.currentSettingMap.get(name).getCurrentSetting(); }
    private static String scoreText(Scoreboardpanel panel) throws Exception { return ((JTextPane) get(panel, "scorepanel")).getText(); }

    private static List<Block> blocks() { return List.of(new IBlock(), new JBlock(), new LBlock(), new OBlock(), new SBlock(), new TBlock(), new ZBlock()); }

    private static String shape(Block block) {
        int[][] cells = new int[block.height()][block.width()];
        for (int y = 0; y < block.height(); y++) for (int x = 0; x < block.width(); x++) cells[y][x] = block.getShape(x, y);
        return Arrays.deepToString(cells);
    }

    private static void press(JPanel target, int code) {
        KeyEvent event = new KeyEvent(target, KeyEvent.KEY_PRESSED, 0, 0, code, KeyEvent.CHAR_UNDEFINED);
        for (KeyListener listener : target.getKeyListeners()) listener.keyPressed(event);
    }

    private static Object get(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object call(Object target, String name) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(target);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
