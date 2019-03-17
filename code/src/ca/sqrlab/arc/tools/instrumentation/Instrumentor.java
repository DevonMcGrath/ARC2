package ca.sqrlab.arc.tools.instrumentation;

import java.io.File;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.Project;
import ca.sqrlab.arc.tools.monitoring.Logger;

/**
 * The {@code Instrumentor} class acts as a framework to instrument a project
 * with noise.
 * 
 * <p>Noising introduces random thread delays - typically near shared
 * variable access, critical regions, etc. These delays can be introduced at
 * two levels: source code and byte code. If the delays are at the source code
 * level, they typically use {@link Thread#sleep(long)} with random values
 * (determined at runtime).
 * 
 * <p>By noising a program, it forces different schedules with each run -
 * which can aid in finding concurrency bugs such as data races.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public abstract class Instrumentor {
	
	/**
	 * Checks to see if the specified project root exists and contains files.
	 * 
	 * @param projectRoot	the directory path to the root of the project.
	 * 
	 * @return true if and only if the project root is a directory and is not
	 * empty.
	 * @since 1.0
	 */
	private boolean isValidProjectRoot(String projectRoot) {
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
	
	/**
	 * Attempts to instrument the specified project's source files. If the
	 * process fails, the logger returned will have a fatal error.
	 * 
	 * @param project	the project to instrument.
	 * @return the logger which tracked the instrumentation process.
	 * 
	 * @see Project#getSourceFiles()
	 * @see Logger#hasFatalError()
	 * @since 1.0
	 */
	public final Logger instrument(Project project) {
		
		// Check the project
		Logger result = new Logger("Instrumentation");
		result.debug("Using '" + getClass().getName() + "' to instrument.");
		if (project == null) {
			result.fatalError("Invalid project.");
			return result;
		}
		String projectRoot = project.getSetting(ARC.SETTING_PROJECT_DIR);
		result.debug("Project root: " + projectRoot);
		
		// Call the actual instrumentation process if valid
		if (isValidProjectRoot(projectRoot)) {
			
			// Check the dependencies for instrumentation
			checkDependencies(result);
			
			// No fatal error, so instrument
			if (!result.hasFatalError()) {
				runInstrumentation(result, project);
			}
		}
		
		// Not valid, don't instrument
		else {
			result.fatalError("Invalid project root '" +
					projectRoot + "'.");
		}
		
		return result;
	}
	
	/**
	 * Checks for required dependencies such as files, directories, processes
	 * etc. If a dependency is missing which is required in the
	 * {@link #runInstrumentation(Logger, Project)} step, a fatal error should
	 * be logged in the logger.
	 * 
	 * @param result	the logger to keep track of the instrumentation process.
	 * 
	 * @see Logger#fatalError(String)
	 * @since 1.0
	 */
	protected abstract void checkDependencies(Logger result);
	
	/**
	 * Attempts to instrument the source files in the project. If any part of
	 * the process fails, a fatal error should be logged.
	 * 
	 * @param result	the logger to keep track of the instrumentation process.
	 * @param project	the project to instrument.
	 * 
	 * @see Logger#fatalError(String)
	 * @since 1.0
	 */
	protected abstract void runInstrumentation(Logger result, Project project);
}
