package seoultech.se.tetris.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import static seoultech.se.tetris.component.Maincontainer.changeColor;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;

import seoultech.se.tetris.blocks.Block;
import seoultech.se.tetris.blocks.BlockGenerator;
import seoultech.se.tetris.rules.CollisionRules;

public class Board extends JComponent {
	private static final long serialVersionUID = 2434035659171694595L;
	
	protected static final int HEIGHT = 20;
	protected static final int WIDTH = 10;
	protected static final char BORDER_CHAR = 'X';
	protected static final int LINEPOINT = 100;
	protected static final int DOWNPOINT = 1;
	private Gamepanel gamepanel;
	private int[][] board;
	private ArrayList<TextChunk> textChunks;
	protected Timer timer;
	private final BlockGenerator blockGenerator = new BlockGenerator();
	private Block curr;
	private boolean isPaused = false;
	private int score = 0;
	private Block next;
	private int erasedLineNum = 0;
	private int level = 1;
	int x = 3; //Default Position.
	int y = 0;
	
	private static final int initInterval = 1000;
	
	public Board(Gamepanel gamepanel) {
		//Board display setting.
		setBackground(changeColor(Color.BLACK));
		CompoundBorder border = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(changeColor(Color.GRAY), 10),
				BorderFactory.createLineBorder(changeColor(Color.DARK_GRAY), 5));
		setBorder(border);
		
