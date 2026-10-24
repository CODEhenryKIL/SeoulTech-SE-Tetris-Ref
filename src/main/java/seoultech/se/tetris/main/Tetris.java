package seoultech.se.tetris.main;

import seoultech.se.tetris.component.Maincontainer;

public class Tetris {

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(() -> {
            Maincontainer maincontainer = new Maincontainer();
            maincontainer.enterStart();
        });
	}
}
