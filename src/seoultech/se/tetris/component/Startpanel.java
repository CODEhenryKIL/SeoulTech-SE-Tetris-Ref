package seoultech.se.tetris.component;

import javax.swing.*;

import static seoultech.se.tetris.component.Maincontainer.changeColor;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class Startpanel extends JPanel{

    private KeyListener playerKeyListener;
    private Maincontainer maincontainer;
    private int width;
    private int height;

    public Startpanel(Maincontainer maincontainer) {
        width = maincontainer.getWidth(); height = maincontainer.getHeight();
        this.maincontainer = maincontainer;
        setPreferredSize(new Dimension(width, height));
		setLayout(new GridBagLayout()); // 컴포넌트 간의 배치를 자유롭게 하기 위해서 GridBagLayout을 사용
		setBackground(changeColor(Color.LIGHT_GRAY)); // background color로 light gray
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // padding 값 조절, 필요 시 제거 가능
        Button.resetStaticField();

		// Title 컴포넌트 만들기
        JLabel titlelabel = new JLabel("TETRIS", SwingConstants.CENTER);
        titlelabel.setFont(new Font("Courier", Font.BOLD, 56));
        titlelabel.setForeground(changeColor(Color.WHITE));

        // Title 컴포넌트 위치를 위한 제약사항
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; // grid[0][0]에 위치
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0; gbc.weighty = 0.3; // 수직 화면의 30% 차지하게 조정

        add(titlelabel, gbc);  // 타이틀은 위쪽에

        // Button 컴포넌트 만들기
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout()); // 3행 1열, 버튼 사이 간격 15px
        buttonPanel.setOpaque(false); // 투명 배경
        buttonPanel.setPreferredSize(new Dimension(100, 150)); // 컴포넌트 사이즈 지정

        // Button 4개 추가하기
        Button startButton = new Button("START") {
            @Override 
            protected void isPressed() {
                Startpanel.this.maincontainer.exitStartEnterGame();
            }
        };
        Button.addButton(buttonPanel, startButton);
        Button menuButton = new Button("MENU") {
            @Override 
            protected void isPressed() {
                Startpanel.this.maincontainer.exitStartEnterSetting();
            }
        };
        Button.addButton(buttonPanel, menuButton);
        Button scoreboardButton = new Button("SCORE") {
            @Override 
            protected void isPressed() {
                Startpanel.this.maincontainer.exitStartEnterScoreboard();
            }
        };
        Button.addButton(buttonPanel, scoreboardButton);
        Button exitButton = new Button("EXIT") {
            @Override 
            protected void isPressed() {
                System.exit(0);
            }
        };
        Button.addButton(buttonPanel, exitButton);

        // Button 컴포넌트 위치를 위한 제약사항
        gbc.gridy = 1; // grid[0][1]에 위치
        gbc.weighty = 0.7; // 수직 화면의 70% 차지하게 조정

        add(buttonPanel, gbc); // 버튼들은 중앙에

        playerKeyListener = new PlayerKeyListener();
		addKeyListener(playerKeyListener);
		setFocusable(true);
		requestFocus();
    }
    
    private class PlayerKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_DOWN:
                selectDown();
                break;
			case KeyEvent.VK_UP:
                selectUp();
				break;
            case KeyEvent.VK_ENTER:
                selectEnter();
                break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
	}

    private void selectUp() {
        Button.getSelectedButton().setOpaque(false);
        Button.reduceCurrentSelected();
        Button.getSelectedButton().setOpaque(true);
        this.revalidate();
        this.repaint();
    }

    private void selectDown() {
        Button.getSelectedButton().setOpaque(false);
        Button.addCurrentSelected();
        Button.getSelectedButton().setOpaque(true);
        this.revalidate();
        this.repaint();
    }

    private void selectEnter() {
        Button.getSelectedButton().isPressed();
    }

    private class Button extends JPanel {

        protected JLabel text;
        private static int numberOfOptions = 0;
        private static int currentSelected = 0;
        private static ArrayList<Button> buttonList;

        public Button(String buttonName) {
            setBackground(changeColor(Color.GRAY));
            if (numberOfOptions == 0) setOpaque(true);
            else setOpaque(false);

            text = new JLabel(buttonName, SwingConstants.CENTER);
            text.setFont(new Font("Courier", Font.BOLD, 16));
            text.setForeground(changeColor(Color.BLACK));
            add(text);

            Dimension labelSize = text.getPreferredSize();
            int panelWidth = labelSize.width + 20;
            int panelHeight = labelSize.height + 20;
            setPreferredSize(new Dimension(panelWidth, panelHeight));
        }

        protected void isPressed() {}

        protected static void addButton(JPanel parent, Button child) {
            if (numberOfOptions > 10) return;
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.gridy = numberOfOptions; gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(2, 2, 2, 2);
            parent.add(child, gbc);
            numberOfOptions++;
            buttonList.add(child);
        }

        protected static void addCurrentSelected() {
            if (currentSelected >= numberOfOptions - 1) {
                currentSelected = 0;
            } else currentSelected++;
        }

        protected static void reduceCurrentSelected() {
            if (currentSelected <= 0) {
                currentSelected = numberOfOptions - 1;
            } else currentSelected--;
        }

        protected static Button getSelectedButton() {
            return buttonList.get(currentSelected);
        }

        protected static void resetStaticField() {
            numberOfOptions = 0;
            currentSelected = 0;
            buttonList = new ArrayList<>();
        }
    }
}
