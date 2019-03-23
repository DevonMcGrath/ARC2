package ca.sqrlab.arc.experiments;

import java.util.List;

public class ExperimentResult {
	
	private long arcExecutionTime;
	
	private long gaExecutionTime;

	private int generationCount;
	
	private int individualsGeneratedCount;
	
	private int individualsEvaluatedCount;
	
	private int testSuiteExecutions;
	
	private boolean foundFix;
	
	private List<String> errors;

	public long getARCExecutionTime() {
		return arcExecutionTime;
	}

	public ExperimentResult setARCExecutionTime(long arcExecutionTime) {
		this.arcExecutionTime = arcExecutionTime;
		return this;
	}

	public long getGAExecutionTime() {
		return gaExecutionTime;
	}

	public ExperimentResult setGAExecutionTime(long gaExecutionTime) {
		this.gaExecutionTime = gaExecutionTime;
		return this;
	}

	public int getGenerationCount() {
		return generationCount;
	}

	public ExperimentResult setGenerationCount(int generationCount) {
		this.generationCount = generationCount;
		return this;
	}

	public int getIndividualsGeneratedCount() {
		return individualsGeneratedCount;
	}

	public ExperimentResult setIndividualsGeneratedCount(
			int individualsGeneratedCount) {
		this.individualsGeneratedCount = individualsGeneratedCount;
		return this;
	}

	public int getIndividualsEvaluatedCount() {
		return individualsEvaluatedCount;
	}

	public ExperimentResult setIndividualsEvaluatedCount(
			int individualsEvaluatedCount) {
		this.individualsEvaluatedCount = individualsEvaluatedCount;
		return this;
	}

	public boolean foundFix() {
		return foundFix;
	}

	public ExperimentResult setFoundFix(boolean foundFix) {
		this.foundFix = foundFix;
		return this;
	}

	public int getTestSuiteExecutions() {
		return testSuiteExecutions;
	}

	public ExperimentResult setTestSuiteExecutions(int testSuiteExecutions) {
		this.testSuiteExecutions = testSuiteExecutions;
		return this;
	}

	public List<String> getErrors() {
		return errors;
	}

	public ExperimentResult setErrors(List<String> errors) {
		this.errors = errors;
		return this;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[arcExecutionTime=" +
				arcExecutionTime + ", gaExecutionTime=" + gaExecutionTime
				+ ", generationCount=" + generationCount + ", individualsGeneratedCount="
				+ individualsGeneratedCount + ", individualsEvaluatedCount=" +
				individualsEvaluatedCount + ", testSuiteExecutions="
				+ testSuiteExecutions + ", foundFix=" + foundFix + ", errors=" + errors + "]";
	}
}
