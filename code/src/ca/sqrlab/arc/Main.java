package ca.sqrlab.arc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.UIManager;

import ca.sqrlab.arc.experiments.*;
import ca.sqrlab.arc.io.FileReader;
import ca.sqrlab.arc.io.FileWriter;
import ca.sqrlab.arc.tools.SettingsManager;
import ca.sqrlab.arc.ui.ARCWindow;
import ca.sqrlab.arc.ui.MainView;

public class Main {
	
	private static final String DEFAULT_REPORT = "arc2-report.html";

	private static final String ARG_NO_GUI = "-nogui";
	
	private static final String ARG_HELP = "-help";
	
	private static final String ARG_ARC = "-arc";
	
	private static final String ARG_PROJECT = "-project";
	
	private static final String ARG_EXPERIMENT = "-experiment";
	
	private static final String ARG_REPORT_FILE = "-report";

	public static void main(String[] args) {
		
		Map<String, String> argsMap = parseArguments(args);
		
		// Check if help was requested
		if ("1".equals(argsMap.get(ARG_HELP))) {
			System.out.println("=== ARC2: Automatic Repair of Concurrency ===");
			System.out.println("GitHub: https://github.com/sqrlab/ARC2");
			System.out.println("Author: Devon McGrath\n");
			System.out.println("Arguments:");
			System.out.println("\t" + ARG_HELP + " prints this help message.");
			System.out.println("\t" + ARG_NO_GUI + " runs ARC2 without a GUI. "
					+ "The ARC and project paths must be provided or an "
					+ "experiment file.");
			System.out.println("\t" + ARG_ARC + " <arc_path> specifies the ARC path.");
			System.out.println("\t" + ARG_PROJECT + " <project_path> specifies"
					+ " the project path.");
			System.out.println("\t" + ARG_EXPERIMENT + " <experiment_file_path> "
					+ "specifies the path to the experiment file.");
			System.out.println("\t" + ARG_REPORT_FILE + " <report_file_path> "
					+ "specifies the experiment result's report file path.");
			System.out.println("\nIf an experiment file is provided, then ARC2"
					+ " will run with no GUI.");
			
			System.exit(0);
		}
		
		// Check for an experiment file
		String expFile = argsMap.get(ARG_EXPERIMENT);
		String reportFile = argsMap.get(ARG_REPORT_FILE);
		if (!expFile.isEmpty()) {
			runExperiments(parseExperimentFile(expFile), reportFile);
			System.exit(0);
		}
		
		String arcPath = argsMap.get(ARG_ARC);
		String projectPath = argsMap.get(ARG_PROJECT);
		
		// No GUI, run a single experiment
		if ("1".equals(argsMap.get(ARG_NO_GUI))) {
			
			// Check if the arguments are valid
			if (!checkNoGUI(arcPath, projectPath)) {
				System.exit(2);
			}
			
			// Create an experiment
			Experiment e = new Experiment()
					.setName("Command Line Argument Experiment");
			ExperimentGroup g = new ExperimentGroup(arcPath, projectPath)
					.setName("Command Line Argument Experiments")
					.setRunsPerExperiment(1)
					.addExperiment(e);
			List<ExperimentGroup> groups = new ArrayList<>();
			groups.add(g);
			
			// Run the experiments
			runExperiments(groups, reportFile);
			
			System.exit(0);
		}
		
		// Set the look and feel to the OS look and feel
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Create the UI
		ARCWindow win = new ARCWindow();
		MainView view = new MainView(arcPath, projectPath);
		win.setView(view);
		win.setVisible(true);
	}
	
