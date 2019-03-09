package ca.sqrlab.arc.evolution;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.Project;
import ca.sqrlab.arc.tools.ARCUtils;
import ca.sqrlab.arc.tools.mutation.TXLMutation;
import ca.sqrlab.arc.tools.testing.TestResult;
import ca.sqrlab.arc.tools.testing.TestRunner;
import ca.sqrlab.arc.tools.testing.TestingSummary;

/**
 * The {@code Individual} class represents an individual, which is a unique
 * version of the project being fixed. If the individual is not the original
 * project (i.e. {@link #getGeneration()} is not 0), then there were a set of
 * mutation operators applied to reach this version of the project.
 * 
 * @author Devon McGrath
 * @see ARCGeneticAlgorithm
 * @see Generation
 * @since 1.0
 */
public class Individual implements Comparable<Individual> {
	
	/** The ID of the individual, which is unique within each generation. */
	private int id;
	
	/** The generation in which this individual exists. */
	private int generation;
	
	/** The path to this individual on the file-system. */
	private String path;
	
	private Individual source;
	
	/** The last results from testing this individual. */
	private TestingSummary testSummary;
	
	/** The last mutation applied to the {@link #source} individual to get to
	 * this individual. */
	private TXLMutation mutation;
	
	/**
	 * Constructs an individual with no information.
	 * @since 1.0
	 */
	public Individual() {}
	