		//Document default style.
		this.gamepanel = gamepanel;
		//Set timer for block drops.
		timer = new Timer(initInterval, new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				moveDown();
				drawBoard();
			}
		});
		
		//Initialize board for the game.
		board = new int[HEIGHT][WIDTH];
		textChunks = new ArrayList<>();	
	}

	protected void boardStart() {
		curr = blockGenerator.nextBlock();
		next = blockGenerator.nextBlock();
		this.gamepanel.drawNextBoard();
		StringBuffer sb = new StringBuffer();
		for(int t=0; t<WIDTH+2; t++) sb.append(BORDER_CHAR); // 윗쪽 보더
		sb.append("\n");
		for(int i=0; i < board.length; i++) {
			sb.append(BORDER_CHAR); // 왼쪽 보더
			for(int j=0; j < board[i].length; j++) sb.append(" ");
			sb.append(BORDER_CHAR); // 오른쪽 보더
			sb.append("\n");
		}
		for(int t=0; t<WIDTH+2; t++) sb.append(BORDER_CHAR); // 아래쪽 보더
		this.setText(sb.toString());
		placeBlock();
		drawBoard();
		timer.start();
	}

	protected void togglePause() {
		if (isPaused) {
			this.timer.start();
			gamepanel.gameRestart();
			isPaused = false;
		} else {
			this.timer.stop();
			gamepanel.gamePause();
			isPaused = true;
		}
	}

	private void placeBlock() {
		int dy = y; int dx = x;
		while(canDown(dx, dy)) dy++;
		for(int j=0; j<curr.height(); j++) {
			for(int i=0; i<curr.width(); i++) {
				if (curr.getShape(i, j) > 0 && textChunks.get((dy+j+1)*(WIDTH+3)+dx+i+1).text.equals(" ")) {
					textChunks.set((dy+j+1)*(WIDTH+3)+dx+i+1, new TextChunk("O", Color.DARK_GRAY));
				}
			}
		}
		for(int j=0; j<curr.height(); j++) {
			for(int i=0; i<curr.width(); i++) {
				if (curr.getShape(i, j) > 0) {
					board[y+j][x+i] += curr.getShape(i, j);
					textChunks.set((y+j+1)*(WIDTH+3)+x+i+1, new TextChunk("O", curr.getColor()));
				}
			}
		}
	}
	
	private void eraseCurr() {
		for(int j=0; j<curr.height(); j++) {
			for(int i=0; i<curr.width(); i++) {
				if (curr.getShape(i, j) > 0) {
					board[j+y][i+x] = 0;
					textChunks.set((y+j+1)*(WIDTH+3)+x+i+1, new TextChunk(" ", Color.WHITE));
				}
			}
		}
		int dy = y; int dx = x;
		while(canDown(dx, dy)) {
			dy++;
		}
		for(int j=0; j<curr.height(); j++) {
			for(int i=0; i<curr.width(); i++) {
				if (curr.getShape(i, j) > 0) {
					textChunks.set((dy+j+1)*(WIDTH+3)+dx+i+1, new TextChunk(" ", Color.WHITE));
				}
			}
		}
	}

	protected boolean canDown(int x, int y) {
		return CollisionRules.canDown(board, curr, x, y);
	}

	protected boolean canRight() {
		return CollisionRules.canRight(board, curr, x, y);
	}

	protected boolean canLeft() {
		return CollisionRules.canLeft(board, curr, x, y);
	}

	protected boolean canRotate() {
		return rotationPosition() != null;
	}

	private int[] rotationPosition() {
		return CollisionRules.rotationPosition(board, curr, x, y);
	}

	protected boolean moveDown() {
		eraseCurr();
		if (canDown(x, y)) {
			addScore(DOWNPOINT);
			y++;
			placeBlock();
			return true;
		}
		lockAndAdvanceBlock();
		return false;
	}

	protected void hardDrop() {  // 하드 드롭 기능 구현
		eraseCurr();
		while (canDown(x, y)) {
			y++;
			addScore(DOWNPOINT * 2);
		}
		if (!lockAndAdvanceBlock()) return;
		drawBoard();
	}

	private boolean lockAndAdvanceBlock() {
		placeBlock();
		eraseLine();

		if (isgameover()) return false;
		curr = next;
		next = blockGenerator.nextBlock();
		this.gamepanel.drawNextBoard();

		x = 3;
		y = 0;
		placeBlock();
		return true;
	}

	protected void moveRight() {
		eraseCurr();
		if (canRight()) x++;
		placeBlock();
	}

	protected void moveLeft() {
		eraseCurr();
		if (canLeft()) x--;
		placeBlock();
	}

	protected void rotate() {
		eraseCurr();
		int[] position = rotationPosition();
		if (position != null) {
			x = position[0];
			y = position[1];
			curr.rotate();
		}
		placeBlock();
	}

	protected void drawBoard() {
		this.repaint();
	}
	
	protected void reset() {
		this.board = new int[HEIGHT][WIDTH];
	}

	protected void eraseLine() {
		int[] tmp;
		ArrayList<TextChunk> tmp_t = new ArrayList<>();
		int combo = 1;
		for (int i = 0; i < HEIGHT; i++) {
			int[] line = board[i];
			if (!Arrays.stream(line).anyMatch(t -> t == 0)) {
				for (int j = i - 1; j >= 0; j--) {
					tmp = board[j];
					tmp_t = new ArrayList<>(textChunks.subList((j+1)*(WIDTH+3), (j+2)*(WIDTH+3)));
					board[j+1] = tmp;
					textChunks.subList((j+2)*(WIDTH+3), (j+3)*(WIDTH+3)).clear();
					textChunks.addAll((j+1)*(WIDTH+3), tmp_t);
				}
				board[0] = new int[WIDTH];
				for (int k = WIDTH+3+1; k < 2*(WIDTH+2); k++) textChunks.set(k, new TextChunk(" ", Color.WHITE));
				addScore(LINEPOINT * combo);
				combo++;
				if ((++erasedLineNum % 10) == 0) levelUp();
			}
		}
	}

	private void levelUp() {
		if (level < 19) level++;
		int ms = (int) Math.round((Math.pow((0.8 - (level - 1) * 0.007), (level - 1)) * 1000));
		timer.setDelay(ms);
	}

	protected void addScore(int POINT) {
		this.score += POINT;
		this.gamepanel.drawScore();
	}

	protected boolean isgameover() {
		for (int i = 0; i < next.height(); i++) {
			for (int j = 0; j < next.width(); j++) {
				if (board[i][j+3] + next.getShape(j, i) > 1) {
					if (timer != null) {
						timer.stop();
						timer = null;
						gamepanel.gameOver();
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override 
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (textChunks.isEmpty()) return;

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 1. 현재 설정된 보더의 두께(여백) 정보 가져오기
		Insets insets = getInsets();
        int availableWidth = getWidth() - insets.left - insets.right;
        int availableHeight = getHeight() - insets.top - insets.bottom;

        if (availableWidth <= 0 || availableHeight <= 0) return;

        // 2. 기준이 될 대형 고정 폰트 설정 (100포인트)
        Font baseFont = new Font("Monospaced", Font.BOLD, 100);
        FontMetrics fm = g2d.getFontMetrics(baseFont);

        // 3. [개행 호환 핵심] 가상의 100포인트 상태에서 전체 다중 행의 가로/세로 총 크기 계산
        int maxOriginalWidth = fm.stringWidth(" ".repeat(WIDTH+2));
        int singleLineHeight = (int) (fm.getHeight() * 0.8);
        int totalOriginalHeight = singleLineHeight * (HEIGHT+2); // 모든 줄의 높이 합산

        // 4. 컴포넌트 실제 크기 대비 늘려야 할 비율 계산
        double scaleX = (double) availableWidth / maxOriginalWidth;
        double scaleY = (double) availableHeight / totalOriginalHeight;

        // 5. 그래픽 변형(Scale) 적용 및 그리기
        AffineTransform oldTransform = g2d.getTransform();
        g2d.setFont(baseFont);
        g2d.setColor(changeColor(Color.BLACK));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.translate(insets.left, insets.top); // 원점 이동
        g2d.scale(scaleX, scaleY);               // 통째로 확대/축소

        // 6. 반복문을 돌며 한 줄씩 Y 좌표를 내려가며 그리기
        int currentY = fm.getAscent(); 
		int currentX = 0;
        for (TextChunk chunk : textChunks) {
			if ("\n".equals(chunk.text)) {
				currentX = 0; // 한 줄 안에서 글자가 이어질 X 좌표 변수
				currentY += singleLineHeight;
				continue;
			}
			g2d.setColor(changeColor(chunk.color)); // 콕 집은 그 색상으로 변경 🎨
			g2d.drawString(chunk.text, currentX, currentY); // 그리기
			currentX += fm.stringWidth(chunk.text); 
        }
        g2d.setTransform(oldTransform); // 그래픽 상태 복원
    }

	public void setText(String text) {
		if (text == null || text.isEmpty()) {
			this.textChunks = new ArrayList<>();
		} else {
			for (String str : text.split("")) {
				textChunks.add(new TextChunk(str, Color.WHITE));
			}
		}
		repaint();
	}

	public class TextChunk {
		String text = " ";
		Color color = Color.WHITE;

		public TextChunk(String text, Color color) {
			this.text = text;
			this.color = color;
		}
	}

	protected int getScore() {
		return score;
	}

	protected Block getNextBlock() {
		return next;
	}

	protected boolean getIsPaused() {
		return isPaused;
	}
}
