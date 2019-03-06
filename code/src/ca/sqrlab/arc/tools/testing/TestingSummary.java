package ca.sqrlab.arc.tools.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The {@code TestingSummary} class takes a number of test results and gets
 * metrics on those set of test results. It computes the average test-suite
 * execution time ({@link #getAverageTime()}), the total number of test-suite
 * unit tests ({@link #getUnitTestCount()}), and the unique array of unit tests
 * which failed in one or more test results ({@link #getFailedMethods()}).
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public class TestingSummary {
	
	/** The number of unit tests in the test-suite. */
	private int unitTestCount;
	
	/** The test results summarized by this object. */
	private TestResult[] results;
	
	/** The map of the test results, mapping a test status to a list of test
	 * results which have that test status. */
	private Map<TestStatus, List<TestResult>> resultMap;
	
	/** The unique array of methods (test cases) which failed. */
	private String[] failedMethods;
	
	/** The average time for the test-suite to execute, in milliseconds. */
	private long averageTime;
	
	/**
	 * Constructs a testing summary with no test results.
	 * 
	 * @since 1.0
	 */
	public TestingSummary() {
		this(null);
	}
	
	/**
	 * Constructs a testing summary with the specified test results.
	 * 
	 * @param results	the test results.
	 * @since 1.0
	 */
	public TestingSummary(TestResult[] results) {
		setResults(results);
	}

	/**
	 * Gets the number of unit tests which were executed across each test
	 * result.
	 * 
	 * @return the number of unit tests.
	 * 
	 * @since 1.0
	 */
	public int getUnitTestCount() {
		return unitTestCount;
	}

	/**
	 * Gets the test results summarized by this testing summary. Note: even if
	 * no test results are included, the value returned is never null.
	 * 
	 * @return the test results.
	 * 
	 * @see #getResultMap()
	 * @see #getResultsFor(TestStatus)
	 * @since 1.0
	 */
	public TestResult[] getResults() {
		return results;
	}

	/**
	 * Sets the test results to be summarized by this testing summary.
	 * 
	 * @param results	the test results.
	 * @return a reference to this object.
	 * 
	 * @since 1.0
	 */
	public TestingSummary setResults(TestResult[] results) {
		if (results == null) {
			results = new TestResult[0];
		}
		this.results = results;
		
		// Calculate the results
		this.unitTestCount = 0;
		this.averageTime = 0;
		int n = results.length;
		this.resultMap = new TreeMap<>();
		List<String> failed = new ArrayList<>();
		for (int i = 0; i < n; i ++) {
			TestResult tr = results[i];
			TestStatus ts = tr.getStatus();
			
			// Add the results to a map
			if (resultMap.containsKey(ts)) { // the result type has been added
				this.resultMap.get(ts).add(tr);
			} else { // not added yet
				List<TestResult> list = new ArrayList<>();
				list.add(tr);
				this.resultMap.put(ts, list);
			}
			
			// Add the failed methods
			String[] testFailures = tr.getFailedMethods();
			if (testFailures != null) {
				for (String f : testFailures) {
					if (!failed.contains(f)) {
						failed.add(f);
					}
				}
			}
			
			// Check the test count
			if (unitTestCount < tr.tests) {
				this.unitTestCount = tr.tests;
			}
			
			this.averageTime += tr.getProgramTimeMillis();
		}
		this.averageTime /= n;
		
		// Create the array of unique failed methods/tests
		n = failed.size();
		this.failedMethods = new String[n];
		for (int i = 0; i < n; i ++) {
			this.failedMethods[i] = failed.get(i);
		}
		
		return this;
	}

	/**
	 * Gets the map of test results based on the test result status. Note: the
	 * map has no empty lists; i.e. if a test status never appeared, there is
	 * no entry for that status.
	 * 
	 * @return the map of test results.
	 * 
	 * @see #getResultsFor(TestStatus)
	 * @see #getResults()
	 * @since 1.0
	 */
	public Map<TestStatus, List<TestResult>> getResultMap() {
		return resultMap;
	}
	
	/**
	 * Gets the list of test results for the given test status. If no test
	 * results have the specified status, null is returned.
	 * 
	 * @param status	the test result status.
	 * @return the list of test results for the test status.
	 * 
	 * @see #getResultMap()
	 * @see #getResults()
	 * @since 1.0
	 */
	public List<TestResult> getResultsFor(TestStatus status) {
		if (status == null || resultMap == null) {
			return null;
		}
		return resultMap.get(status);
	}
	
	/**
	 * Gets the number of executions of the entire test-suite. Note: this is
	 * not the number of tests in the test-suite. For the number of tests in
	 * the overall test-suite, see {@link #getUnitTestCount()}.
	 * 
	 * @return the number of test-suite executions.
	 * 
	 * @since 1.0
	 */
	public int getNumberOfTestsRun() {
		return results == null? 0 : results.length;
	}
	
	/**
	 * Gets the average time for the program to execute the test-suite.
	 * 
	 * @return the average test result execution time, in milliseconds or 0 if
	 * there are no test results.
	 * 
	 * @see TestResult#getProgramTimeMillis()
	 * @since 1.0
	 */
	public long getAverageTime() {
		return averageTime;
	}
	
	/**
	 * Gets the unit tests which failed during the all the executions of the
	 * test-suite. Note: the values in the array are unique.
	 * 
	 * @return the names of the failed test-suite methods.
	 * 
	 * @since 1.0
	 */
	public String[] getFailedMethods() {
		return failedMethods;
	}
}