	/**
	 * Constructs an individual from the specified generation with the
	 * specified ID and file system path.
	 * 
	 * @param id			the unique ID (i.e. individual number) for the
	 * 						individual within the generation.
	 * @param generation	the generation number.
	 * @param path			the file system path to the root directory of this
	 * 						individual.
	 * @since 1.0
	 */
	public Individual(int id, int generation, String path) {
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
	 * 
	 * @see #hasBeenTested()
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
	
	/**
	 * Checks if this individual has been tested.
	 * 
	 * @return true if and only if there was at least one test run.
	 * @see #test(ARC, int)
	 * @since 1.0
	 */
	public boolean hasBeenTested() {
		return testSummary != null && testSummary.getNumberOfTestsRun() > 0;
	}
	
	/**
	 * Checks if this individual exists on the file system.
	 * 
	 * @return true if and only if the path is to a directory which exists.
	 * @see #getPath()
	 * @since 1.0
	 */
	public boolean exists() {
		return path != null && (new File(path)).isDirectory();
	}
	
	/**
	 * Gets the fitness score of the individual. A higher fitness score means
	 * that the individual performed better during testing.
	 * 
	 * @return the fitness score of the individual or
	 * {@link Float#NEGATIVE_INFINITY} if the individual has not been tested.
	 * @see #hasBeenTested()
	 * @see #getTestSummary()
	 * @since 1.0
	 */
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

	/**
	 * Gets the root path to the individual on the file system. The root
	 * directory for a project is the directory which contains the
	 * {@link Project#PROJECT_CONFIG_FILE}. For the individual, the root is on
	 * the same relative directory level as the project. For example:
	 * 
	 * <p>Assume the project root is: {@code /Users/user/ARC/input}<br>
	 * And the individual is located in: {@code /Users/user/ARC/tmp/0/0}<br>
	 * Then the individual's root is {@code /Users/user/ARC/tmp/0/0}.
	 * 
	 * @return the root directory path to the individual or null if it was not
	 * set.
	 * @see #setPath(String)
	 * @since 1.0
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the root directory path of the individual.
	 * 
	 * @param path	the directory path to the individual on the file system.
	 * @return a reference to this individual.
	 * @see #getPath()
	 * @since 1.0
	 */
	public Individual setPath(String path) {
		this.path = path;
		return this;
	}

	/**
	 * Gets the unique ID of this individual, which is simply the individual
	 * number in the generation. No two individuals within the same generation
	 * may have the same ID.
	 * 
	 * @return the individual's ID.
	 * 
	 * @see #setId(int)
	 * @see #getGeneration()
	 * @see #setGeneration(int)
	 * @since 1.0
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the ID of the individual.
	 * 
	 * @param id	the unique ID (i.e. individual number) for the
	 * 				individual within the generation.
	 * @return a reference to this individual.
	 * 
	 * @see #getId()
	 * @see #getGeneration()
	 * @see #setGeneration(int)
	 * @since 1.0
	 */
	public Individual setId(int id) {
		this.id = id;
		return this;
	}
	
	/**
	 * Gets the generation number which this individual belongs to.
	 * 
	 * @return the generation number.
	 * 
	 * @see #setGeneration(int)
	 * @see #getId()
	 * @see #setId(int)
	 * @since 1.0
	 */
	public int getGeneration() {
		return generation;
	}

	/**
	 * Sets the generation number which this individual belongs to.
	 * 
	 * @param generation	the generation number.
	 * @return a reference to this individual.
	 * 
	 * @see #getGeneration()
	 * @see #getId()
	 * @see #setId(int)
	 * @since 1.0
	 */
	public Individual setGeneration(int generation) {
		this.generation = generation;
		return this;
	}

	/**
	 * Gets the individual which this one originated from after a mutation was
	 * applied.
	 * 
	 * @return the source individual, or null if this individual represents the
	 * original project.
	 * @see #setSource(Individual)
	 * @since 1.0
	 */
	public Individual getSource() {
		return source;
	}

	/**
	 * Sets the source individual, which is the individual that was mutated to
	 * reach this individual.
	 * 
	 * @param source	the source individual.
	 * @return a reference to this individual.
	 * @see #getSource()
	 * @since 1.0
	 */
	public Individual setSource(Individual source) {
		this.source = source;
		return this;
	}

	/**
	 * Gets the testing summary from when this individual was last tested.
	 * 
	 * @return the test results or null if the individual was never tested.
	 * @see #test(ARC, int)
	 * @see #hasBeenTested()
	 * @since 1.0
	 */
	public TestingSummary getTestSummary() {
		return testSummary;
	}

	/**
	 * Gets a list of all the mutations applied the original project to reach
	 * this individual's state.
	 * 
	 * @return a list of mutations such that the first index is the first
	 * mutation applied and the last index is the last mutation applied. If
	 * this is the original project (or no mutations were set), an empty list
	 * is returned.
	 * 
	 * @see #getMutation()
	 * @see #setMutation(TXLMutation)
	 * @since 1.0
	 */
	public List<TXLMutation> getMutations() {
		
		// Build the list of mutations
		List<TXLMutation> mutations = new ArrayList<>();
		Individual individual = this;
		while (individual != null) {
			TXLMutation m = individual.mutation;
			if (m != null) {
				mutations.add(m);
			}
			individual = individual.source;
		}
		
		// Reverse the list
		List<TXLMutation> result = new ArrayList<>();
		int n = mutations.size();
		for (int i = n - 1; i >= 0; i --) {
			result.add(mutations.remove(i));
		}
		
		return result;
	}
	
	/**
	 * Gets the mutation which was applied to the source individual to reach
	 * this individual. If this individual represents the original project,
	 * the mutation should be null.
	 * 
	 * @return the mutation applied or null if no mutation was applied.
	 * 
	 * @see #setMutation(TXLMutation)
	 * @see #getMutations()
	 * @since 1.0
	 */
	public TXLMutation getMutation() {
		return mutation;
	}

	/**
	 * Sets the mutation which was applied to the source individual to reach
	 * this individual. If this individual represents the original project,
	 * there should not be a mutation.
	 * 
	 * @param mutation	the mutation applied.
	 * @return a reference to this individual.
	 * 
	 * @see #getMutation()
	 * @see #getMutations()
	 * @see #getSource()
	 * @since 1.0
	 */
	public Individual setMutation(TXLMutation mutation) {
		this.mutation = mutation;
		return this;
	}
	
	/**
	 * Compares this individual to another based on the fitness score.
	 * 
	 * @param obj	the other individual.
	 * @return 0 if the fitness scores are the same, 1 if the fitness score of
	 * this individual is better than the other or the other is null, or -1 if
	 * this individual's score is lower than the other individual's.
	 * 
	 * @see #getScore()
	 * @since 1.0
	 */
	@Override
	public int compareTo(Individual obj) {
		if (this == obj) {
			return 0;
		} if (obj == null) {
			return 1;
		}
		
		// Compare scores
		final float thisScore = getScore();
		final float thatScore = obj.getScore();
		if (thisScore == thatScore) {
			return 0;
		}
		
		return thisScore < thatScore? -1 : 1;
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
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id=" + id + ", generation="
				+ generation + ", tested=" + hasBeenTested() + ", path='"
				+ path + "']";
	}
}
