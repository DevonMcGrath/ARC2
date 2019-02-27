package ca.sqrlab.arc.tools;

import java.util.ArrayList;
import java.util.List;

import ca.sqrlab.arc.io.ProcessResult;

public class ProcessStatus {
	
	private List<String> info;
	
	private List<String> warnings;
	
	private List<String> errors;
	
	private boolean fatalError;
	
	private String fatalErrorMessage;
	
	private ProcessResult result;
	
	public ProcessStatus() {
		this.info = new ArrayList<>();
		this.warnings = new ArrayList<>();
		this.errors = new ArrayList<>();
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

	public boolean hasFatalError() {
		return fatalError;
	}

	public void setFatalError(boolean fatalError) {
		this.fatalError = fatalError;
	}

	public String getFatalErrorMessage() {
		return fatalErrorMessage;
	}

	public void setFatalErrorMessage(String fatalErrorMessage) {
		this.fatalErrorMessage = fatalErrorMessage;
	}

	public ProcessResult getResult() {
		return result;
	}

	public void setResult(ProcessResult result) {
		this.result = result;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[fatalError=" + fatalError +
				", fatalErrorMessage='" + fatalErrorMessage + "']";
	}
}
