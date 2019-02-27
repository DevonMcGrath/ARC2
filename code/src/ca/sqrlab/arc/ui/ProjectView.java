package ca.sqrlab.arc.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.ARCRunner;
import ca.sqrlab.arc.FinishListener;
import ca.sqrlab.arc.Project;
import ca.sqrlab.arc.io.FileUtils;
import ca.sqrlab.arc.tools.monitoring.Logger;
import ca.sqrlab.arc.tools.monitoring.Message;
import ca.sqrlab.arc.tools.monitoring.Phase;

public class ProjectView extends ARCView implements FinishListener {

	private static final long serialVersionUID = 5534951958158670494L;
	
	private static final int STATE_NOT_VALID = -1;
	
	private static final int STATE_NOT_INITIALIZED = 0;
	
	private static final int STATE_RUNNING = 1;
	
	private static final int STATE_NOT_RUNNING = 2;
	
	private ARCRunner runner;
	
	private String arcPath;
	
	private String projectPath;
	
	private int runState;
	
	private JButton backBtn;
	
	private JButton changePrBtn;
	
	private JButton runBtn;
	
	private JLabel arcPathLabel;
	
	private JLabel projectPathLabel;
	
	private JLabel errLabel;
	
	private JComboBox<String> messageFilter;
	
	private JScrollPane logScrollPane;
	
	private JTextPane logMessages;
	
	public ProjectView() {
		this(System.getProperty("user.dir"));
	}
	
	public ProjectView(String arcPath) {
		this(arcPath, arcPath);
		
		// Try to dynamically find a project
		if (ARC.isInitialized(this.arcPath) && !Project.isInitialized(projectPath)) {
			List<File> files = FileUtils.find(this.arcPath,
					Project.PROJECT_CONFIG_FILE_REGEX, true);
			if (!files.isEmpty()) {
				setProjectPath(files.get(0).getParentFile().getAbsolutePath());
				updateButtons();
			}
		}
	}
	
	public ProjectView(String arcPath, String projectPath) {
		super(new BorderLayout());
		this.runner = new ARCRunner();
		init();
		setArcPath(arcPath);
		setProjectPath(projectPath);
		this.runner.setOnFinish(this);
	}
	
