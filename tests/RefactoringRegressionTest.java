package seoultech.se.tetris.component;

import java.awt.Container;
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
import javax.swing.JLabel;
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
    private static final String[] START_LABELS = {"START", "MENU", "SCORE", "EXIT"};
    private static final String[] SETTING_LABELS = {"Window Option: ", "Change Keys", "Reset Scoreboard", "Color Option: ", "Reset setting", "Save and Exit"};
    private static final String[] KEY_LABELS = {"Rotate: ", "Down: ", "Left: ", "Right: ", "Pause: ", "Hard Drop: ", "Save and Exit"};

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }

    private record Check(String id, String description, CheckedRunnable action) {}

    private static final List<Check> CHECKS = List.of(
        new Check("frame-size", "실제 창 크기와 게임 설정 크기의 독립성", RefactoringRegressionTest::frameSize),
        new Check("start-return", "게임·점수판 왕복 시 이전 패널 제거와 시작 메뉴 초기화", RefactoringRegressionTest::startReturn),
        new Check("menu-navigation", "세 메뉴의 방향키 순환·강조 및 MENU/SCORE 실행", RefactoringRegressionTest::menuNavigation),
        new Check("menu-round-trip", "키 설정 반복 왕복 시 선택 유지·리스너 복원", RefactoringRegressionTest::menuRoundTrip),
        new Check("key-input-mode", "키 입력 대기 중 방향키·예약 키·중복 키 처리", RefactoringRegressionTest::keyInputMode),
        new Check("menu-capacity", "기존 메뉴 등록 상한 11개와 순환 범위", RefactoringRegressionTest::menuCapacity),
        new Check("start-menu-isolation", "시작 화면 인스턴스 간 선택 독립성", RefactoringRegressionTest::startMenuIsolation),
        new Check("setting-menu-isolation", "설정 화면 인스턴스 간 선택 독립성", RefactoringRegressionTest::settingMenuIsolation),
        new Check("key-menu-isolation", "키 변경 화면 인스턴스 간 선택 독립성", RefactoringRegressionTest::keyMenuIsolation),
        new Check("key-change", "중복 키 거부 → 키 변경 → 저장·재로드", RefactoringRegressionTest::keyChange),
        new Check("reset-settings", "사용자 설정을 기본값으로 복원·저장", RefactoringRegressionTest::resetSettings),
        new Check("unknown-setting", "모르는 설정을 무시하고 정상 설정 유지", RefactoringRegressionTest::unknownSetting),
        new Check("settings-isolation", "설정 인스턴스와 기본값 간 독립성", RefactoringRegressionTest::settingsIsolation),
        new Check("key-file-validation", "정상·손상·5키 파일의 기존 허용 및 거절 규칙", RefactoringRegressionTest::keyFileValidation),
        new Check("key-file-round-trip", "허용되는 키 문자열의 원형 유지와 여섯 키 재로드", RefactoringRegressionTest::keyFileRoundTrip),
        new Check("key-edit-staging", "키 편집·서브메뉴 저장·파일 저장의 시점 분리", RefactoringRegressionTest::keyEditStaging),
        new Check("setting-option-cycle", "창 크기·색상 전체 순환 및 저장·재로드", RefactoringRegressionTest::settingOptionCycle),
        new Check("setting-file-boundaries", "누락·빈 파일·알 수 없는 값·공백 처리", RefactoringRegressionTest::settingFileBoundaries),
        new Check("key-array-ownership", "설정 조회·저장 배열과 내부 값의 독립성", RefactoringRegressionTest::keyArrayOwnership),
        new Check("block-initialization", "7종 블록의 초기 모양 전체·색상", RefactoringRegressionTest::blockInitialization),
        new Check("block-rotation", "7종 블록의 칸 수·색·시계 방향 회전", RefactoringRegressionTest::blockRotation),
        new Check("block-generation", "실제 생성기의 7종 생성·시드 재현", RefactoringRegressionTest::blockGeneration),
        new Check("collision", "양쪽 벽·고정 블록·바닥 충돌", RefactoringRegressionTest::collision),
        new Check("blocked-rotation", "막힌 회전의 좌표·모양 유지", RefactoringRegressionTest::blockedRotation),
        new Check("hard-drop", "하드드롭 위치·점수·다음 블록 승계", RefactoringRegressionTest::hardDrop),
        new Check("drop-input-score", "자동·수동 낙하와 하드드롭의 기존 점수·이동", RefactoringRegressionTest::dropInputScore),
        new Check("lock-advance", "다섯 입력 경로의 바닥·고정 블록 위 고정과 미리보기 승계", RefactoringRegressionTest::lockAdvance),
        new Check("lock-line-level", "두 낙하의 실제 고정을 통한 두 줄 삭제·레벨 경계", RefactoringRegressionTest::lockLineLevel),
        new Check("lock-game-over", "고정 후 게임 오버 시 승계·난수 진행 없이 즉시 중단", RefactoringRegressionTest::lockGameOver),
        new Check("lock-clear-before-game-over", "줄 삭제로 생성 자리가 비면 게임 오버 없이 다음 블록 배치", RefactoringRegressionTest::lockClearBeforeGameOver),
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

    private static void menuNavigation() throws Exception {
        Maincontainer owner = owner("");
        var layout = new java.awt.CardLayout();
        JPanel root = new JPanel(layout);
        set(owner, "cardLayout", layout);
        set(owner, "mainPanel", root);
        owner.enterStart();
        Startpanel start = (Startpanel) get(owner, "startPanel");
        checkNavigation(start, menuButtons(start, START_LABELS));
        press(start, KeyEvent.VK_DOWN);
        press(start, KeyEvent.VK_DOWN);
        press(start, KeyEvent.VK_ENTER);
        require(((JPanel) get(owner, "scoreboardPanel")).getParent() == root, "SCORE에서 Enter를 누르면 점수판이 열려야 합니다.");
        owner.exitScoreboardEnterStart();
        start = (Startpanel) get(owner, "startPanel");
        press(start, KeyEvent.VK_DOWN);
        press(start, KeyEvent.VK_ENTER);
        Settingpanel setting = (Settingpanel) get(owner, "settingPanel");
        require(setting.getParent() == root, "MENU에서 Enter를 누르면 설정이 열려야 합니다.");
        setting.setSize(500, 600);
        checkNavigation(setting, menuButtons(setting, SETTING_LABELS));
        press(setting, KeyEvent.VK_DOWN);
        press(setting, KeyEvent.VK_ENTER);
        JPanel keys = keyPanel(setting);
        checkNavigation(keys, menuButtons(keys, KEY_LABELS));
    }

    private static void menuRoundTrip() throws Exception {
        Settingpanel setting = new Settingpanel(owner(""));
        setting.setSize(500, 600);
        List<JPanel> options = menuButtons(setting, SETTING_LABELS);
        press(setting, KeyEvent.VK_DOWN);
        for (int round = 0; round < 3; round++) {
            press(setting, KeyEvent.VK_ENTER);
            JPanel keys = keyPanel(setting);
            require(setting.getKeyListeners().length == 0 && keys.getKeyListeners().length == 1,
                "키 변경 중에는 상위 메뉴가 같은 입력을 처리하면 안 됩니다.");
            assertSelection(menuButtons(keys, KEY_LABELS), 0);
            press(keys, KeyEvent.VK_UP);
            press(keys, KeyEvent.VK_ENTER);
            require(keys.getParent() == null && setting.getKeyListeners().length == 1,
                "복귀하면 키 변경 창은 제거되고 상위 리스너는 하나여야 합니다.");
            assertSelection(options, 1);
        }
        press(setting, KeyEvent.VK_UP);
        assertSelection(options, 0);
    }

    private static void keyInputMode() throws Exception {
        Maincontainer owner = owner("");
        Settingpanel setting = new Settingpanel(owner);
        setting.setSize(500, 600);
        press(setting, KeyEvent.VK_DOWN);
        press(setting, KeyEvent.VK_ENTER);
        JPanel keys = keyPanel(setting);
        List<JPanel> options = menuButtons(keys, KEY_LABELS);
        press(keys, KeyEvent.VK_ENTER);
        press(keys, KeyEvent.VK_DOWN);
        assertSelection(options, 0);
        require(((Integer[]) get(keys, "currentKey"))[0] == KeyEvent.VK_UP && hasLabel(keys, "Please Enter an Unused Key"),
            "중복 방향키는 메뉴를 이동시키거나 기존 키를 변경하면 안 됩니다.");
        press(keys, KeyEvent.VK_V);
        for (int i = 0; i < 4; i++) press(keys, KeyEvent.VK_DOWN);
        assertSelection(options, 4);
        press(keys, KeyEvent.VK_ENTER);
        press(keys, KeyEvent.VK_ENTER);
        require(((Integer[]) get(keys, "currentKey"))[4] == KeyEvent.VK_P && hasLabel(keys, "Enter Is Reserved for Exit"),
            "PAUSE의 Enter 예약 규칙을 유지해야 합니다.");
        press(keys, KeyEvent.VK_P); // 현재와 같은 키는 허용하고 메뉴 이동 모드로 복귀.
        require(keys.getKeyListeners().length == 1 && !hasLabel(keys, "Press Key to Change"),
            "키 입력을 마치면 메뉴 리스너 하나만 복원해야 합니다.");
        press(keys, KeyEvent.VK_DOWN);
        assertSelection(options, 5);
        press(keys, KeyEvent.VK_ENTER);
        press(keys, KeyEvent.VK_RIGHT);
        assertSelection(options, 5);
        require(((Integer[]) get(keys, "currentKey"))[5] == KeyEvent.VK_SPACE,
            "하드드롭 키 변경 시에도 중복 키를 거부해야 합니다.");
        press(keys, KeyEvent.VK_B);
        press(keys, KeyEvent.VK_DOWN);
        press(keys, KeyEvent.VK_ENTER);
        String expected = DEFAULT_KEYS.replace("ROTATE=38", "ROTATE=86").replace("HARD_DROP=32", "HARD_DROP=66");
        require(value(owner.setting, "keySetting").equals(expected), "선택한 Rotate·Hard Drop 키만 변경돼야 합니다.");
        owner.setting.exportSetting();
        require(value(owner.new Setting(), "keySetting").equals(expected), "여섯 키의 저장·재로드가 유지돼야 합니다.");
    }

    private static void startMenuIsolation() throws Exception {
        Maincontainer owner = owner("");
        var layout = new java.awt.CardLayout();
        JPanel root = new JPanel(layout);
        set(owner, "cardLayout", layout);
        set(owner, "mainPanel", root);
        Startpanel first = new Startpanel(owner);
        set(owner, "startPanel", first);
        root.add(first, "StartScreen");
        List<JPanel> firstButtons = menuButtons(first, START_LABELS);
        press(first, KeyEvent.VK_DOWN);
        Startpanel second = new Startpanel(owner);
        List<JPanel> secondButtons = menuButtons(second, START_LABELS);
        press(first, KeyEvent.VK_DOWN);
        assertSelection(firstButtons, 2);
        assertSelection(secondButtons, 0);
        press(first, KeyEvent.VK_ENTER);
        require(((JPanel) get(owner, "scoreboardPanel")).getParent() == root, "첫 화면의 Enter가 첫 화면에서 선택한 SCORE를 실행해야 합니다.");
    }

    private static void settingMenuIsolation() throws Exception {
        Maincontainer owner = owner("");
        Settingpanel first = new Settingpanel(owner);
        List<JPanel> firstButtons = menuButtons(first, SETTING_LABELS);
        press(first, KeyEvent.VK_DOWN);
        Maincontainer secondOwner = owner("");
        Settingpanel second = new Settingpanel(secondOwner);
        List<JPanel> secondButtons = menuButtons(second, SETTING_LABELS);
        press(first, KeyEvent.VK_DOWN);
        assertSelection(firstButtons, 2);
        assertSelection(secondButtons, 0);
        press(first, KeyEvent.VK_UP);
        press(first, KeyEvent.VK_UP);
        press(first, KeyEvent.VK_ENTER);
        require(value(owner.setting, "windowSize").equals("750X625") && value(secondOwner.setting, "windowSize").equals("600X500"),
            "첫 화면의 Enter가 다른 화면의 설정을 바꾸면 안 됩니다.");
    }

    private static void keyMenuIsolation() throws Exception {
        Maincontainer owner = owner("");
        Settingpanel first = new Settingpanel(owner);
        first.setSize(500, 600);
        press(first, KeyEvent.VK_DOWN);
        press(first, KeyEvent.VK_ENTER);
        JPanel firstKeys = keyPanel(first);
        List<JPanel> firstButtons = menuButtons(firstKeys, KEY_LABELS);
        press(firstKeys, KeyEvent.VK_DOWN);
        press(firstKeys, KeyEvent.VK_DOWN);
        Settingpanel second = new Settingpanel(owner);
        second.setSize(500, 600);
        press(second, KeyEvent.VK_DOWN);
        press(second, KeyEvent.VK_ENTER);
        JPanel secondKeys = keyPanel(second);
        List<JPanel> secondButtons = menuButtons(secondKeys, KEY_LABELS);
        press(firstKeys, KeyEvent.VK_DOWN);
        assertSelection(firstButtons, 3);
        assertSelection(secondButtons, 0);
        press(firstKeys, KeyEvent.VK_ENTER);
        press(firstKeys, KeyEvent.VK_B);
        require(((Integer[]) get(firstKeys, "currentKey"))[3] == KeyEvent.VK_B
                && ((Integer[]) get(secondKeys, "currentKey"))[3] == KeyEvent.VK_RIGHT,
            "첫 키 변경 화면의 입력이 다른 키 변경 화면에 적용되면 안 됩니다.");
    }

    @SuppressWarnings("unchecked")
    private static void menuCapacity() throws Exception {
        Startpanel panel = new Startpanel(owner(""));
        JPanel parent = (JPanel) menuButtons(panel, START_LABELS).get(0).getParent();
        Class<?> type = Arrays.stream(Startpanel.class.getDeclaredClasses())
            .filter(item -> item.getSimpleName().equals("Button")).findFirst().orElseThrow();
        var constructor = type.getDeclaredConstructor(Startpanel.class, String.class);
        constructor.setAccessible(true);
        MenuButtons<JPanel> menu = (MenuButtons<JPanel>) get(panel, "menu");
        JPanel rejected = null;
        for (int index = 4; index < 12; index++) {
            JPanel button = (JPanel) constructor.newInstance(panel, "EXTRA " + index);
            menu.add(parent, button);
            rejected = button;
        }
        require(parent.getComponentCount() == 11 && rejected.getParent() == null,
            "기존 동작대로 11개를 등록하고 12번째부터 등록하지 않아야 합니다.");
        List<JPanel> buttons = Arrays.stream(parent.getComponents()).map(JPanel.class::cast).toList();
        assertSelection(buttons, 0);
        press(panel, KeyEvent.VK_UP);
        assertSelection(buttons, 10);
        press(panel, KeyEvent.VK_DOWN);
        assertSelection(buttons, 0);
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
        original.updateKeyCodes(new Integer[] {87, 40, 37, 39, 80, 32});
        Maincontainer.Setting other = owner.new Setting();
        require(value(other, "keySetting").equals(DEFAULT_KEYS), "다른 설정 인스턴스로 사용자 키가 전파되면 안 됩니다.");
        require(value(original, "keySetting").equals(CUSTOM_KEYS), "새 인스턴스가 기존 사용자 키를 바꾸면 안 됩니다.");
        original.clearSetting();
        require(value(original, "keySetting").equals(DEFAULT_KEYS), "기본값 목록이 오염되면 안 됩니다.");
    }

    private static void keyFileValidation() throws Exception {
        List<String> invalid = List.of(
            "", DEFAULT_KEYS.substring(0, DEFAULT_KEYS.lastIndexOf(',')),
            DEFAULT_KEYS + ",EXTRA=65", DEFAULT_KEYS + ",", DEFAULT_KEYS.replace(",DOWN", ",,DOWN"),
            DEFAULT_KEYS.replace("ROTATE=38", "TURN=38"),
            DEFAULT_KEYS.replace("ROTATE=38,DOWN=40", "DOWN=40,ROTATE=38"),
            DEFAULT_KEYS.replace("ROTATE=38", "ROTATE="),
            DEFAULT_KEYS.replace("ROTATE=38", "ROTATE=38=65"),
            DEFAULT_KEYS.replace("ROTATE=38", "ROTATE=abc"),
            DEFAULT_KEYS.replace("ROTATE=38", "ROTATE=2147483648"),
            DEFAULT_KEYS.replace("ROTATE=38", "ROTATE=0"),
            DEFAULT_KEYS.replace("ROTATE=38", "ROTATE=-1"),
            DEFAULT_KEYS.replace("ROTATE=38", "ROTATE= 38"),
            DEFAULT_KEYS.replace("ROTATE=38", "ROTATE=40"),
            DEFAULT_KEYS.replace("PAUSE=80", "PAUSE=10"));
        for (String keys : invalid) {
            Maincontainer owner = owner("windowSize:900X750\ncolorBlind:Tritanopia\nkeySetting:" + keys + "\n");
            require(value(owner.setting, "keySetting").equals(DEFAULT_KEYS), "잘못된 키 파일은 기본 키를 유지해야 합니다: " + keys);
            require(value(owner.setting, "windowSize").equals("900X750")
                    && value(owner.setting, "colorBlind").equals("Tritanopia"), "잘못된 키가 다른 정상 설정을 지우면 안 됩니다.");
        }
        Maincontainer owner = owner("keySetting:" + CUSTOM_KEYS + "\nkeySetting:broken\n");
        require(value(owner.setting, "keySetting").equals(CUSTOM_KEYS), "뒤의 잘못된 줄이 앞의 정상 키를 덮어쓰면 안 됩니다.");
    }

    private static void keyFileRoundTrip() throws Exception {
        for (String keys : List.of(CUSTOM_KEYS,
                "ROTATE=65,DOWN=83,LEFT=68,RIGHT=70,PAUSE=71,HARD_DROP=72",
                DEFAULT_KEYS.replace("HARD_DROP=32", "HARD_DROP=10"),
                DEFAULT_KEYS.replace("ROTATE=38", "ROTATE=+0038"))) {
            Maincontainer owner = owner("keySetting:" + keys + "\n");
            require(value(owner.setting, "keySetting").equals(keys), "허용된 파일의 키 문자열을 임의로 변경하면 안 됩니다.");
            owner.setting.exportSetting();
            require(value(owner.new Setting(), "keySetting").equals(keys), "저장·재로드 후 허용된 키 문자열이 유지되어야 합니다.");
            Settingpanel settings = new Settingpanel(owner);
            press(settings, KeyEvent.VK_DOWN);
            press(settings, KeyEvent.VK_ENTER);
            Integer[] actual = (Integer[]) get(keyPanel(settings), "currentKey");
            String[] parts = keys.split(",");
            for (int i = 0; i < parts.length; i++) {
                require(actual[i] == Integer.parseInt(parts[i].split("=")[1]), "여섯 키의 순서와 번호를 보존해야 합니다.");
            }
        }
    }

    private static void keyEditStaging() throws Exception {
        Maincontainer owner = owner("keySetting:" + DEFAULT_KEYS + "\n");
        byte[] originalFile = Files.readAllBytes(Path.of("setting.txt"));
        Settingpanel settings = new Settingpanel(owner);
        press(settings, KeyEvent.VK_DOWN);
        press(settings, KeyEvent.VK_ENTER);
        JPanel keys = keyPanel(settings);
        press(keys, KeyEvent.VK_ENTER);
        press(keys, KeyEvent.VK_A);
        require(value(owner.setting, "keySetting").equals(DEFAULT_KEYS), "편집 중에는 저장된 키를 변경하면 안 됩니다.");
        Settingpanel another = new Settingpanel(owner);
        press(another, KeyEvent.VK_DOWN);
        press(another, KeyEvent.VK_ENTER);
        require(((Integer[]) get(keyPanel(another), "currentKey"))[0] == KeyEvent.VK_UP,
            "저장 전 새 화면에 미완료 편집이 전파되면 안 됩니다.");
        press(keys, KeyEvent.VK_UP);
        press(keys, KeyEvent.VK_ENTER);
        String expected = DEFAULT_KEYS.replace("ROTATE=38", "ROTATE=65");
        require(value(owner.setting, "keySetting").equals(expected), "키 서브메뉴 저장은 메모리 설정을 갱신해야 합니다.");
        require(Arrays.equals(originalFile, Files.readAllBytes(Path.of("setting.txt"))), "상위 메뉴 저장 전에는 파일을 변경하면 안 됩니다.");
        require(value(owner.new Setting(), "keySetting").equals(DEFAULT_KEYS), "파일 저장 전 재로드는 원래 키여야 합니다.");
        owner.setting.exportSetting();
        require(value(owner.new Setting(), "keySetting").equals(expected), "파일 저장 후에는 변경한 키를 읽어야 합니다.");
    }

    private static void settingOptionCycle() throws Exception {
        Maincontainer owner = owner("");
        Settingpanel settings = new Settingpanel(owner);
        for (String size : List.of("750X625", "900X750", "600X500", "750X625")) {
            press(settings, KeyEvent.VK_ENTER);
            require(value(owner.setting, "windowSize").equals(size) && hasLabel(settings, size), "창 크기 순서와 표시가 같아야 합니다.");
        }
        for (int i = 0; i < 3; i++) press(settings, KeyEvent.VK_DOWN);
        for (String color : List.of("Protanopia", "Deuteranopia", "Tritanopia", "Normal", "Protanopia")) {
            press(settings, KeyEvent.VK_ENTER);
            require(value(owner.setting, "colorBlind").equals(color) && hasLabel(settings, color), "색상 순서와 표시가 같아야 합니다.");
        }
        require(Files.readString(Path.of("setting.txt")).isEmpty(), "선택만 바꿔서는 파일을 저장하면 안 됩니다.");
        owner.setting.exportSetting();
        Maincontainer.Setting loaded = owner.new Setting();
        require(value(loaded, "windowSize").equals("750X625") && value(loaded, "colorBlind").equals("Protanopia"),
            "변경한 창 크기·색상을 저장하고 다시 읽어야 합니다.");
    }

    private static void settingFileBoundaries() throws Exception {
        Maincontainer owner = owner("");
        Files.delete(Path.of("setting.txt"));
        require(value(owner.new Setting(), "keySetting").equals(DEFAULT_KEYS), "파일이 없으면 기본 키여야 합니다.");
        owner = owner("\ninvalid line\nwindowSize:bad\ncolorBlind:Unknown\nfuture:value\n");
        require(value(owner.setting, "windowSize").equals("600X500") && value(owner.setting, "colorBlind").equals("Normal"),
            "알 수 없는 값은 기본 설정을 유지해야 합니다.");
        owner = owner("  windowSize : 900X750  \n  colorBlind : Tritanopia  \n keySetting : " + CUSTOM_KEYS + "  \n");
        require(value(owner.setting, "windowSize").equals("900X750") && value(owner.setting, "colorBlind").equals("Tritanopia")
                && value(owner.setting, "keySetting").equals(CUSTOM_KEYS), "줄 앞뒤와 콜론 주변 공백은 기존처럼 허용해야 합니다.");
    }

    private static void keyArrayOwnership() throws Exception {
        Maincontainer owner = owner("");
        Integer[] first = owner.setting.getKeyCodes();
        Integer[] second = owner.setting.getKeyCodes();
        require(first != second && Arrays.equals(first, second), "조회마다 독립된 편집 배열이어야 합니다.");
        first[0] = KeyEvent.VK_W;
        require(second[0] == KeyEvent.VK_UP && value(owner.setting, "keySetting").equals(DEFAULT_KEYS),
            "반환 배열 변경이 다른 배열이나 저장된 설정에 전파되면 안 됩니다.");
        owner.setting.updateKeyCodes(first);
        first[0] = KeyEvent.VK_A;
        require(value(owner.setting, "keySetting").equals(CUSTOM_KEYS) && owner.setting.getKeyCodes()[0] == KeyEvent.VK_W,
            "저장에 전달한 배열을 나중에 바꿔도 설정값이 바뀌면 안 됩니다.");
    }

    private static void blockInitialization() {
        record Expected(Block block, String shape, java.awt.Color color) {}
        for (Expected expected : List.of(
                new Expected(new IBlock(), "[[1, 1, 1, 1]]", java.awt.Color.CYAN),
                new Expected(new JBlock(), "[[1, 1, 1], [0, 0, 1]]", java.awt.Color.BLUE),
                new Expected(new LBlock(), "[[1, 1, 1], [1, 0, 0]]", java.awt.Color.ORANGE),
                new Expected(new OBlock(), "[[1, 1], [1, 1]]", java.awt.Color.YELLOW),
                new Expected(new SBlock(), "[[0, 1, 1], [1, 1, 0]]", java.awt.Color.GREEN),
                new Expected(new TBlock(), "[[0, 1, 0], [1, 1, 1]]", java.awt.Color.MAGENTA),
                new Expected(new ZBlock(), "[[1, 1, 0], [0, 1, 1]]", java.awt.Color.RED))) {
            Block block = expected.block();
            String name = block.getClass().getSimpleName();
            require(expected.shape().equals(shape(block)), name + "의 초기 모양이 바뀌면 안 됩니다.");
            require(expected.color().equals(block.getColor()), name + "의 초기 색이 바뀌면 안 됩니다.");
        }
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

    private enum DropInput { MOVE_DOWN, TIMER, DOWN_KEY, HARD_DROP, HARD_DROP_KEY }

    private static void drop(GameFixture fixture, DropInput input, boolean expectedMove) {
        switch (input) {
            case MOVE_DOWN -> require(fixture.board.moveDown() == expectedMove, "moveDown 반환값이 바뀌면 안 됩니다.");
            case TIMER -> {
                for (var listener : fixture.initialTimer.getActionListeners()) {
                    listener.actionPerformed(new java.awt.event.ActionEvent(fixture.initialTimer, 0, "regression-tick"));
                }
            }
            case DOWN_KEY -> press(fixture.game, Maincontainer.DOWN);
            case HARD_DROP -> fixture.board.hardDrop();
            case HARD_DROP_KEY -> press(fixture.game, Maincontainer.HARD_DROP);
        }
    }

    private static void dropInputScore() throws Exception {
        for (DropInput input : DropInput.values()) {
            try (GameFixture fixture = new GameFixture()) {
                Board board = fixture.board;
                Block current = new OBlock();
                prepare(board, current, 3, 0);
                Block next = board.getNextBlock();
                drop(fixture, input, true);
                boolean hard = input == DropInput.HARD_DROP || input == DropInput.HARD_DROP_KEY;
                int expected = hard ? 36 : input == DropInput.DOWN_KEY ? 2 : 1;
                require(board.getScore() == expected, input + ": 1칸 자동/직접=1점, 수동=2점, 하드드롭=거리×2점을 유지해야 합니다.");
                require(((JTextPane) get(fixture.game, "scorepanel")).getText().contains("\n" + expected), "화면 점수도 갱신되어야 합니다.");
                require(board.x == 3 && board.y == (hard ? 0 : 1), input + ": 이동 후 좌표가 달라지면 안 됩니다.");
                require(get(board, "curr") == (hard ? next : current), input + ": 하드드롭만 즉시 다음 블록으로 넘어가야 합니다.");
                if (!hard) require(board.getNextBlock() == next, "이동만 할 때 미리보기 블록이 바뀌면 안 됩니다.");
            }
        }
    }

    private static void lockAdvance() throws Exception {
        for (DropInput input : DropInput.values()) {
            for (boolean obstacle : new boolean[] {false, true}) {
                try (GameFixture fixture = new GameFixture()) {
                    Board board = fixture.board;
                    int x = obstacle ? 3 : 0, y = obstacle ? 5 : 18;
                    prepare(board, new OBlock(), x, y);
                    if (obstacle) {
                        call(board, "eraseCurr");
                        fixedCell(board, 3, 7);
                        call(board, "placeBlock");
                    }
                    Block next = board.getNextBlock();
                    Random random = (Random) get(board, "random");
                    random.setSeed(205);
                    Random expected = new Random(205);
                    String expectedNext = List.of("IBlock", "JBlock", "LBlock", "ZBlock", "SBlock", "TBlock", "OBlock").get(expected.nextInt(7));
                    drop(fixture, input, false);
                    require(board.getScore() == 0, input + ": 더 내려갈 수 없으면 낙하 점수를 추가하면 안 됩니다.");
                    require(get(board, "curr") == next && board.x == 3 && board.y == 0, "미리보기 객체를 그대로 시작 좌표로 승계해야 합니다.");
                    require(board.getNextBlock().getClass().getSimpleName().equals(expectedNext)
                            && random.nextLong() == expected.nextLong(), "새 미리보기는 기존 생성기를 정확히 한 번 사용해야 합니다.");
                    assertPreview(fixture.game, board.getNextBlock());
                    for (int row = y; row < y + 2; row++) for (int col = x; col < x + 2; col++) {
                        require(cells(board)[row][col] == 1, "기존 O블록을 제자리에 고정해야 합니다.");
                        assertChunk(board, col, row, "O", java.awt.Color.YELLOW);
                    }
                    require(Arrays.stream(cells(board)).flatMapToInt(Arrays::stream).sum() == (obstacle ? 9 : 8), "고정 블록과 새 현재 블록을 한 번씩만 배치해야 합니다.");
                    assertChunk(board, 4, 0, "O", java.awt.Color.MAGENTA);
                    if (!obstacle) {
                        assertChunk(board, 4, 18, "O", java.awt.Color.DARK_GRAY);
                        for (int col = 3; col <= 5; col++) assertChunk(board, col, 19, "O", java.awt.Color.DARK_GRAY);
                    }
                    require(get(fixture.game, "gameOverPanel") == null, "정상 고정에서 게임 오버를 표시하면 안 됩니다.");
                }
            }
        }
    }

    private static void lockLineLevel() throws Exception {
        for (DropInput input : DropInput.values()) {
            try (GameFixture fixture = new GameFixture()) {
                Board board = fixture.board;
                prepare(board, new OBlock(), 3, 18);
                call(board, "eraseCurr");
                fillLockLines(board);
                fixedCell(board, 0, 17);
                call(board, "placeBlock");
                set(board, "erasedLineNum", 8);
                Block next = board.getNextBlock();
                drop(fixture, input, false);
                require(board.getScore() == 300 && (int) get(board, "erasedLineNum") == 10, "고정한 O블록으로 두 줄을 지우고 기존 combo 점수를 적용해야 합니다.");
                require((int) get(board, "level") == 2 && board.timer.getDelay() == 793, "10줄 경계에서 레벨과 타이머 간격을 갱신해야 합니다.");
                require(cells(board)[19][0] == 1 && Arrays.stream(cells(board)).flatMapToInt(Arrays::stream).sum() == 5,
                    "남은 고정 칸을 두 줄 내리고 새 블록을 한 번만 배치해야 합니다.");
                assertChunk(board, 0, 19, "O", java.awt.Color.RED);
                require(get(board, "curr") == next && board.x == 3 && board.y == 0, "줄 삭제 후에도 next 승계를 유지해야 합니다.");
            }
        }
    }

    private static void lockGameOver() throws Exception {
        for (DropInput input : DropInput.values()) {
            try (GameFixture fixture = new GameFixture()) {
                Board board = fixture.board;
                Block current = new OBlock(), next = new OBlock();
                prepare(board, current, 0, 18);
                set(board, "next", next);
                fixedCell(board, 3, 0);
                fixture.game.drawNextBoard();
                Object preview = get(get(fixture.game, "nextblockpanel"), "textChunks");
                Random random = (Random) get(board, "random");
                random.setSeed(205);
                fixture.initialTimer.start();
                drop(fixture, input, false);
                require(board.timer == null && !fixture.initialTimer.isRunning() && get(fixture.game, "gameOverPanel") != null,
                    "두 낙하 경로 모두 타이머를 종료하고 게임 오버를 표시해야 합니다.");
                require(get(board, "curr") == current && board.getNextBlock() == next && board.x == 0 && board.y == 18,
                    "게임 오버 뒤 curr/next·좌표를 바꾸면 안 됩니다.");
                require(random.nextLong() == new Random(205).nextLong(), "게임 오버 뒤 새 난수를 소비하면 안 됩니다.");
                require(get(get(fixture.game, "nextblockpanel"), "textChunks") == preview,
                    "게임 오버 뒤 미리보기 표시를 새로 바꾸면 안 됩니다.");
                assertPreview(fixture.game, next);
                require(board.getScore() == 0 && cells(board)[0][3] == 1
                        && Arrays.stream(cells(board)).flatMapToInt(Arrays::stream).sum() == 5,
                    "게임 오버 뒤 점수 추가나 다음 블록 중첩이 없어야 합니다.");
            }
        }
    }

    private static void lockClearBeforeGameOver() throws Exception {
        for (DropInput input : DropInput.values()) {
            try (GameFixture fixture = new GameFixture()) {
                Board board = fixture.board;
                prepare(board, new OBlock(), 3, 18);
                call(board, "eraseCurr");
                fillLockLines(board);
                Block next = new OBlock();
                set(board, "next", next);
                fixedCell(board, 3, 0);
                call(board, "placeBlock");
                drop(fixture, input, false);
                require(get(fixture.game, "gameOverPanel") == null && board.timer != null,
                    "줄 삭제가 생성 자리를 비우면 게임 오버로 종료하면 안 됩니다.");
                require(get(board, "curr") == next && board.getScore() == 300 && cells(board)[2][3] == 1,
                    "줄 삭제를 게임 오버 판정보다 먼저 하고 다음 블록을 배치해야 합니다.");
                require(cells(board)[0][3] == 1 && cells(board)[0][4] == 1 && cells(board)[1][3] == 1 && cells(board)[1][4] == 1,
                    "비워진 시작 좌표에 O블록이 정상 배치되어야 합니다.");
            }
        }
    }

    private static void fillLockLines(Board board) throws Exception {
        for (int row = 18; row < 20; row++) for (int col = 0; col < 10; col++) {
            if (col != 3 && col != 4) fixedCell(board, col, row);
        }
    }

    private static void assertPreview(Gamepanel game, Block next) throws Exception {
        StringBuilder expected = new StringBuilder("      \n      \n");
        for (int row = 0; row < next.height(); row++) {
            expected.append(' ');
            for (int col = 0; col < next.width(); col++) expected.append(next.getShape(col, row) == 0 ? ' ' : 'O');
            expected.append('\n');
        }
        StringBuilder actual = new StringBuilder();
        for (Object chunk : (List<?>) get(get(game, "nextblockpanel"), "textChunks")) {
            actual.append(get(chunk, "text"));
            require(get(chunk, "color").equals(next.getColor()), "미리보기 색이 새 next와 같아야 합니다.");
        }
        require(actual.toString().equals(expected.toString()), "미리보기 표시가 새 next 모양 전체와 같아야 합니다.");
    }

    @SuppressWarnings("unchecked")
    private static void assertChunk(Board board, int x, int y, String text, java.awt.Color color) throws Exception {
        List<Board.TextChunk> chunks = (List<Board.TextChunk>) get(board, "textChunks");
        Board.TextChunk chunk = chunks.get((y + 1) * 13 + x + 1);
        require(chunk.text.equals(text) && chunk.color.equals(color), "표시 청크가 격자·블록 색과 일치해야 합니다: " + x + "," + y);
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
    private static String value(Maincontainer.Setting setting, String name) throws Exception {
        java.util.Map<?, ?> values = (java.util.Map<?, ?>) get(setting, "currentSettingMap");
        return (String) call(values.get(name), "getCurrentSetting");
    }
    private static String scoreText(Scoreboardpanel panel) throws Exception { return ((JTextPane) get(panel, "scorepanel")).getText(); }

    private static List<Block> blocks() { return List.of(new IBlock(), new JBlock(), new LBlock(), new OBlock(), new SBlock(), new TBlock(), new ZBlock()); }

    private static String shape(Block block) {
        int[][] cells = new int[block.height()][block.width()];
        for (int y = 0; y < block.height(); y++) for (int x = 0; x < block.width(); x++) cells[y][x] = block.getShape(x, y);
        return Arrays.deepToString(cells);
    }

    private static JPanel keyPanel(Settingpanel setting) {
        return Arrays.stream(setting.getComponents())
            .filter(item -> item.getClass().getSimpleName().equals("ChangeKeyPanel"))
            .map(JPanel.class::cast).findFirst().orElseThrow();
    }

    private static List<JPanel> menuButtons(Container root, String... labels) {
        List<JPanel> buttons = new ArrayList<>();
        for (String label : labels) {
            JPanel button = findMenuButton(root, label);
            require(button != null, "메뉴 항목을 찾을 수 없습니다: " + label);
            buttons.add(button);
        }
        return buttons;
    }

    private static JPanel findMenuButton(Container root, String label) {
        for (var child : root.getComponents()) {
            if (child instanceof JPanel panel) {
                if (Arrays.stream(panel.getComponents()).anyMatch(item -> item instanceof JLabel text && label.equals(text.getText()))) return panel;
                JPanel found = findMenuButton(panel, label);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static boolean hasLabel(Container root, String label) {
        for (var child : root.getComponents()) {
            if (child instanceof JLabel text && label.equals(text.getText())) return true;
            if (child instanceof Container container && hasLabel(container, label)) return true;
        }
        return false;
    }

    private static void assertSelection(List<JPanel> buttons, int selected) {
        for (int index = 0; index < buttons.size(); index++) {
            require(buttons.get(index).isOpaque() == (index == selected),
                "선택 강조는 항목 " + selected + "에만 있어야 합니다(검사 항목 " + index + ").");
        }
    }

    private static void checkNavigation(JPanel panel, List<JPanel> buttons) {
        assertSelection(buttons, 0);
        press(panel, KeyEvent.VK_UP);
        assertSelection(buttons, buttons.size() - 1);
        press(panel, KeyEvent.VK_DOWN);
        assertSelection(buttons, 0);
        for (int index = 1; index <= buttons.size(); index++) {
            press(panel, KeyEvent.VK_DOWN);
            assertSelection(buttons, index % buttons.size());
        }
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
