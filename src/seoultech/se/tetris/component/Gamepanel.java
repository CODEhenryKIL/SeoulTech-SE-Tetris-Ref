package seoultech.se.tetris.component;

import static seoultech.se.tetris.component.Maincontainer.changeColor;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import seoultech.se.tetris.blocks.Block;

public class Gamepanel extends JPanel{
    private Board board;
    private Nextblockpanel nextblockpanel;
    private JTextPane scorepanel;
    private StyledDocument docScore;
    private SimpleAttributeSet styleSetScore1;
    private SimpleAttributeSet styleSetScore2;
    private Maincontainer maincontainer;
    private int score;
    private String usrname = "PLAYER";
    private StringBuffer userNameBuffer;
    private JLabel setUserNameText;
    private Path path;
    private JPanel pausepanel;
    private JPanel gameOverPanel;
    private int HEIGHT;
    private int WIDTH;
    private KeyListener playerKeyListener;

    public Gamepanel(Maincontainer maincontainer) {
        setLayout(null);
        setBackground(changeColor(Color.LIGHT_GRAY));
        this.maincontainer = maincontainer;
        HEIGHT = maincontainer.getHeight(); WIDTH = maincontainer.getWidth();

        board = new Board(this);
        board.setBounds(10, 10, WIDTH*330/500, HEIGHT*580/600);
        add(board);

        nextblockpanel = new Nextblockpanel(this);
        nextblockpanel.setBounds(WIDTH*330/500+20, 10, WIDTH*140/500, HEIGHT*140/600);
        add(nextblockpanel);

        scorepanel = new JTextPane();
        scorepanel.setEditable(false);
        scorepanel.setBounds(WIDTH*330/500+20, HEIGHT*140/600+20, WIDTH*140/500, HEIGHT*50/600);
		scorepanel.setBorder(BorderFactory.createLineBorder(changeColor(Color.GRAY), 1));
        scorepanel.setOpaque(false);
        scorepanel.setText("Scores\n" + this.score);
        docScore = scorepanel.getStyledDocument();
        styleSetScore1 = new SimpleAttributeSet();
        styleSetScore2 = new SimpleAttributeSet();
		StyleConstants.setFontSize(styleSetScore1, 18);
		StyleConstants.setFontFamily(styleSetScore1, "Courier");
		StyleConstants.setForeground(styleSetScore1, changeColor(Color.GRAY));
		StyleConstants.setAlignment(styleSetScore1, StyleConstants.ALIGN_CENTER);
		docScore.setParagraphAttributes(0, 1, styleSetScore1, false);
        StyleConstants.setFontSize(styleSetScore2, 18);
		StyleConstants.setFontFamily(styleSetScore2, "Courier");
		StyleConstants.setForeground(styleSetScore2, changeColor(Color.RED));
		StyleConstants.setAlignment(styleSetScore2, StyleConstants.ALIGN_RIGHT);
        docScore.setParagraphAttributes(7, 1, styleSetScore2, false);
        add(scorepanel);

        board.boardStart();

        playerKeyListener = new PlayerKeyListener();
		addKeyListener(playerKeyListener);
		setFocusable(true);
		requestFocus();
        setFocusTraversalKeysEnabled(false);
    }
    
    protected void drawNextBoard() {
		nextblockpanel.drawNextBoard();
	}

    protected void drawScore() {
        this.score = board.getScore();
        this.scorepanel.setText("Scores\n" + this.score);
        docScore = scorepanel.getStyledDocument();
		docScore.setParagraphAttributes(0, 1, styleSetScore1, false);
        docScore.setParagraphAttributes(7, 1, styleSetScore2, false);
    }

    protected void gameOver() {
        gameOverPanel = new JPanel() {
		    @Override 
		    protected void paintComponent(Graphics g) {
			    super.paintComponent(g);
			    Graphics2D g2d = (Graphics2D) g.create();
			    g2d.setColor(changeColor(new Color(0, 0, 0, 200)));
			    g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
		    }
        };
        gameOverPanel.setOpaque(false);
        gameOverPanel.setBounds(0, 0, WIDTH, HEIGHT);
        gameOverPanel.setLayout(new GridBagLayout());
        
        JPanel gridPanel = new JPanel(new GridLayout(0, 1));
        gridPanel.setOpaque(false);

            JLabel gotext = new JLabel("Game Over", SwingConstants.CENTER);
            gotext.setFont(new Font("Courier", Font.BOLD, 30));
            gotext.setForeground(changeColor(Color.WHITE));

            JLabel sctext = new JLabel("Your score is " + board.getScore(), SwingConstants.CENTER);
            sctext.setFont(new Font("Courier", Font.BOLD, 16));
            sctext.setForeground(changeColor(Color.WHITE));

            JLabel entext = new JLabel("Enter your name", SwingConstants.CENTER);
            entext.setFont(sctext.getFont());
            entext.setForeground(changeColor(Color.WHITE));

            setUserNameText = new JLabel("PLAYER", SwingConstants.CENTER);
            setUserNameText.setFont(gotext.getFont());
            setUserNameText.setForeground(changeColor(new Color(255, 255, 255, 90)));

        int gridPanelHeight = gotext.getPreferredSize().height + sctext.getPreferredSize().height + entext.getPreferredSize().height + setUserNameText.getPreferredSize().height + 50;
        gridPanel.setPreferredSize(new Dimension(WIDTH, gridPanelHeight));

        gridPanel.add(gotext);
        gridPanel.add(sctext);
        gridPanel.add(entext);
        gridPanel.add(setUserNameText);
        gameOverPanel.add(gridPanel);

        add(gameOverPanel);
        setComponentZOrder(gameOverPanel, 0);
        revalidate();
        repaint();

        userNameBuffer = new StringBuffer();
        removeKeyListener(playerKeyListener);
        UserNameListener userNameListener = new UserNameListener();
        addKeyListener(userNameListener);
        requestFocusInWindow();
    }