	/**
	 * Parses the arguments provided, including both flags and key-value pairs.
	 * 
	 * @param args	the arguments (i.e. from {@link #main(String[])}).
	 * @return the map of arguments to their provided values.
	 * 
	 * @since 1.0
	 */
	private static Map<String, String> parseArguments(String[] args) {
		
		// Put the default values in the map
		TreeMap<String, String> argsMap = new TreeMap<>();
		argsMap.put(ARG_NO_GUI, "0");
		argsMap.put(ARG_HELP, "0");
		argsMap.put(ARG_ARC, "");
		argsMap.put(ARG_PROJECT, "");
		argsMap.put(ARG_EXPERIMENT, "");
		argsMap.put(ARG_REPORT_FILE, DEFAULT_REPORT);
		
		// Parse the arguments
		int n = args == null? 0 : args.length;
		for (int i = 0; i < n; i ++) {
			
			String a = args[i];
			String next = (i == n - 1)? "" : args[i + 1];
			
			// Flags
			if (a.equals(ARG_NO_GUI) || a.equals(ARG_HELP)) {
				argsMap.put(a, "1");
			}
			
			// Key-value pairs
			else if (a.equals(ARG_ARC) || a.equals(ARG_PROJECT) ||
					a.equals(ARG_EXPERIMENT) || a.equals(ARG_EXPERIMENT)) {
				argsMap.put(a, next);
				i++;
			}
		}
		
		return argsMap;
	}

	/**
	 * Parses an experiment file to get the experiments provided.
	 * 
	 * @param path	the path to the experiment file.
	 * @return the list of experiment groups as defined in the file.
	 * 
	 * @since 1.0
	 */
	private static List<ExperimentGroup> parseExperimentFile(String path) {
		
		// File does not exist
		List<ExperimentGroup> groups = new ArrayList<>();
		if (path == null || path.isEmpty()) {
			System.err.println("[parseExperimentFile]: error: empty path.");
			return groups;
		} if (!new File(path).isFile()) {
			System.err.println("[parseExperimentFile]: error: invalid path.");
			return groups;
		}
		
		List<String> lines = FileReader.read(path);
		if (lines == null || lines.isEmpty()) {
			return groups;
		}
		
		// Parse the lines
		ExperimentGroup g = null;
		Experiment e = null;
		SettingsManager s = null;
		boolean inExp = false;
		while (!lines.isEmpty()) {
			String line = lines.remove(0);
			
			boolean ge = line.startsWith("# [Experiment Group]");
			boolean ee = line.startsWith("# [Experiment]");
			
			// Not in an experiment group
			if (!ge && g == null) {
				continue;
			}
			
			// Start of a new experiment group
			if (ge) {
				if (g != null) {
					if (e != null) {
						g.addExperiment(e);
					}
					groups.add(g);
				}
				s = new SettingsManager();
				g = new ExperimentGroup()
						.setRunsPerExperiment(1)
						.setSettings(s);
				e = null;
				inExp = false;
			}
			
			// Start of an experiment
			else if (ee) {
				if (e != null) {
					g.addExperiment(e);
					if (inExp) {
						e.setSettings(s);
					}
				} if (!inExp) {
					g.setSettings(s);
				}
				e = new Experiment().setName("(Not set)");
				inExp = true;
				s = new SettingsManager();
				e.setSettings(s);
			}
			
			// General
			else {
				
				// Name
				if (line.startsWith("NAME=")) {
					String name = line.substring(5);
					if (inExp) {
						e.setName(name);
					} else {
						g.setName(name);
					}
				}
				
				// ARC path
				else if (!inExp && line.startsWith("ARC_PATH=")) {
					g.setArcPath(line.substring(9));
				}
				
				// Project path
				else if (!inExp && line.startsWith("PROJECT_PATH=")) {
					g.setProjectPath(line.substring(13));
				}
				
				// Runs
				else if (!inExp && line.startsWith("RUNS=")) {
					int runs = 1;
					try {
						runs = Integer.parseInt(line.substring(5));
					} catch (NumberFormatException err) {
						err.printStackTrace();
					}
					g.setRunsPerExperiment(runs);
				}
				
				// Just a setting
				else {
					s.addSetting(line);
				}
			}
		}
		if (g != null) {
			if (e != null) {
				g.addExperiment(e);
			}
			groups.add(g);
		}
		
		return groups;
	}
	
