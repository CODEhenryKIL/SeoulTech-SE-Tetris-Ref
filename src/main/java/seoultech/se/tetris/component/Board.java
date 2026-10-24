package seoultech.se.tetris.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static seoultech.se.tetris.component.Maincontainer.changeColor;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;

import seoultech.se.tetris.blocks.Block;
import seoultech.se.tetris.blocks.AnchorBlock;
import seoultech.se.tetris.blocks.BombBlock;
import seoultech.se.tetris.blocks.ColumnBlock;
import seoultech.se.tetris.blocks.ItemBlock;
import seoultech.se.tetris.blocks.RowBlock;
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
    private SecondaryLoop currentLoop;
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
    private final java.util.BitSet clearingRows = new java.util.BitSet(HEIGHT);
    private Timer clearAnimationTimer;
    private long clearAnimationStarted;
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
		curr = blockGenerator.nextBlock(gamepanel.getDifficulty());
		next = blockGenerator.nextBlock(gamepanel.getDifficulty());
		this.gamepanel.drawNextBoard(next);
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
					textChunks.set((y+j+1)*(WIDTH+3)+x+i+1, new TextChunk(curr.getText(i, j), curr.getText(i, j).equals("O") ? curr.getColor() : Color.WHITE));
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
			addScore(DOWNPOINT * level);
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
			addScore(DOWNPOINT * level);
		}
		if (!lockAndAdvanceBlock()) return;
		drawBoard();
	}

	private boolean lockAndAdvanceBlock() {
        int clearedBefore = erasedLineNum;
		placeBlock();
        if (curr instanceof ItemBlock) useItem();
		eraseLine();

		if (isgameover()) return false;
		curr = next;
        next = gamepanel.gameMode.equals("Item") && erasedLineNum / 10 > clearedBefore / 10
            ? blockGenerator.nextItemBlock() : blockGenerator.nextBlock(gamepanel.getDifficulty());
		this.gamepanel.drawNextBoard(next);

		x = 3;
		y = 0;
		placeBlock();
		return true;
	}

	protected void useItem() {
		if (curr instanceof AnchorBlock) {
			eraseCurr();
			for (int i = y+curr.height(); i<HEIGHT; i++) {
				for (int j = x; j < x+curr.width(); j++) {
					board[i][j] = 0;
					int idx = (i+1)*(WIDTH+3)+j+1;
					textChunks.set(idx, new TextChunk(" ", Color.WHITE));
				}
			}
			y = HEIGHT - curr.height();
			placeBlock();
		} else if (curr instanceof BombBlock) {
			int startX = Math.max(0, x-2); int endX = Math.min(x+curr.width()+1, WIDTH-1);
			int startY = Math.max(0, y-2); int endY = Math.min(y+curr.height()+1, HEIGHT-1);
			for (int i = startY; i<=endY; i++) {
				for (int j = startX; j<=endX; j++) {
					board[i][j] = 0;
					int idx = (i+1)*(WIDTH+3)+j+1;
					textChunks.set(idx, new TextChunk(" ", Color.WHITE));
				}
			}
		} else if (curr instanceof RowBlock) {
			int itemY = ((RowBlock)curr).getItemY();
			eraseRowLine(y + itemY);
		} else if (curr instanceof ColumnBlock) {
			int itemX = ((ColumnBlock)curr).getItemX();
			eraseColumnLine(x + itemX);
		}
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
        java.util.BitSet fullRows = new java.util.BitSet(HEIGHT);
        for (int row = 0; row < HEIGHT; row++) {
            if (Arrays.stream(board[row]).allMatch(cell -> cell != 0)) fullRows.set(row);
        }
        if (!fullRows.isEmpty()) animateClearedRows(fullRows);
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

    private void animateClearedRows(java.util.BitSet rows) {
        stopAnimations();
        clearingRows.or(rows);
        clearAnimationStarted = System.nanoTime();
        clearAnimationTimer = new Timer(30, event -> {
            if (System.nanoTime() - clearAnimationStarted >= 210_000_000L) stopAnimations();
            repaint();
        });
        clearAnimationTimer.start();
    }

    void stopAnimations() {
        if (clearAnimationTimer != null) clearAnimationTimer.stop();
        clearAnimationTimer = null;
        clearingRows.clear();
    }

	private void eraseRowLine(int row) {
		int[] tmp;
		ArrayList<TextChunk> tmp_t = new ArrayList<>();
		eraseRowAnimation(row);
		SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
		this.currentLoop = loop;
		loop.enter();
		for (int j = row - 1; j >= 0; j--) {
			tmp = board[j];
			tmp_t = new ArrayList<>(textChunks.subList((j+1)*(WIDTH+3), (j+2)*(WIDTH+3)));
			board[j+1] = tmp;
			textChunks.subList((j+2)*(WIDTH+3), (j+3)*(WIDTH+3)).clear();
			textChunks.addAll((j+1)*(WIDTH+3), tmp_t);
		}
		board[0] = new int[WIDTH];
		for (int k = WIDTH+3+1; k < 2*(WIDTH+2); k++) textChunks.set(k, new TextChunk(" ", Color.WHITE));
	}

	private void eraseColumnLine(int col) {
		eraseColumnAnimation(col);
		SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
		this.currentLoop = loop;
		loop.enter();
		for (int i = 0; i<HEIGHT; i++) {board[i][col] = 0;}
	}

	protected void eraseRowAnimation(int lineIndex) {
		timer.stop();
		int[] start = {(lineIndex+1)*(WIDTH+3)+1};
		int[] end = {(lineIndex+2)*(WIDTH+3)-2};
		int[] middle = {start[0] + ((end[0] - start[0]) / 2)};
		int[] cnt = {0};
		Collections.fill(textChunks.subList(start[0], end[0]), new TextChunk("O", Color.WHITE));
		Timer animationTimer = new Timer(50, new ActionListener() {
			@Override 
			public void actionPerformed(ActionEvent e) {
				if (cnt[0] >= 4) {
					Timer t = (Timer) e.getSource();
					timer.start();
					t.stop();
					if (currentLoop != null) {
						currentLoop.exit();
					}
					return;
				}
				Collections.fill(textChunks.subList(middle[0], start[0]+end[0]-middle[0]), new TextChunk(" ", Color.WHITE));
				drawBoard();
				middle[0]--;
				cnt[0]++;
			}
		});
		animationTimer.start();
	}

	private void eraseColumnAnimation(int columnIndex) {
		timer.stop();
		int[] middle = {(HEIGHT / 2)-1};
		int[] cnt = {0};
		for (int i = 0; i<HEIGHT; i++) {textChunks.set((i+1)*(WIDTH+3)+columnIndex+1, new TextChunk("O", Color.WHITE));}
		Timer animationTimer = new Timer(50, new ActionListener() {
			@Override 
			public void actionPerformed(ActionEvent e) {
				if (cnt[0] >= HEIGHT/2) {
					Timer t = (Timer) e.getSource();
					timer.start();
					t.stop();
					if (currentLoop != null) {
						currentLoop.exit();
					}
					return;
				}
				int idx1 = (middle[0]+1)*(WIDTH+3)+columnIndex+1;
				int idx2 = (HEIGHT-middle[0])*(WIDTH+3)+columnIndex+1;
				textChunks.set(idx1, new TextChunk(" ", Color.WHITE));
				textChunks.set(idx2, new TextChunk(" ", Color.WHITE));
				drawBoard();
				middle[0]--;
				cnt[0]++;
			}
		});
		animationTimer.start();
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
                        stopAnimations();
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
        if (!clearingRows.isEmpty()) {
            g2d.setColor(new Color(255, 255, 255, 160));
            for (int row = clearingRows.nextSetBit(0); row >= 0; row = clearingRows.nextSetBit(row + 1)) {
                g2d.fillRect(fm.stringWidth(" "), (row + 1) * singleLineHeight,
                    fm.stringWidth(" ".repeat(WIDTH)), singleLineHeight);
            }
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