    private void saveScore() {
        path = Paths.get("tetris_score.txt");
        String str = this.usrname + ":" + this.score;
        try {
            Files.write(path, Arrays.asList(str), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND); // 파일이 없으면 새로 만들고(CREATE), 있으면 맨 뒤에 이어 붙임(APPEND)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void gamePause() {
        pausepanel = new JPanel() {
		    @Override 
		    protected void paintComponent(Graphics g) {
			    super.paintComponent(g);
			    Graphics2D g2d = (Graphics2D) g.create();
			    g2d.setColor(changeColor(new Color(255, 255, 255, 80)));
			    g2d.fillRect(0, 0, board.getWidth(), board.getHeight());
                g2d.dispose();
		    }
	    };
        pausepanel.setOpaque(false);
        pausepanel.setBounds(10, 10, board.getWidth(), board.getHeight());
        pausepanel.setLayout(new GridBagLayout());
        JPanel textpanel = new JPanel(new GridLayout(3, 1, 0, 10));
        textpanel.setOpaque(false);

            JLabel pause = new JLabel("- PAUSED -", SwingConstants.CENTER);
            pause.setFont(new Font("Courier", Font.BOLD, 20));
            pause.setForeground(changeColor(Color.WHITE));

            JLabel explane = new JLabel("press pause to resume", SwingConstants.CENTER);
            explane.setFont(new Font("Courier", Font.BOLD, 13));
            explane.setForeground(changeColor(Color.WHITE));

            JLabel exit = new JLabel("press enter to exit", SwingConstants.CENTER);
            exit.setFont(new Font("Courier", Font.BOLD, 13));
            exit.setForeground(changeColor(Color.WHITE));

        int h = pause.getPreferredSize().height + explane.getPreferredSize().height + exit.getPreferredSize().height;
        textpanel.setPreferredSize(new Dimension(board.getWidth(), h+20));

        textpanel.add(pause);
        textpanel.add(explane);
        textpanel.add(exit);
        pausepanel.add(textpanel);

        add(pausepanel);
        setComponentZOrder(pausepanel, 0);
        revalidate();
        repaint();
    }

    protected void gameExit() {
        maincontainer.exitGameEnterStart();
    }

    protected void gameRestart() {
        remove(pausepanel);
        revalidate();
        repaint();
    }

    protected Block getNextBlock() {
        return board.getNextBlock();
    }

    public class PlayerKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			if (board.getIsPaused()) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (board.timer != null) {
						board.timer.stop();
						board.timer = null;
					}
					gameExit();
					return;
				} else if (e.getKeyCode() == Maincontainer.PAUSE) {
					board.togglePause();
					return;
				} else return;
			}
			int i = e.getKeyCode();
			if (i == Maincontainer.DOWN) {
				if (board.moveDown()) board.addScore(Board.DOWNPOINT);
				board.drawBoard();
				return;
			} else if (i == Maincontainer.RIGHT) {
				board.moveRight();
				board.drawBoard();
				return;
			} else if (i == Maincontainer.LEFT) {
				board.moveLeft();
				board.drawBoard();
				return;
			} else if (i == Maincontainer.ROTATE) {
				board.rotate();
				board.drawBoard();
				return;
			} else if (i == Maincontainer.HARD_DROP) { // 하드 드롭 키 할당
				board.hardDrop();
				board.drawBoard();
				return;
			} else if (i == Maincontainer.PAUSE) {
				board.togglePause();
				return;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
	}

    public class UserNameListener implements KeyListener {

        @Override
		public void keyTyped(KeyEvent e) {}

        @Override 
        public void keyPressed(KeyEvent e) {
            char c = e.getKeyChar();
            int i = e.getKeyCode();
            if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) && userNameBuffer.length() < 10) { // A~Z = 65~90, 1~9 = 48~57
                userNameBuffer.append(c);
                setUserNameText.setText(userNameBuffer.toString());
                setUserNameText.setForeground(changeColor(Color.WHITE));
            } else if (i == KeyEvent.VK_BACK_SPACE && !userNameBuffer.isEmpty()) {
                userNameBuffer.deleteCharAt(userNameBuffer.length()-1);
                setUserNameText.setText(userNameBuffer.toString());
                if (userNameBuffer.isEmpty()) {
                    setUserNameText.setForeground(changeColor(new Color(255, 255, 255, 90)));
                    setUserNameText.setText("PLAYER");
                }
            } else if (i == KeyEvent.VK_ENTER) {
                usrname = setUserNameText.getText();
                removeKeyListener(this);
                saveScore();
                maincontainer.exitGameEnterStart();
            } else e.consume();
        }

        @Override 
        public void keyReleased(KeyEvent e) {}
    }
}