	private void init() {
		
		this.arcPath = "";
		this.projectPath = "";
		super.setBackground(Color.WHITE);
		ComponentListener cl = new ComponentListener();
		
		// Create the top panel
		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
		top.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		top.setBackground(Color.WHITE);
		this.backBtn = new JButton("Back");
		this.backBtn.setToolTipText("Go back to the main screen");
		this.backBtn.addActionListener(cl);
		this.arcPathLabel = new JLabel(arcPath);
		top.add(backBtn);
		top.add(new JLabel("ARC Path: "));
		top.add(arcPathLabel);
		
		// Create the middle panel
		JPanel middle = new JPanel(new BorderLayout());
		middle.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
		middle.setBackground(Color.WHITE);
		String[] opts = {"Fatal Errors", "Errors", "Warnings", "Debug"};
		this.messageFilter = new JComboBox<>(opts);
		this.messageFilter.setSelectedIndex(opts.length - 1);
		this.messageFilter.addActionListener(cl);
		JPanel m1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		m1.setBackground(Color.WHITE);
		m1.add(new JLabel("Filter:"));
		m1.add(messageFilter);
		this.logMessages = new JTextPane();
		this.logMessages.setContentType("text/html");
		this.logMessages.setBackground(Color.WHITE);
		this.logMessages.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		this.logMessages.setEditable(false);
		this.logScrollPane = new JScrollPane(logMessages);
		middle.add(m1, BorderLayout.NORTH);
		middle.add(logScrollPane, BorderLayout.CENTER);
		
		// Create the bottom panel
		JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 0));
		bottom.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		bottom.setBackground(Color.WHITE);
		JPanel b1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		b1.setBackground(Color.WHITE);
		b1.add(new JLabel("Project Path: "));
		this.projectPathLabel = new JLabel(projectPath);
		b1.add(projectPathLabel);
		bottom.add(b1);
		
		JPanel b2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		b2.setBackground(Color.WHITE);
		this.changePrBtn = new JButton("Change Project");
		this.changePrBtn.addActionListener(cl);
		this.changePrBtn.setToolTipText("Change the directory of the project");
		this.runBtn = new JButton("Initialize");
		this.runBtn.addActionListener(cl);
		this.errLabel = new JLabel();
		this.errLabel.setForeground(Color.RED);
		b2.add(changePrBtn);
		b2.add(runBtn);
		b2.add(errLabel);
		bottom.add(b2);
		
		// Add the main panels to the layout
		add(top, BorderLayout.NORTH);
		add(middle, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
		
		updateButtons();
	}
	
	@Override
	public void onFinish(int id, Object obj) {
		updateLog();
		updateButtons();
	}
	
	/**
	 * Updates the component which displays the logs to display all the latest
	 * messages according to the filter.
	 * 
	 * @since 1.0
	 */
	public void updateLog() {
		
		// No log
		if (runner == null || runner.getLogger() == null) {
			this.logMessages.setText("");
			return;
		}
		Logger l = runner.getLogger();
		final int priority = messageFilter.getSelectedIndex();
		
		// Build the text
		String text = "";
		List<Phase> phases = l.getPhases();
		for (Phase p : phases) {
			
			// Add the messages from the phase
			List<Message> messages = p.getMessages();
			String pt = "<br /><b style=\"font-family: Arial, sans-serif;"
					+ "color: #003366;font-size: 16px;\">" + p.getName() + "</b><br />";
			int added = 0;
			
			for (Message m : messages) {
				if (m.getType().priority <= priority) {
					pt += m.getFullMessageHTML() + "<br />";
					added ++;
				}
			}
			
			// No messages based on filter
			if (added == 0) {
				pt += "(No messages match the filter)<br />";
			}
			
			text = pt + text;
		}
		text =  "<div style=\"font-family: Arial, sans-serif;\">" +
				text + "</div>";
		this.logMessages.setText(text);
	}
	
	public void updateButtons() {
		
		String txt = "Initialize";
		String tt = "Initialize the directory as a project", err = "";
		boolean enabled = true, cdEnabled = true;
		
		// Check if the directory has a build file
		if (!Project.hasBuildFile(projectPath)) {
			enabled = false;
			err = "Error: no build file, cannot initialize "
					+ "this directory as a project.";
			this.runState = STATE_NOT_VALID;
		}
		
		// Check if it has been initialized
		else if (Project.isInitialized(projectPath)) {
			
			// Check if ARC is already running
			if (runner != null && !runner.isFinished()) {
				txt = "Stop ARC";
				tt = "Stop executing ARC";
				this.runState = STATE_RUNNING;
				cdEnabled = false;
			} else {
				txt = "Start ARC";
				tt = "Start executing ARC";
				this.runState = STATE_NOT_RUNNING;
			}
		} else {
			this.runState = STATE_NOT_INITIALIZED;
		}
		
		
		// Update the UI
		this.errLabel.setText(err);
		this.runBtn.setText(txt);
		this.runBtn.setToolTipText(tt);
		this.runBtn.setEnabled(enabled);
		this.changePrBtn.setEnabled(cdEnabled);
	}
	
	public boolean changeProjectPath() {
		
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(new File(projectPath));
		int result = fc.showOpenDialog(this);
		 
		// User selected a directory
		if (result == JFileChooser.APPROVE_OPTION) {
			File dir = fc.getSelectedFile();
			setProjectPath(dir.getAbsolutePath());
			return true;
		}
		
		return false;
	}
	
	public String getArcPath() {
		return arcPath;
	}

	public void setArcPath(String arcPath) {
		this.arcPath = FileUtils.asValidPath(arcPath);
		this.arcPathLabel.setText(this.arcPath);
		this.runner.setArcPath(this.arcPath);
	}
	
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = FileUtils.asValidPath(projectPath);
		this.projectPathLabel.setText(this.projectPath);
		this.runner.setProjectPath(this.projectPath);
	}
	
	public ARCRunner getRunner() {
		return runner;
	}

	private class ComponentListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			// Go back to the main screen
			Object src = e.getSource();
			if (src == backBtn) {
				window.setView(new MainView(arcPath));
			}
			
			// Change the path to the project
			else if (src == changePrBtn) {
				if (changeProjectPath()) {
					updateButtons();
				}
			}
			
			// The run button was clicked
			else if (src == runBtn) {
				
				// Need to initialize
				if (runState == STATE_NOT_INITIALIZED) {
					// TODO create config file
				}
				
				// Start running
				else if (runState == STATE_NOT_RUNNING) {
					if (runner == null) {
						runner = new ARCRunner(arcPath, projectPath);
					} else {
						Logger l = runner.getLogger();
						if (l != null) {
							l.clear();
						}
					}
					updateLog();
					runner.startARC();
					updateButtons();
				}
				
				// Stop running
				else if (runState == STATE_RUNNING) {
					if (runner != null) {
						runner.stopARC();
					}
				}
			}
			
			// The message filter was changed
			else if (src == messageFilter) {
				updateLog();
			}
		}
	}
}
