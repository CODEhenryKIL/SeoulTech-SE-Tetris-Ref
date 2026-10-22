package seoultech.se.tetris.component;

import static seoultech.se.tetris.component.Maincontainer.changeColor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.CompoundBorder;

import seoultech.se.tetris.blocks.Block;

public class Nextblockpanel extends JComponent{

    private ArrayList<TextChunk> textChunks;

    public Nextblockpanel() {
        this.textChunks = new ArrayList<>();
        setBackground(changeColor(Color.BLACK));
        CompoundBorder border = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(changeColor(Color.GRAY), 10),
			BorderFactory.createLineBorder(changeColor(Color.DARK_GRAY), 5));
		setBorder(border);
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
        int maxOriginalWidth = fm.stringWidth(" ".repeat(6));
        int singleLineHeight = (int) (fm.getHeight() * 0.8);
        int totalOriginalHeight = singleLineHeight * (6); // 모든 줄의 높이 합산

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

    private class TextChunk {
		String text = " ";
		Color color = Color.WHITE;

		public TextChunk(String text, Color color) {
			this.text = text;
			this.color = color;
		}
	}

    private void setText(String text, Color color) {
		if (text == null || text.isEmpty()) {
			this.textChunks = new ArrayList<>();
		} else {
            textChunks = new ArrayList<>();
			for (String str : text.split("")) {
                Color c = color;
                if (!str.equals("O")) c = Color.WHITE;
				textChunks.add(new TextChunk(str, c));
			}
		}
		repaint();
	}

    protected void drawNextBoard(Block next) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 6; i++) sb.append(" ");
        sb.append("\n");
        for (int i = 0; i < 6; i++) sb.append(" ");
        sb.append("\n");
        for (int i = 0; i < next.height(); i++) {
            sb.append(" ");
            for (int j = 0; j < next.width(); j++) {
                if (next.getShape(j, i) == 0) sb.append(" ");
                else sb.append(next.getText(j, i));
            }
            sb.append("\n");
        }
        setText(sb.toString(), next.getColor());
    }
}
