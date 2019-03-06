package ca.sqrlab.arc.evolution;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.tools.ARCUtils;
import ca.sqrlab.arc.tools.testing.TestResult;
import ca.sqrlab.arc.tools.testing.TestRunner;
import ca.sqrlab.arc.tools.testing.TestStatus;

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
	private TestResult[] results;
	
	/** The {@link #results} placed in a map based on the test status. */
	private Map<TestStatus, List<TestResult>> resultMap;
	
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
		if (!ARCUtils.copyProject(path,
				arc.getSetting(ARC.SETTING_PROJECT_DIR), null)) {
			return false;
		}
		
		// Run the program test-suite the specified number of times
		TestRunner runner = new TestRunner(arc);
		this.results = runner.execute(runs, false);
		
		// Place the results in a map
		int n = results.length;
		this.resultMap = new TreeMap<>();
		for (int i = 0; i < n; i ++) {
			TestResult tr = results[i];
			TestStatus ts = tr.getStatus();
			
			// Get the previous list
			if (resultMap.containsKey(ts)) {
				this.resultMap.get(ts).add(tr);
			} else { // not added yet
				List<TestResult> list = new ArrayList<>();
				list.add(tr);
				this.resultMap.put(ts, list);
			}
		}
		
		return true;
	}
	
	public boolean hasBeenTested() {
		return results != null && results.length > 0;
	}
	
	public boolean exists() {
		return path != null && (new File(path)).isDirectory();
	}
	
	public float getScore() {
		
		// No tests
		if (results == null || results.length == 0) {
			return Float.NEGATIVE_INFINITY;
		}
		
		// Total the results
		int passes = 0, fails = 0, testsRan = 0, n = results.length;
		for (int i = 0; i < n; i ++) {
			TestResult r = results[i];
			passes += r.successes;
			fails += r.failures;
			testsRan += r.tests;
		}
		
		/* TODO: keep track of each individual test
		 
		 F1 = # of JUnit tests which ALL pass / # of JUnit test cases
		 
		 F2 = passes / testsRan
		 
		 F3 = (F1 + F2) / 2
		 
		 */
		
		float F2 = (float) passes / testsRan;
		
		return F2;//TODO
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

	public TestResult[] getResults() {
		return results;
	}
	
	public Map<TestStatus, List<TestResult>> getResultMap() {
		return resultMap;
	}
	
	public List<TestResult> getResultsFor(TestStatus status) {
		if (status == null || resultMap == null) {
			return null;
		}
		return resultMap.get(status);
	}
	
	public int getNumberOfTestsRun() {
		return results == null? 0 : results.length;
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
