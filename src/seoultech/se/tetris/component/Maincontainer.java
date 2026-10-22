package seoultech.se.tetris.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

// 창을 구성하는 클래스입니다.
public class Maincontainer extends JFrame{

    private static int HEIGHT = 600;
    private static int WIDTH = 500;
    protected static int ROTATE = 38;
    protected static int DOWN = 40;
    protected static int LEFT = 37;
    protected static int RIGHT = 39;
    protected static int PAUSE = 80;
    protected static int HARD_DROP = 32;
    protected static String colorPalette = "Normal"; // 0: 일반 색상, 1: 적색약, 2: 녹색약, 3:청황색약
    protected Setting setting;
    private JPanel mainPanel; // container에 띄울 panel로 background color가 black인 아무것도 없는 panel입니다.
    private CardLayout cardLayout; // 추후 mainPanel에 추가될 panel끼리 전환하기 위한 기반입니다. 현재는 코드에 큰 영향이 없습니다.
    private Startpanel startPanel; // 시작 시 보여질 화면 panel입니다.
    private Gamepanel gamePanel; // 게임 화면 panel입니다.
    private Settingpanel settingPanel;
    private Scoreboardpanel scoreboardPanel;

    public Maincontainer() {
        setting = new Setting();
        setTitle("SeoulTech SE Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        applySetting();
    }

    public void enterStart() { // 게임이 시작됨을 알리는 메소드
        startPanel = new Startpanel(this); // Startpanel 객체 생성 및 container 객체 넘겨주기
        mainPanel.add(startPanel, "StartScreen"); // mainPanel에 startPanel 추가
        mainPanel.revalidate(); // 변경사항을 재계산
        mainPanel.repaint(); // 변경사항 화면에 다시 그리기
        cardLayout.show(mainPanel, "StartScreen");
        startPanel.requestFocusInWindow();
    }
    // Start 버튼 상호작용 함수
    protected void exitStartEnterGame() { // 시작화면에서 시작 버튼을 눌러 게임화면으로 들어감을 알리는 메소드
        gamePanel = new Gamepanel(this); // Gamepanel 객체 생성 및 container 객체 넘겨주기
        mainPanel.add(gamePanel, "GameScreen"); // mainPanel에 gamePanel 추가
        mainPanel.remove(startPanel); // 게임 시작 시에는 시작 화면이 불필요하므로, startPanel 제거하여 memory leaking 방지하기
        mainPanel.revalidate(); // 변경사항을 재계산
        mainPanel.repaint(); // 변경사항 화면에 다시 그리기
        cardLayout.show(mainPanel, "GameScreen");
        gamePanel.requestFocusInWindow();
    }
    protected void exitGameEnterStart() {
        if (gamePanel != null && gamePanel.getParent() != null) mainPanel.remove(gamePanel);
        enterStart();
    }
    // Setting 버튼 상호작용 함수
    protected void exitStartEnterSetting() {
        settingPanel = new Settingpanel(this);
        mainPanel.add(settingPanel, "MenuScreen");
        mainPanel.remove(startPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
        cardLayout.show(mainPanel, "MenuScreen");
        settingPanel.requestFocusInWindow();
    }
    protected void exitSettingEnterStart() {
        mainPanel.remove(settingPanel);
        remove(mainPanel);
        applySetting();
        enterStart();
    }
    // Scoreboard 버튼 상호작용 함수
    protected void exitStartEnterScoreboard() {
        scoreboardPanel = new Scoreboardpanel(this);
        mainPanel.add(scoreboardPanel, "ScoreboardScreen");
        mainPanel.remove(startPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
        cardLayout.show(mainPanel, "ScoreboardScreen");
        scoreboardPanel.requestFocusInWindow();
    }
    protected void exitScoreboardEnterStart() {
        mainPanel.remove(scoreboardPanel);
        enterStart();
    }

    private void applySetting() {
        String[] h_w = setting.currentSettingMap.get("windowSize").getCurrentSetting().split("X");
        HEIGHT = Integer.parseInt(h_w[0]); WIDTH = Integer.parseInt(h_w[1]);
        String[] parts = setting.currentSettingMap.get("keySetting").getCurrentSetting().split(",");
        Integer[] keys = new Integer[6];
        for (int i = 0; i < 6; i++) keys[i] = Integer.parseInt(parts[i].split("=")[1]);
        ROTATE = keys[0]; DOWN = keys[1]; LEFT = keys[2]; RIGHT = keys[3]; PAUSE = keys[4]; HARD_DROP = keys[5];
        colorPalette = setting.currentSettingMap.get("colorBlind").getCurrentSetting();
        
        // container에 띄우기 위한 빈 panel
        mainPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setOpaque(true);
        add(mainPanel); // container에 mainPanel 추가
        pack();
        setVisible(true);
    }

    // 게임 설정 크기는 제목 표시줄·테두리를 포함한 JFrame의 실제 크기와 구분한다.
    public int getConfiguredWidth() {return WIDTH;}
    public int getConfiguredHeight() {return HEIGHT;}

    public static Color changeColor(Color exColor) {
        Color newColor = exColor;
        int r = exColor.getRed(); int g = exColor.getGreen();
        int b = exColor.getBlue(); int a = exColor.getAlpha();
        int newR; int newG; int newB; int newA;
        switch (colorPalette) {
            case "Normal":
                return newColor;
            case "Protanopia":
                newR = r;
                newG = (int) (0.5089 * r + 0.4911 * g);
                newB = (int) (0.6174 * r - 0.6173 * g + b);
                newA = a;
                newR = Math.max(0, Math.min(255, newR));
                newG = Math.max(0, Math.min(255, newG));
                newB = Math.max(0, Math.min(255, newB));
                newColor = new Color(newR, newG, newB, newA);
                return newColor;  
            case "Deuteranopia":
                newR = r;
                newG = (int) (0.2023 * r + 0.7977 * g);
                newB = (int) (0.5174 * r - 0.5175 * g + b);
                newA = a;
                newR = Math.max(0, Math.min(255, newR));
                newG = Math.max(0, Math.min(255, newG));
                newB = Math.max(0, Math.min(255, newB));
                newColor = new Color(newR, newG, newB, newA);
                return newColor;
            case "Tritanopia":
                newR = (int) (0.957 * r + 0.043 * g);
                newG = (int) (0.733 * g + 0.267 * b);
                newB = (int) (0.475 * g + 0.525 * b);
                newA = a;
                newR = Math.max(0, Math.min(255, newR));
                newG = Math.max(0, Math.min(255, newG));
                newB = Math.max(0, Math.min(255, newB));
                newColor = new Color(newR, newG, newB, newA);
                return newColor;
            default:
                return newColor;
        }
    }

    public class Setting {
        /*
        세팅 추가 시 지침
        1. validSettingMap에 설정 값으로 들어올 수 있는 값을 전부 넣기
        - 설정의 이름이 될 값을 String으로 지정해야 합니다.
        - 설정의 초기값이 될 값을 ArrayList<String> 목록의 가장 처음으로 할당해야 합니다.
        - 설정 변경은 항상 ArrayList<>에 담긴 순서대로 변경됩니다.
        2. key와 관련된 값은 기존에 쓰던 별도의 메소드를 그대로 사용
        - 만약 추가나 변경이 필요할 경우, isValidKeySetting()을 고쳐주시길 바랍니다.
        */
        protected LinkedHashMap<String, SettingComponent> currentSettingMap;
        private static final LinkedHashMap<String, ArrayList<String>> validSettingMap = new LinkedHashMap<>(Map.of(
            "windowSize", new ArrayList<>(List.of("600X500","750X625","900X750")),
            "keySetting", new ArrayList<>(List.of("ROTATE=38,DOWN=40,LEFT=37,RIGHT=39,PAUSE=80,HARD_DROP=32")),
            "resetScore", new ArrayList<>(List.of("false")),
            "colorBlind", new ArrayList<>(List.of("Normal","Protanopia","Deuteranopia","Tritanopia"))
        ));

        public Setting() {
            currentSettingMap = new LinkedHashMap<>();
            clearSetting();
            decodeSetting();
        }

        private void decodeSetting() {
            Path path = Paths.get("setting.txt");
            if (Files.exists(path)) {
                try {
                    List<String> lines = Files.readAllLines(path);
                    for (String line : lines) {
                        if (line.trim().isEmpty()) continue;
                        String[] tokens = line.split(":", 2);
                        if (tokens.length == 2) {
                            String key = tokens[0].trim();
                            String value = tokens[1].trim();
                            if (!currentSettingMap.containsKey(key)) continue;
                            if (key.equals("keySetting")) {
                                if (isValidKeySetting(value)) currentSettingMap.get(key).modifySetting(value);;
                            } else {
                                currentSettingMap.get(key).findApplySetting(value);;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean isValidKeySetting(String value) {
            String[] expectedKeys = {"ROTATE", "DOWN", "LEFT", "RIGHT", "PAUSE", "HARD_DROP"};
            String[] parts = value.split(",", -1);
            if (parts.length != expectedKeys.length) return false;

            int[] keyCodes = new int[expectedKeys.length];
            try {
                for (int i = 0; i < expectedKeys.length; i++) {
                    String[] keyValue = parts[i].split("=", -1);
                    if (keyValue.length != 2 || !keyValue[0].equals(expectedKeys[i])) return false;
                    keyCodes[i] = Integer.parseInt(keyValue[1]);
                    if (keyCodes[i] <= 0) return false;
                    if (i == 4 && keyCodes[i] == KeyEvent.VK_ENTER) return false; //TODO? 엔터키를 하드드롭에도 금지시켜야하나요?
                    for (int j = 0; j < i; j++) {
                        if (keyCodes[i] == keyCodes[j]) return false;
                    }
                }
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }

        protected void clearSetting() {
            for (Map.Entry<String, ArrayList<String>> entry : validSettingMap.entrySet()) {
                String name = entry.getKey();
                SettingComponent settingComponent = new SettingComponent(entry.getValue());
                currentSettingMap.put(name, settingComponent);
            }
        }

        protected static String[] getSettingList() {
            return validSettingMap.keySet().toArray(new String[0]);
        }

        protected void exportSetting() {
            Path path = Paths.get("setting.txt");
            StringBuffer sb = new StringBuffer();
            for (String line : getSettingList()) {
                String str = line + ":" + currentSettingMap.get(line).getCurrentSetting() + "\n";
                sb.append(str);
            }    
            try {
                Files.write(path, Arrays.asList(sb.toString()), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }        
        }

        protected class SettingComponent {
            
            private int numberOfList;
            private int currentIndex = 0;
            private ArrayList<String> list;

            protected SettingComponent(ArrayList<String> list) {
                this.list = new ArrayList<>(list);
                numberOfList = list.size();
            }

            protected String getCurrentSetting() {
                return list.get(currentIndex);
            }

            protected void modifySetting(String str) {
                list.set(currentIndex, str);
            }

            protected void findApplySetting(String str) {
                if (numberOfList == 1) return; // 단일 항목의 경우, 찾는 것이 아닌 modifySetting()을 사용
                for (int i = 0; i < numberOfList; i++) { // valid한 값이 아니면 변경될 수 없음
                    if (list.get(i).equals(str)) currentIndex = i;
                }
            }

            protected void changeSetting() {
                if (currentIndex >= numberOfList - 1) currentIndex = 0;
                else currentIndex++;
            }
        }
    }
}