	/**
	 * Runs the specified groups of experiments and generates a report file.
	 * If the report file is not specified (i.e. null or an empty string),
	 * the {@link #DEFAULT_REPORT} file path is used.
	 * 
	 * @param groups		the groups of experiments to execute.
	 * @param reportFile	the report file name.
	 * @since 1.0
	 */
	private static void runExperiments(List<ExperimentGroup> groups, String reportFile) {
		
		for (ExperimentGroup group : groups) {
			System.out.println("GROUP NAME: " + group.getName());
			System.out.println("ARC PATH: " + group.getArcPath());
			System.out.println("PROJECT PATH: " + group.getProjectPath());
			group.getSettings().printSettings();
			Experiment[] exps = group.getExperiments();
			System.out.println();
			for (Experiment e : exps) {
				System.out.println("EXPERIMENT NAME: " + e.getName());
				e.getSettings().printSettings();
			}
			System.out.println("\n\n");
		}
		
		// Check if there are any
		if (groups == null || groups.isEmpty()) {
			System.err.println("No experiments to run.");
			System.exit(1);
		}
		
		// Check the report file
		if (reportFile == null || reportFile.isEmpty()) {
			System.out.println("WARNING: no specified value for the report "
					+ "file argument '" + ARG_REPORT_FILE + "', using default "
					+ "'" + DEFAULT_REPORT + "'.");
			reportFile = DEFAULT_REPORT;
		}
		
		// Execute the experiments
		ReportBuilder report = new ReportBuilder()
				.addPageTitle("ARC2 Experiment Report");
		for (ExperimentGroup group : groups) {
			report.addExperimentGroup(group, group.run());
		}
		List<String> data = new ArrayList<String>();
		data.add(report.getHTML());
		FileWriter.write(reportFile, data, false);
	}
	
	/**
	 * Checks if the specified paths are valid and initialized.
	 * 
	 * @param arcPath		the ARC path to test.
	 * @param projectPath	the project path to test.
	 * @return true if and only if both paths are valid and initialized.
	 * 
	 * @since 1.0
	 */
	private static boolean checkNoGUI(String arcPath, String projectPath) {
		
		boolean err = false, arcValid = true, prValid = true;
		
		// Check the ARC path
		if (arcPath == null || arcPath.isEmpty()) {
			System.err.println("FATAL ERROR: no ARC path specified for argument '"
					+ ARG_ARC + "'!");
			err = true;
			arcValid = false;
		} else if (!new File(arcPath).isDirectory()) {
			System.err.println("FATAL ERROR: the specified ARC directory '"
					+ arcPath + "' does not exist!");
			err = true;
			arcValid = false;
		}
		
		// Check the project path
		if (projectPath == null || projectPath.isEmpty()) {
			System.err.println("FATAL ERROR: no project path specified for argument '"
					+ ARG_PROJECT + "'!");
			err = true;
			prValid = false;
		} else if (!new File(projectPath).isDirectory()) {
			System.err.println("FATAL ERROR: the specified project directory '"
					+ projectPath + "' does not exist!");
			err = true;
			prValid = false;
		}
		
		// Check if the ARC directory is initialized
		if (arcValid && !ARC.isInitialized(arcPath)) {
			System.err.println("FATAL ERROR: the specified ARC directory '"
					+ arcPath + "' is missing an ARC2 config file: '"
					+ ARC.ARC_CONFIG_FILE + "!");
			err = true;
			arcValid = false;
		}
		
		// Check if the project directory is initialized
		if (prValid && !Project.isInitialized(projectPath)) {
			System.err.println("FATAL ERROR: the specified project directory '"
					+ projectPath + "' is missing an ARC2 project config file: '"
					+ Project.PROJECT_CONFIG_FILE + "!");
			err = true;
			prValid = false;
		}
		
		return !err;
	}
}
