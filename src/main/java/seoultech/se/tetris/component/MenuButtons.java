package seoultech.se.tetris.component;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

final class MenuButtons<T extends JPanel> {
    private final List<T> buttons = new ArrayList<>();
    private final Insets insets;
    private int currentSelected;

    MenuButtons() {
        this(new Insets(0, 0, 0, 0));
    }

    MenuButtons(Insets insets) {
        this.insets = insets;
    }

    void add(JPanel parent, T button) {
        if (buttons.size() > 10) return; // 기존 등록 상한: 최대 11개.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = buttons.size();
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        button.setOpaque(buttons.isEmpty());
        parent.add(button, gbc);
        buttons.add(button);
    }

    void move(int direction) {
        selected().setOpaque(false);
        currentSelected = Math.floorMod(currentSelected + direction, buttons.size());
        selected().setOpaque(true);
    }

    T selected() {
        return buttons.get(currentSelected);
    }

    int selectedIndex() {
        return currentSelected;
    }
}
