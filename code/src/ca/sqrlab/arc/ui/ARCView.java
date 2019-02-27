package ca.sqrlab.arc.ui;

import java.awt.LayoutManager;

import javax.swing.JPanel;

public class ARCView extends JPanel {

	private static final long serialVersionUID = 4115331638864189522L;
	
	protected ARCWindow window;
	
	public ARCView() {}
	
	public ARCView(LayoutManager layout) {
		super(layout);
	}
}
