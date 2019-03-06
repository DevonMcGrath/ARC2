package ca.sqrlab.arc.tools.testing;

import java.util.ArrayList;
import java.util.List;

import ca.sqrlab.arc.io.ProcessResult;

public class TestResult {
	
	public int tests;
	
	public int failures;
	
	public int successes;
	
	private long executionTimeMillis;
	
	private long programTimeMillis;
	
	private String command;
	
	private ProcessResult processResult;
	
	private TestStatus status;
	
	private String[] failedMethods;
	
	private List<String> info;
	
	private List<String> warnings;
	
	private List<String> errors;
	
	public TestResult() {
		reset();
	}
	
	public void reset() {
		this.tests = 0;
		this.failures = 0;
		this.successes = 0;
		this.executionTimeMillis = 0;
		this.programTimeMillis = 0;
		this.command = "";
		this.status = TestStatus.UNKNOWN;
		this.processResult = null;
		this.failedMethods = new String[0];
		this.info = new ArrayList<>();
		this.warnings = new ArrayList<>();
		this.errors = new ArrayList<>();
	}
	
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public long getProgramTimeMillis() {
		return programTimeMillis;
	}

	public void setProgramTimeMillis(long programTimeMillis) {
		this.programTimeMillis = programTimeMillis;
	}

	public long getExecutionTimeMillis() {
		return executionTimeMillis;
	}
	
	public void setExecutionTimeMillis(long executionTimeMillis) {
		this.executionTimeMillis = executionTimeMillis;
	}

	public ProcessResult getProcessResult() {
		return processResult;
	}

	public void setProcessResult(ProcessResult processResult) {
		this.processResult = processResult;
	}

	public TestStatus getStatus() {
		return status == null? TestStatus.UNKNOWN : status;
	}
	
	public void setStatus(TestStatus status) {
		this.status = status;
	}

	public String[] getFailedMethods() {
		return failedMethods;
	}

	public void setFailedMethods(String[] failedMethods) {
		if (failedMethods == null) {
			failedMethods = new String[0];
		}
		this.failedMethods = failedMethods;
	}

	public List<String> getInfo() {
		return info;
	}
	
	public void addInfo(String msg) {
		if (msg != null) {
			this.info.add(msg);
		}
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	public void addWarning(String msg) {
		if (msg != null) {
			this.warnings.add(msg);
		}
	}
	
	public List<String> getErrors() {
		return errors;
	}
	
	public void addError(String msg) {
		if (msg != null) {
			this.errors.add(msg);
		}
	}
}
