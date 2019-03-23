package ca.sqrlab.arc.experiments;

import java.io.PrintStream;

import ca.sqrlab.arc.tools.SettingsManager;

public class ExperimentGroup {
	
	private String name;

	private int runsPerExperiment;
	
	private SettingsManager settings;
	
	private Experiment[] experiments;
	
	private String arcPath;
	
	private String projectPath;
	
	public ExperimentGroup() {
		setExperiments(null);
	}
	
	public ExperimentGroup(String arcPath, String projectPath) {
		setExperiments(null);
		this.arcPath = arcPath;
		this.projectPath = projectPath;
	}
	
	public ExperimentResult[][] run() {
		return run(System.out);
	}
	
	public ExperimentResult[][] run(PrintStream out) {
		
		// Nothing to do
		int n = experiments.length;
		if (n == 0) {
			print(out, "No experiments.");
			return new ExperimentResult[0][0];
		} if (runsPerExperiment < 1) {
			print(out, "The number of executions per experiment is less than 1.");
			return new ExperimentResult[n][0];
		}
		
		// Run the experiments
		print(out, "Experiment Group: " + name);
		ExperimentResult[][] results = new ExperimentResult[n][runsPerExperiment];
		for (int i = 0; i < n; i ++) {
			Experiment e = experiments[i];
			print(out, "Experiment " + (i + 1) + " of " + n + ": " + e.getName());
			if (e.getSettings() != null) {
				e.getSettings().updateSettings(settings);
			}
			
			// Run the experiment the specified number of times
			for (int j = 0; j < runsPerExperiment; j ++) {
				print(out, (j + 1) + " of " + runsPerExperiment);
				results[i][j] = e.run(arcPath, projectPath);
				print(out, "Done. Results=" + results[i][j]);
			}
		}
		
		return results;
	}

	public String getName() {
		return name;
	}

	public ExperimentGroup setName(String name) {
		this.name = name;
		return this;
	}

	public int getRunsPerExperiment() {
		return runsPerExperiment;
	}

	public ExperimentGroup setRunsPerExperiment(int runsPerExperiment) {
		this.runsPerExperiment = runsPerExperiment;
		return this;
	}

	public SettingsManager getSettings() {
		return settings;
	}

	public ExperimentGroup setSettings(SettingsManager settings) {
		this.settings = settings;
		return this;
	}

	public Experiment[] getExperiments() {
		return experiments;
	}

	public ExperimentGroup setExperiments(Experiment[] experiments) {
		if (experiments == null) {
			experiments = new Experiment[0];
		}
		this.experiments = experiments;
		return this;
	}
	
	public ExperimentGroup addExperiment(Experiment experiment) {
		if (experiment == null) {
			return this;
		}
		
		// Add the experiment to the array
		int n = experiments.length;
		Experiment[] tmp = new Experiment[n + 1];
		for (int i = 0; i < n; i ++) {
			tmp[i] = experiments[i];
		}
		tmp[n] = experiment;
		this.experiments = tmp;
		
		return this;
	}

	public String getArcPath() {
		return arcPath;
	}

	public void setArcPath(String arcPath) {
		this.arcPath = arcPath;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	private static void print(PrintStream out, String msg) {
		if (out != null) {
			out.println(msg);
		}
	}
}
