package ca.sqrlab.arc;

import javax.swing.UIManager;

import ca.sqrlab.arc.ui.ARCWindow;
import ca.sqrlab.arc.ui.MainView;

public class Main {

	public static void main(String[] args) {
		
		// Set the look and feel to the OS look and feel
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Create the UI
		ARCWindow win = new ARCWindow();
		MainView view = new MainView("/Users/devon/Documents/school/thesis/new-arc");
		win.setView(view);
		win.setVisible(true);
	}

}
