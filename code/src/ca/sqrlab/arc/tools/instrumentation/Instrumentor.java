package ca.sqrlab.arc.tools.instrumentation;

import java.io.File;

import ca.sqrlab.arc.tools.ProcessStatus;

public abstract class Instrumentor {

	/** The root directory for the Java project. */
	protected String projectRoot;
	
	/**
	 * Constructs a default instrumentor, using the current working directory.
	 * @since 1.0
	 */
	public Instrumentor() {
		this(".");
	}
	
	/**
	 * Constructs an instrumentor with the specified project path.
	 * @param projectRoot	the root directory of the Java project.
	 * @since 1.0
	 */
	public Instrumentor(String projectRoot) {
		this.projectRoot = projectRoot;
	}
	
	/**
	 * Checks to see if the specified project root exists and contains files.
	 * 
	 * @return true if and only if the project root is a directory and is not
	 * empty.
	 * 
	 * @since 1.0
	 */
	public boolean isValidProjectRoot() {
		if (projectRoot == null || projectRoot.isEmpty()) {
			return false;
		}
		File dir = new File(projectRoot);
		if (!dir.isDirectory()) {
			return false;
		}
		File[] files = dir.listFiles();
		return files != null && files.length > 0;
	}
	
	public final ProcessStatus instrument() {
		
		// Call the actual instrumentation process if valid
		ProcessStatus result = new ProcessStatus();
		result.addInfo("Using '" + getClass().getName() + "' to instrument.");
		result.addInfo("Project root: " + projectRoot);
		if (isValidProjectRoot()) {
			
			// Check the dependencies for instrumentation
			checkDependencies(result);
			
			// No fatal error, so instrument
			if (!result.hasFatalError()) {
				runInstrumentation(result);
			}
		}
		
		// Not valid, don't instrument
		else {
			result.setFatalError(true);
			result.setFatalErrorMessage("Invalid project root '" +
					projectRoot + "'.");
		}
		
		return result;
	}
	
	protected abstract void checkDependencies(ProcessStatus result);
	
	protected abstract void runInstrumentation(ProcessStatus result);

	public String getProjectRoot() {
		return projectRoot;
	}

	public void setProjectRoot(String projectRoot) {
		this.projectRoot = projectRoot;
	}
}
