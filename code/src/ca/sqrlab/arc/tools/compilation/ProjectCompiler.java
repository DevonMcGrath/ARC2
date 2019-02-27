package ca.sqrlab.arc.tools.compilation;

import java.io.File;

import ca.sqrlab.arc.tools.monitoring.Logger;

/**
 * The {@code ProjectCompiler} class acts as a compiler for a Java project.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public abstract class ProjectCompiler {
	
	/** The root directory for the Java project. */
	protected String projectRoot;
	
	/**
	 * Constructs a default compiler, using the current working directory.
	 * @since 1.0
	 */
	public ProjectCompiler() {
		this(".");
	}
	
	/**
	 * Constructs a compiler with the specified project path.
	 * @param projectRoot	the root directory of the Java project.
	 * @since 1.0
	 */
	public ProjectCompiler(String projectRoot) {
		this.projectRoot = projectRoot;
	}
	
	/**
	 * Checks to see if the specified project root exists and contains files.
	 * 
	 * @return true if and only if the project root is a directory and is not
	 * empty.
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
	
	/**
	 * Attempts to compile the project at the specified root path. If any error
	 * occurs during or before compilation is attempted, the returned object
	 * will have the fatal error flag set to true
	 * ({@link Logger#hasFatalError()}).
	 * 
	 * @return the result of the compilation.
	 * @since 1.0
	 */
	public Logger compile() {
		
		// Call the actual compilation process if valid
		Logger result = new Logger("Compilation");
		result.debug("Using '" + getClass().getName() + "' to compile.");
		if (isValidProjectRoot()) {
			
			// Check the dependencies for compilation
			checkDependencies(result);
			
			// No fatal error, so compile
			if (!result.hasFatalError()) {
				runCompiler(result);
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
	 * This method checks for required dependencies such as files and processes
	 * before compilation is attempted. If a fatal error occurs (such as a
	 * missing build file), this method should log a fatal error through
	 * {@link Logger#fatalError(String)}.
	 * 
	 * <p>This method is always called before
	 * {@link #runCompiler(Logger)}.
	 * 
	 * @param result	the current status of the compilation process.
	 * @see #runCompiler(Logger)
	 * @since 1.0
	 */
	protected abstract void checkDependencies(Logger result);
	
	/**
	 * Attempts to compile the project in the specified root directory. If a
	 * fatal error occurs, this method should log a fatal error through
	 * {@link Logger#fatalError(String)}.
	 * 
	 * <p>This method is always called after
	 * {@link #checkDependencies(Logger)}.
	 * 
	 * @param result	the current status of the compilation process.
	 * @see #checkDependencies(Logger)
	 * @since 1.0
	 */
	protected abstract void runCompiler(Logger result);
	
	/**
	 * Gets the project root. The project root is the directory which contains
	 * all the files required to compile the project (such as a build file and
	 * all the source files).
	 * 
	 * @return the project root.
	 * @since 1.0
	 */
	public String getProjectRoot() {
		return projectRoot;
	}

	/**
	 * Sets the root project directory.
	 * 
	 * @param projectRoot	the project directory path.
	 * @since 1.0
	 */
	public void setProjectRoot(String projectRoot) {
		this.projectRoot = projectRoot;
	}
}
