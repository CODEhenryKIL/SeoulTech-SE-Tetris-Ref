package seoultech.se.tetris.component;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import static seoultech.se.tetris.component.Maincontainer.changeColor;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import seoultech.se.tetris.score.ScoreStore;

public class Scoreboardpanel extends JPanel{
    private Maincontainer maincontainer;
    private JTextPane scorepanel;
    private PlayerKeyListener playerKeyListener;
    private int HEIGHT;
    private int WIDTH;

    public Scoreboardpanel(Maincontainer maincontainer) {
        setLayout(null);
        setBackground(changeColor(Color.LIGHT_GRAY));
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        this.maincontainer = maincontainer;

        HEIGHT = maincontainer.getConfiguredHeight();
        WIDTH = maincontainer.getConfiguredWidth();

        scorepanel = new JTextPane();
        scorepanel.setEditable(false);
        scorepanel.setBounds(10, 10, WIDTH-20, 3*(HEIGHT-20)/4);
        scorepanel.setBorder(BorderFactory.createLineBorder(changeColor(Color.GRAY), 1));
        scorepanel.setOpaque(false);
        scorepanel.setMargin(new Insets(10, 10, 10, 10));
        drawScore();

        JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new GridLayout());
        buttonpanel.setOpaque(false); // 투명 배경

            JLabel exitbutton = new JLabel("Return to Main", SwingConstants.CENTER);
            exitbutton.setFont(new Font("Courier", Font.BOLD, 16));
            exitbutton.setForeground(changeColor(Color.BLACK));
            exitbutton.setBackground(changeColor(Color.GRAY));
            exitbutton.setOpaque(true);

        Dimension labelSize = exitbutton.getPreferredSize();
        int panelWidth = labelSize.width + 20;
        int panelHeight = labelSize.height + 20;
        buttonpanel.setBounds((WIDTH-panelWidth)/2, (3*(HEIGHT-20)/4+10)+((HEIGHT-10)-(3*(HEIGHT-20)/4+10)-panelHeight)/2, panelWidth, panelHeight);
        buttonpanel.add(exitbutton);

        add(scorepanel);
        add(buttonpanel);

        playerKeyListener = new PlayerKeyListener();
		addKeyListener(playerKeyListener);
		setFocusable(true);
		requestFocus();
    }
    
    public void drawScore() {
        List<ScoreStore.Score> loadedScores = ScoreStore.readRanked();
        int totalusernum = loadedScores.size();
        StringBuffer sb = new StringBuffer();
        int counter = 1;
        for (int j = 0; j < Math.min(totalusernum, 10); j++) {
            sb.append(counter + ".\t" + loadedScores.get(j).username() + ": " + loadedScores.get(j).score() + " (" + loadedScores.get(j).difficulty() + ")" + "\n");
            counter++;
        }
        for (int k = 0; k < 10 - Math.min(totalusernum, 10); k++) {
            sb.append(counter + ".\t" + "###: 0\n");
            counter++;
        }
        scorepanel.setText(sb.toString());
        StyledDocument doc = scorepanel.getStyledDocument();
        SimpleAttributeSet styleSet = new SimpleAttributeSet();
		StyleConstants.setFontSize(styleSet, 25);
		StyleConstants.setFontFamily(styleSet, "Courier");
        StyleConstants.setBold(styleSet, true);
		StyleConstants.setForeground(styleSet, changeColor(Color.BLACK));
		StyleConstants.setLineSpacing(styleSet, -0.1f);
        doc.setCharacterAttributes(0, doc.getLength(), styleSet, false);
        scorepanel.setStyledDocument(doc);
    }
    
    public class PlayerKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    selectEnter();
                    break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}

    protected void selectEnter() {
        this.maincontainer.exitScoreboardEnterStart();
    }
}
