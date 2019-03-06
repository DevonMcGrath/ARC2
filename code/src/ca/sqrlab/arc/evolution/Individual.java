package ca.sqrlab.arc.evolution;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.tools.ARCUtils;
import ca.sqrlab.arc.tools.testing.TestResult;
import ca.sqrlab.arc.tools.testing.TestRunner;
import ca.sqrlab.arc.tools.testing.TestingSummary;

public class Individual {
	
	/** The ID of the individual, which is unique within each generation. */
	private int id;
	
	/** The generation in which this individual exists. */
	private int generation;
	
	/** The path to this individual on the file-system. */
	private String path;
	
	private int srcGeneration = -1;
	
	private int srcIndividual = -1;
	
	/** The last results from testing this individual. */
	private TestingSummary testSummary;
	
	private List<Mutation> mutations;
	
	public Individual() {
		this.mutations = new ArrayList<>();
	}
	
	public Individual(int id, int generation, String path) {
		this.mutations = new ArrayList<>();
		this.id = id;
		this.generation = generation;
		this.path = path;
	}
	
	/**
	 * Tests this individual by using it's test-suite.
	 * 
	 * @param arc	the current ARC with all the settings.
	 * @param runs	the number of runs to test with.
	 * @return true if and only if tests were run.
	 * @since 1.0
	 */
	public boolean test(ARC arc, int runs) {
		
		// Check the arguments
		if (arc == null || runs < 1) {
			return false;
		}
		
		// Check that the path exists
		if (path == null || !(new File(path)).isDirectory()) {
			return false;
		}
		
		// Copy over the project
		if (!ARCUtils.copyProjectSourceFiles(arc, path,
				arc.getSetting(ARC.SETTING_PROJECT_DIR), null)) {
			return false;
		}
		
		// Run the program test-suite the specified number of times
		TestRunner runner = new TestRunner(arc);
		this.testSummary = runner.execute(runs, false);
		
		return true;
	}
	
	public boolean hasBeenTested() {
		return testSummary != null && testSummary.getNumberOfTestsRun() > 0;
	}
	
	public boolean exists() {
		return path != null && (new File(path)).isDirectory();
	}
	
	public float getScore() {
		
		// No tests
		if (!hasBeenTested()) {
			return Float.NEGATIVE_INFINITY;
		}
		
		// Total the results
		TestResult[] results = testSummary.getResults();
		int passes = 0, testsRan = 0, n = results.length;
		for (int i = 0; i < n; i ++) {
			TestResult r = results[i];
			passes += r.successes;
			testsRan += r.tests;
		}
		
		/*
		 Possible Fitness Functions:
		 
		 F1 = # of JUnit tests which ALL pass / # of JUnit test cases
		    = passed / totalUnitTests
		 - Not good enough on it's own (would need more unit tests)
		 
		 F2 = sum of all test result successes / # of test suite executions
		    = passes / testsRan
		 
		 F3 = (F1 + F2) / 2
		 - Combined fitness function
		 */
		
		// Calculate the fitness of the individual
		/*String[] failed = testSummary.getFailedMethods();
		int totalUnitTests = testSummary.getUnitTestCount();
		int passed = totalUnitTests;
		if (failed != null) {
			passed -= failed.length;
		}
		float F1 = (float) passed / totalUnitTests;*/
		float F2 = (float) passes / testsRan;
		//float F3 = (F1 + F2) / 2;
		
		return F2;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getGeneration() {
		return generation;
	}

	public void setGeneration(int generation) {
		this.generation = generation;
	}

	public int getSourceGeneration() {
		return srcGeneration;
	}

	public void setSourceGeneration(int srcGeneration) {
		this.srcGeneration = srcGeneration;
	}

	public int getSourceIndividual() {
		return srcIndividual;
	}

	public void setSourceIndividual(int srcIndividual) {
		this.srcIndividual = srcIndividual;
	}
	
	public void setSource(int generation, int individual) {
		this.srcGeneration = generation;
		this.srcIndividual = individual;
	}

	public TestingSummary getTestSummary() {
		return testSummary;
	}

	public void setTestSummary(TestingSummary testSummary) {
		this.testSummary = testSummary;
	}

	public List<Mutation> getMutations() {
		return mutations;
	}

	public void setMutations(List<Mutation> mutations) {
		this.mutations = mutations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Individual other = (Individual) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
}
