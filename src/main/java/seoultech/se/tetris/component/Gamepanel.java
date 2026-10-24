package seoultech.se.tetris.component;

import static seoultech.se.tetris.component.Maincontainer.changeColor;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import seoultech.se.tetris.blocks.Block;
import seoultech.se.tetris.Difficulty;
import seoultech.se.tetris.score.ScoreStore;

public class Gamepanel extends JPanel{
    private Board board;
    private final Difficulty difficulty;
    private Nextblockpanel nextblockpanel;
    private JTextPane scorepanel;
    private StyledDocument docScore;
    private SimpleAttributeSet styleSetScore1;
    private SimpleAttributeSet styleSetScore2;
    private Maincontainer maincontainer;
    private int score;
    private String userName = "PLAYER";
    private StringBuffer userNameBuffer;
    private JLabel setUserNameText;
    private JPanel pausepanel;
    private JPanel gameOverPanel;
    private int HEIGHT;
    private int WIDTH;
    private KeyListener playerKeyListener;

    public Gamepanel(Maincontainer maincontainer) {
        setLayout(null);
        setBackground(changeColor(Color.LIGHT_GRAY));
        this.maincontainer = maincontainer;
        this.difficulty = maincontainer.getDifficulty();
        HEIGHT = maincontainer.getConfiguredHeight(); WIDTH = maincontainer.getConfiguredWidth();

        board = new Board(this);
        board.setBounds(10, 10, WIDTH*330/500, HEIGHT*580/600);
        add(board);

        nextblockpanel = new Nextblockpanel();
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
        JButton menuButton = new JButton("MENU");
        menuButton.setBounds(WIDTH*330/500+20, HEIGHT*200/600+30, WIDTH*140/500, 35);
        menuButton.addActionListener(event -> {
            if (board.timer != null) { board.timer.stop(); board.timer = null; }
            gameExit();
        });
        add(menuButton);

        board.boardStart();

        playerKeyListener = new PlayerKeyListener();
		addKeyListener(playerKeyListener);
		setFocusable(true);
		requestFocus();
        setFocusTraversalKeysEnabled(false);
    }
    
    Difficulty getDifficulty() { return difficulty; }

    protected void drawNextBoard(Block next) {
		nextblockpanel.drawNextBoard(next);
	}

    protected void drawScore() {
        this.score = board.getScore();
        this.scorepanel.setText("Scores\n" + this.score);
        docScore = scorepanel.getStyledDocument();
		docScore.setParagraphAttributes(0, 1, styleSetScore1, false);
        docScore.setParagraphAttributes(7, 1, styleSetScore2, false);
    }

    protected void gameOver() {
        var rankings = ScoreStore.readRanked();
        if (rankings.size() >= 10 && board.getScore() <= rankings.get(9).score()) {
            board.stopAnimations();
            maincontainer.exitGameEnterScoreboard(null, board.getScore(), difficulty);
            return;
        }
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
        ScoreStore.append(this.userName, this.score, difficulty);
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
        board.stopAnimations();
        maincontainer.exitGameEnterStart();
    }

    protected void gameRestart() {
        remove(pausepanel);
        revalidate();
        repaint();
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
            if (i == KeyEvent.VK_ESCAPE && i != Maincontainer.DOWN && i != Maincontainer.LEFT
                    && i != Maincontainer.RIGHT && i != Maincontainer.ROTATE
                    && i != Maincontainer.PAUSE && i != Maincontainer.HARD_DROP) {
                if (board.timer != null) { board.timer.stop(); board.timer = null; }
                gameExit();
                return;
            }
			if (i == Maincontainer.DOWN) {
				board.moveDown();
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
            if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) && userNameBuffer.length() < 10) { // 영문 대소문자와 숫자 0~9를 최대 10자까지 입력한다.
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
                userName = setUserNameText.getText();
                removeKeyListener(this);
                saveScore();
                maincontainer.exitGameEnterScoreboard(userName, score, difficulty);
            } else e.consume();
        }

        @Override 
        public void keyReleased(KeyEvent e) {}
    }
}
