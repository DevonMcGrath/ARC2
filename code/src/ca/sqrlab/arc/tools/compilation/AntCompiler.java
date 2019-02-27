package ca.sqrlab.arc.tools.compilation;

import java.io.File;

import ca.sqrlab.arc.io.FileUtils;
import ca.sqrlab.arc.io.ProcessResult;
import ca.sqrlab.arc.tools.monitoring.Logger;

/**
 * The {@code AntCompiler} class compiles a program using ant. Ant uses a build
 * file, specifically {@value #BUILD_FILE} which is located in the root
 * directory of the project.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public class AntCompiler extends ProjectCompiler {
	
	/** The name of the build file ant uses to compile Java programs. */
	public static final String BUILD_FILE = "build.xml";
	
	/** The default name of the target to build the project. */
	public static final String DEFAULT_ANT_COMPILE_TARGET = "compile";
	
	/** The default path to the ant executable. */
	public static final String DEFAULT_ANT_PATH = "/usr/local/bin/ant";
	
	/** The path to the ant executable to use for compilation. */
	private String antProcessPath;
	
	/** The name of the target to build the project. */
	private String compileTarget;
	
	/**
	 * Creates an ant compiler for the specified project and compile target,
	 * with the default ant process path.
	 * 
	 * @param projectRoot	the project root directory path.
	 * @param compileTarget	the name of the target to compile the project.
	 * @since 1.0
	 */
	public AntCompiler(String projectRoot, String compileTarget) {
		this(projectRoot, compileTarget, DEFAULT_ANT_PATH);
	}
	
	/**
	 * Creates an ant compiler for the specified project and compile target,
	 * with the specified ant process path.
	 * 
	 * @param projectRoot		the project root directory path.
	 * @param compileTarget		the name of the target to compile the project.
	 * @param antProcessPath	the path to the ant executable.
	 * @since 1.0
	 */
	public AntCompiler(String projectRoot, String compileTarget,
			String antProcessPath) {
		super(projectRoot);
		setAntProcessPath(antProcessPath);
		setCompileTarget(compileTarget);
	}

	/**
	 * Checks that ant is an executable process and that the build file exists
	 * in the project directory.
	 */
	@Override
	protected void checkDependencies(Logger result) {
		
		// Check that ANT is set
		if (antProcessPath == null || antProcessPath.isEmpty()) {
			result.fatalError("Error: the ant process path "
					+ "has not been specified.");
		} else {
			File ant = new File(antProcessPath);
			if (!ant.canExecute()) {
				result.fatalError("Error: the ant process path '" +
				antProcessPath + "' is not a valid executable.");
			}
		}
		
		// Check that the build file exists
		char ds = FileUtils.getDirectorySeparator(projectRoot);
		File f = new File(projectRoot + ds + BUILD_FILE);
		if (!f.isFile()) {
			result.fatalError("Error: no build file '" + BUILD_FILE +
					"' was found in the project directory '" + projectRoot + "'.");
		}
	}

	/**
	 * Attempts to compile the program using the ant executable and the build
	 * file located in the project root directory.
	 */
	@Override
	protected void runCompiler(Logger result) {
		
		// Try to run the compile command
		try {
			String cmd = antProcessPath + " " + compileTarget;
			ProcessResult pr = new ProcessResult(Runtime.getRuntime().exec(
					cmd, null, new File(projectRoot)));
			pr.readStreams();
			
			// Check the output streams for errors
			result.debug("Executed command '" + cmd + "'.");
			String o = pr.getSTDOUT(), e = pr.getSTDERR();
			if (o == null) {
				o = "";
			} if (e == null) {
				e = "";
			}
			o = o.toLowerCase();
			e = e.toLowerCase();
			if (o.indexOf("build failed") >= 0 || e.indexOf("build failed") >= 0) {
				result.fatalError("Error: build failed (check "
						+ "dependencies such as JUnit to ensure they appear "
						+ "where they are expected to be).");
			}
			result.debug("STDOUT='" + pr.getSTDOUT() + "'");
			result.debug("STDERR='" + pr.getSTDERR() + "'");
			
		} catch (Exception e) { // process failed
			result.fatalError("Error: unable to compile using '" +
					antProcessPath + "'. " + e.getLocalizedMessage());
		}
		
	}

	/**
	 * Gets the path to the ant executable.
	 * 
	 * @return the ant process path.
	 * @since 1.0
	 */
	public String getAntProcessPath() {
		return antProcessPath;
	}

	/**
	 * Sets the full path to the ant executable.
	 * 
	 * @param antProcessPath	the path to the ant executable.
	 * @since 1.0
	 */
	public void setAntProcessPath(String antProcessPath) {
		this.antProcessPath = antProcessPath;
	}

	/**
	 * Gets the name of the target that will be used to compile the project.
	 * This value will never be null.
	 * 
	 * @return the compile target name.
	 * @since 1.0
	 */
	public String getCompileTarget() {
		return compileTarget;
	}

	/**
	 * Sets the name of the target that will be used to compile the project. If
	 * the value specified is null, the target name will be set to
	 * {@value #DEFAULT_ANT_COMPILE_TARGET}.
	 * 
	 * @param compileTarget	the name of the compile/build target.
	 * @since 1.0
	 */
	public void setCompileTarget(String compileTarget) {
		if (compileTarget == null || compileTarget.isEmpty()) {
			compileTarget = DEFAULT_ANT_COMPILE_TARGET;
		}
		this.compileTarget = compileTarget;
	}
}
