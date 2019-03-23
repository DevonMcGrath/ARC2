package ca.sqrlab.arc.experiments;

import java.util.ArrayList;
import java.util.List;

import ca.sqrlab.arc.ARCRunner;
import ca.sqrlab.arc.evolution.ARCGeneticAlgorithm;
import ca.sqrlab.arc.evolution.Generation;
import ca.sqrlab.arc.evolution.Individual;
import ca.sqrlab.arc.tools.SettingsManager;
import ca.sqrlab.arc.tools.monitoring.Logger;
import ca.sqrlab.arc.tools.monitoring.Message;
import ca.sqrlab.arc.tools.monitoring.MessageType;
import ca.sqrlab.arc.tools.monitoring.Phase;
import ca.sqrlab.arc.tools.testing.TestingSummary;

public class Experiment {
	
	private String name;
	
	private SettingsManager settings;
	
	public ExperimentResult run(String arcPath, String projectPath) {
		
		// Run ARC
		ARCRunner runner = new ARCRunner(arcPath, projectPath);
		runner.setExtraSettings(settings);
		runner.startARCSync();
		
		// Get the results
		ARCGeneticAlgorithm ga = runner.getGeneticAlgorithm();
		int gens = 0, inCreated = 0, inEvaluated = 0, tsExes = 0;
		if (ga != null) {
			List<Generation> generations = ga.getGenerations();
			gens = generations.size();
			int runs = ga.getRuns();
			for (Generation g : generations) {
				List<Individual> pop = g.getPopulation();
				inCreated += pop.size();
				
				// Add all the test-suite executions for the generation
				for (Individual i : pop) {
					TestingSummary summary = i.getTestSummary();
					if (summary == null) {
						continue;
					}
					
					int n = summary.getNumberOfTestsRun();
					if (n > runs) {
						tsExes += runs;
					}
					tsExes += n;
				}
			}
			
			// Get the true number of evaluated individuals
			inEvaluated = inCreated;
			if (ga.foundFix()) {
				Generation last = generations.get(gens - 1);
				Individual fix = ga.getSolution();
				List<Individual> pop = last.getPopulation();
				inEvaluated -= (pop.size() - (pop.indexOf(fix) + 1));
			}
		}
		
		// Get any fatal errors
		List<String> errors = new ArrayList<>();
		Logger l = runner.getLogger();
		if (l.hasFatalError()) {
			List<Phase> phases = l.getPhases();
			for (Phase p : phases) {
				List<Message> msgs = p.getMessages();
				for (Message m : msgs) {
					if (m.getType() == MessageType.FATAL_ERROR) {
						errors.add(m.getFullMessageHTML());
					}
				}
			}
		}
		
		
		// Set the results
		ExperimentResult result = new ExperimentResult()
				.setARCExecutionTime(runner.getExecutionTime())
				.setGAExecutionTime(runner.getGAExecutionTime())
				.setFoundFix(runner.foundFix())
				.setGenerationCount(gens)
				.setIndividualsEvaluatedCount(inEvaluated)
				.setIndividualsGeneratedCount(inCreated)
				.setTestSuiteExecutions(tsExes)
				.setErrors(errors);
		
		return result;
	}
	
	public String getName() {
		return name;
	}
	
	public Experiment setName(String name) {
		this.name = name;
		return this;
	}

	public SettingsManager getSettings() {
		return settings;
	}

	public void setSettings(SettingsManager settings) {
		this.settings = settings;
	}
}
