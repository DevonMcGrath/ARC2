package ca.sqrlab.arc.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.io.FileUtils;

public class MainView extends ARCView {

	private static final long serialVersionUID = 3359252407247743000L;
	
	private String arcPath;
	
	private String projectPath;
	
	private boolean canContinue;
	
	private JLabel pathLabel;
	
	private JButton changeBtn;
	
	private JButton initBtn;

	public MainView() {
		this(System.getProperty("user.dir"));
	}
	
	public MainView(String arcPath) {
		super(new BorderLayout());
		init();
		setArcPath(arcPath);
	}
	
	public MainView(String arcPath, String projectPath) {
		this(arcPath);
		this.projectPath = projectPath;
	}
	
	private void init() {
		
		this.arcPath = "";
		super.setBackground(Color.WHITE);
		ComponentListener cl = new ComponentListener();
		
		// Create the top panel
		JPanel top = new JPanel(new GridLayout(1, 0, 0, 0));
		top.setBackground(Color.WHITE);
		top.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		Font fontTitleSmall = new Font("Arial", Font.BOLD, 20);
		Font fontTitleLarge = new Font("Arial", Font.BOLD, 30);
		Color sqrlabColorDark = new Color(0, 51, 102);
		JLabel sqrlab = new JLabel("SQR LAB");
		sqrlab.setForeground(sqrlabColorDark);
		sqrlab.setFont(fontTitleLarge);
		sqrlab.setHorizontalAlignment(JLabel.RIGHT);
		JLabel title = new JLabel("Automatic Repair of Concurrency");
		title.setForeground(sqrlabColorDark);
		title.setFont(fontTitleSmall);
		top.add(title);
		top.add(sqrlab);
		
		// Create the bottom panel
		JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 0));
		bottom.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		bottom.setBackground(Color.WHITE);
		JPanel b1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		b1.setBackground(Color.WHITE);
		b1.add(new JLabel("ARC Path: "));
		this.pathLabel = new JLabel(arcPath);
		b1.add(pathLabel);
		bottom.add(b1);
		
		JPanel b2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		b2.setBackground(Color.WHITE);
		this.changeBtn = new JButton("Change");
		this.changeBtn.addActionListener(cl);
		this.initBtn = new JButton("Initialize");
		this.initBtn.addActionListener(cl);
		b2.add(changeBtn);
		b2.add(initBtn);
		bottom.add(b2);
		
		// Add the main panels to the layout
		add(top, BorderLayout.NORTH);
		add(bottom, BorderLayout.SOUTH);
	}

	public String getArcPath() {
		return arcPath;
	}

	public void setArcPath(String arcPath) {
		this.arcPath = FileUtils.asValidPath(arcPath);
		this.pathLabel.setText(this.arcPath);
		this.updateButtons();
	}
	
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public void updateButtons() {
		
		// Check if the directory has been initialized for ARC
		this.canContinue = ARC.isInitialized(arcPath);
		this.initBtn.setText(canContinue? "Continue" : "Initialize");
	}
	
	public boolean changeArcPath() {
		
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(new File(arcPath));
		int result = fc.showOpenDialog(this);
		 
		// User selected a directory
		if (result == JFileChooser.APPROVE_OPTION) {
			File dir = fc.getSelectedFile();
			setArcPath(dir.getAbsolutePath());
			return true;
		}
		
		return false;
	}
	
	private class ComponentListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			// Change the directory ARC will use
			Object src = e.getSource();
			if (src == changeBtn) {
				if (changeArcPath()) {
					updateButtons();
				}
			}
			
			// Initialize or continue
			else if (src == initBtn) {
				
				// Initialize
				if (!canContinue) {
					ARC.initialize(arcPath);
					updateButtons();
				}
				
				// Continue to main part of ARC
				else {
					window.setView(new ProjectView(arcPath, projectPath));
				}
			}
		}
	}
}
