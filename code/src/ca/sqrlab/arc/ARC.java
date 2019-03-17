package ca.sqrlab.arc;

import java.io.File;

import ca.sqrlab.arc.evolution.ARCGeneticAlgorithm;
import ca.sqrlab.arc.io.FileUtils;
import ca.sqrlab.arc.tools.SettingsManager;

/**
 * The {@code ARC} class stores all the general ARC settings, as well as the
 * project settings.
 * 
 * @author Devon McGrath
 * @see Project
 * @since 1.0
 */
public class ARC extends SettingsManager {
	
	/** The configuration file for ARC to use with standard cross-project values. */
	public static final String ARC_CONFIG_FILE = "arc-prog-config.txt";
	
	/** The default memory test processes are run with, in MB. */
	public static final int DEFAULT_PROGRAM_MB = 1024;
	
	/** The setting name for the JVM process path. */
	public static final String SETTING_JAVA = "JAVA";
	
	/** The setting name for the Java compiler process. */
	public static final String SETTING_JAVAC = "JAVAC";
	
	/** The setting name for the TXL process path. */
	public static final String SETTING_TXL = "TXL";
	
	/** The setting name for the Ant process path. */
	public static final String SETTING_ANT = "ANT";
	
	/** The setting name for the directory separator which is '\' on Windows
	 * and '/' on other systems. */
	public static final String SETTING_DIR_SEPARATOR = "S";
	
	/** The setting name for the root directory of ARC. */
	public static final String SETTING_ROOT = "ROOT";
	
	/** The setting name for the working project directory. This is not to be
	 * confused with {@link #SETTING_ORIGINAL_PROJECT_DIR}. */
	public static final String SETTING_PROJECT_DIR = "PROJECT_DIR";
	
	/** The setting name for the original project directory. This directory is
	 * not modified, unless it happens to be the same as
	 * {@link #SETTING_PROJECT_DIR}. */
	public static final String SETTING_ORIGINAL_PROJECT_DIR =
			"ORIGINAL_PROJECT_DIR";
	
	/** The setting name for the ARC directory, which contains all the
	 * temporary files/directories ARC needs to run. */
	public static final String SETTING_ARC_DIR = "ARC_DIR";
	
	/** The setting name for the directory to any required libraries such as
	 * JUnit. */
	public static final String SETTING_LIB_DIR = "LIB_DIR";
	
	/** The setting name for the path to the JUnit jar. */
	public static final String SETTING_JUNIT_JAR = "JUNIT_JAR";
	
	/** The setting name for the path to the Hamcrest jar. */
	public static final String SETTING_HAMCREST_JAR = "HAMCREST_JAR";
	
	/** The setting name for the path to the temporary directory. */
	public static final String SETTING_TMP_DIR = "TMP_DIR";
	
	/** The setting name for the directory which contains all the mutants. */
	public static final String SETTING_MUTANT_DIR = "MUTANT_DIR";
	
	/** The setting name for the directory which contains the TXL resources
	 * (i.e. operators, Java Grammar, etc.). */
	public static final String SETTING_TXL_DIR = "TXL_DIR";
	
	/** The setting name for the number of milliseconds until a program times
	 * out. */
	public static final String SETTING_TIMEOUT_MILLIS = "TIMEOUT_MILLIS";
	
	/** The setting name for the multiplier which is used to dynamically
	 * determine the timeout. */
	public static final String SETTING_TIMEOUT_MULTIPLIER = "TIMEOUT_MULT";
	
	/** The setting name for the class which contains the test suite. */
	public static final String SETTING_PROJECT_TESTSUITE = "PROJECT_TESTSUITE";
	
	/** The setting name for the classpath to invoke the JVM with when testing
	 * the project. */
	public static final String SETTING_PROJECT_CLASSPATH = "PROJECT_CLASSPATH";
	
	/** The setting name for the ant target name which compiles the project. */
	public static final String SETTING_PROJECT_COMPILE_CMD = "PROJECT_COMPILE_CMD";
	
	/** The setting name for the ant target name which tests the project. */
	public static final String SETTING_PROJECT_TEST_CMD = "PROJECT_TEST_CMD";
	
	/** The setting name for the number of individuals in a given generation. */
	public static final String SETTING_POPULATION_COUNT = "POPULATION_COUNT";
	
	/** The setting name for the number of generations before ARC decides that
	 * the given program cannot be fixed. */
	public static final String SETTING_MAX_GENERATIONS = "MAX_GENERATIONS";
	
	/** The setting name for the number of runs to use on each individual
	 * during the GA phase. */
	public static final String SETTING_RUN_COUNT = "RUN_COUNT";
	
	/** The setting name for the path with the project source files (not test
	 * files). */
	public static final String SETTING_PROJECT_SOURCE_DIR = "PROJECT_SRC_DIR";
	
	/** The setting name for the path to the TXL files used by C-FLASH. */
	public static final String SETTING_CFLASH_TXL_DIR = "CFLASH_TXL_DIR";
	
	/** The setting name for the path to the directory which will contain the
	 * fixed program, if one was found. */
	public static final String SETTING_OUTPUT_DIR = "OUTPUT_DIR";
	
	/** The default directory in which a solution will be placed if one is
	 * found by ARC. */
	public static final String DEFAULT_OUTPUT_DIR = "${" + SETTING_ROOT +
			"}${" + SETTING_DIR_SEPARATOR + "}output";
	
	/** The project for ARC to run. */
	private Project project;
	
	/** The root directory of ARC, which should contain the
	 * {@link #ARC_CONFIG_FILE} in the same directory. */
	private String root;
	
	/**
	 * Constructs ARC with the root as the current working directory.
	 * @since 1.0
	 */
	public ARC() {
		this(null);
		loadDefaultSettings();
	}
	
	/**
	 * Constructs ARC with the specified directory. If the directory is not
	 * valid, it will be converted into the current working directory.
	 * 
	 * @param root	the path to the root directory ARC should use.
	 * @since 1.0
	 */
	public ARC(String root) {
		super();
		loadDefaultSettings();
		setRoot(root);
	}
	
	/**
	 * Constructs ARC with the specified directory and project. If the
	 * directory is not valid, it will be converted into the current working
	 * directory.
	 * 
	 * @param root		the path to the root directory ARC should use.
	 * @param project	the project ARC should use.
	 * @since 1.0
	 */
	public ARC(String root, Project project) {
		super();
		loadDefaultSettings();
		setRoot(root);
		setProject(project);
	}
	
	/**
	 * Loads the settings from the ARC config file ({@value #ARC_CONFIG_FILE}).
	 * Note: any static settings (as defined by
	 * {@link #isStaticSetting(String)}) cannot be overwritten.
	 * 
	 * @return the number of settings parsed from the file or -1 if the file
	 * could not be parsed.
	 * @since 1.0
	 */
	public int loadSettings() {
		
		// Root directory doesn't exist
		if (root == null || !(new File(root)).isDirectory()) {
			return -1;
		}
		
		// Check that the config file exists
		char dirSep = FileUtils.getDirectorySeparator(root);
		String path = root + dirSep + ARC_CONFIG_FILE;
		if (!(new File(path)).isFile()) {
			return -1;
		}
		
		// Parse the settings file
		clearSettings();
		loadDefaultSettings();
		int parsed = parseFromFile(path);
		loadStaticSettings();
		
		return parsed;
	}
	
	/**
	 * Loads the default settings for ARC.
	 * 
	 * @see #loadStaticSettings()
	 * @see #loadSettings()
	 * @since 1.0
	 */
	public void loadDefaultSettings() {
		
		// Tools
		setSetting(SETTING_JAVA, "java");
		setSetting(SETTING_JAVAC, "javac");
		setSetting(SETTING_ANT, "/usr/local/bin/ant");
		setSetting(SETTING_TXL, "/usr/local/bin/txl");
		
		// Directories
		String slash = "${" + SETTING_DIR_SEPARATOR + "}";
		setSetting(SETTING_TXL_DIR, "${" + SETTING_LIB_DIR + "}" + slash + "TXL");
		setSetting("SRC_DIR", "${ROOT}/src");
		setSetting("WORKAREA_DIR", "${ROOT}/workarea");
		setSetting("PROJECT_DIR", "${ROOT}/input");
		setSetting("CONTEST_DIR", "${LIB_DIR}/ConTest");
		setSetting("CONTEST_JAR", "${CONTEST_DIR}/ConTest.jar");
		setSetting("CONTEST_CFPARSE_JAR", "${CONTEST_DIR}/cfparse.jar");
		setSetting("CONTEST_KINGPROPERTIES_FILE", "${CONTEST_DIR}/KingProperties");
		setSetting("CHORD_DIR", "${LIB_DIR}/Chord");
		setSetting("CHORD_JAR", "${CHORD_DIR}/chord.jar");
		setSetting("CHORD_PROPERTIES_FILE", "${CHORD_DIR}/chord.properties");
		setSetting("PROJECT_SRC_DIR", "${ARC_DIR}/source");
		setSetting("PROJECT_CLASS_DIR", "${ARC_DIR}/class");
		
		// General defaults
		setSetting(Project.PROJECT_TEST_MB, "" + DEFAULT_PROGRAM_MB);
		setSetting("CONTEST_TIMEOUT_MULTIPLIER", "15");
		setSetting(SETTING_TIMEOUT_MILLIS, "300000");
		setSetting(SETTING_TIMEOUT_MULTIPLIER, "15");
		setSetting(SETTING_OUTPUT_DIR, DEFAULT_OUTPUT_DIR);
		
		// Genetic Algorithm defaults
		setSetting(SETTING_POPULATION_COUNT,
				"" + ARCGeneticAlgorithm.DEFAULT_POPULATION_COUNT);
		setSetting(SETTING_MAX_GENERATIONS,
				"" + ARCGeneticAlgorithm.DEFAULT_MAX_GENERATIONS);
		setSetting(SETTING_RUN_COUNT,
				"" + ARCGeneticAlgorithm.DEFAULT_RUN_COUNT);
		
		loadStaticSettings();
	}
	
	/**
	 * Loads the static settings which cannot be changed by the user or
	 * program.
	 * 
	 * @see #isStaticSetting(String)
	 * @see #loadDefaultSettings()
	 * @see #loadSettings()
	 * @since 1.0
	 */
	public void loadStaticSettings() {
		String slash = "${" + SETTING_DIR_SEPARATOR + "}";
		String ad = "${" + SETTING_ARC_DIR + "}";
		super.setSetting(SETTING_ROOT, root);
		super.setSetting(SETTING_ARC_DIR, "${" + SETTING_ROOT + "}" + slash + "ARC");
		super.setSetting(SETTING_LIB_DIR, "${" + SETTING_ROOT + "}" + slash + "lib");
		super.setSetting(SETTING_DIR_SEPARATOR,
				System.lineSeparator().length() == 1? "/" : "\\");
		super.setSetting(SETTING_PROJECT_DIR, ad + slash + "project");
		super.setSetting(SETTING_TMP_DIR, ad + slash + "tmp");
		super.setSetting(SETTING_MUTANT_DIR, ad + slash + "mutants");
	}
	
	/**
	 * Gets the path to the ARC directory, which is contained in the root
	 * directory.
	 * 
	 * @return the ARC directory path.
	 * @since 1.0
	 */
	public String getARCDirectory() {
		return getSetting(SETTING_ARC_DIR);
	}
	
	@Override
	public String getSetting(String setting) {
		if (project == null || isStaticSetting(setting)) {
			return super.getSetting(setting);
		}
		String ps = project.getSetting(setting);
		return ps == null? super.getSetting(setting) : ps;
	}
	
	@Override
	public String setSetting(String setting, String value) {
		if (setting == null || setting.isEmpty() || isStaticSetting(setting)) {
			return null;
		}
		return super.setSetting(setting, value);
	}

	/**
	 * Gets the project which ARC will attempt to fix.
	 * 
	 * @return the project.
	 * @since 1.0
	 */
	public Project getProject() {
		return project;
	}
	
	/**
	 * Sets the project ARC will attempt to fix.
	 * 
	 * @param project	the project.
	 * 
	 * @since 1.0
	 */
	public void setProject(Project project) {
		this.project = project;
		if (project != null) {
			project.setSetting(SETTING_ROOT, root);
			project.setSetting(SETTING_PROJECT_DIR,
					getSetting(SETTING_PROJECT_DIR));
		}
	}
	
	/**
	 * Gets the root directory path for ARC.
	 * 
	 * @return the root path.
	 * @since 1.0
	 */
	public String getRoot() {
		return root;
	}
	
	/**
	 * Sets the root directory for ARC to use.
	 * 
	 * @param root	the root directory path.
	 * 
	 * @since 1.0
	 */
	public void setRoot(String root) {
		
		// Correct the directory
		root = FileUtils.asValidPath(root);
		
		// Update value
		this.root = root;
		super.setSetting(SETTING_ROOT, root);
		if (project != null) {
			this.project.setSetting(SETTING_ROOT, root);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[root='" + root +
				"', project='" + project + "', " + super.toString() + "]";
	}
	
	/**
	 * Checks if a setting name is reserved as a static setting. Static
	 * settings cannot be changed.
	 * 
	 * @param setting	the name of the setting
	 * @return true if and only if the name of the setting is reserved.
	 * 
	 * @see Project#isStaticSetting(String)
	 * @see #SETTING_DIR_SEPARATOR
	 * @see #SETTING_ARC_DIR
	 * @see #SETTING_ROOT
	 * @see #SETTING_LIB_DIR
	 * @see #SETTING_PROJECT_DIR
	 * @see #SETTING_TMP_DIR
	 * @see #SETTING_MUTANT_DIR
	 * @since 1.0
	 */
	public static boolean isStaticSetting(String setting) {
		
		// Not a valid setting
		if (setting == null || setting.isEmpty()) {
			return false;
		}
		
		// Check if it is a static setting
		final String[] ss = {SETTING_DIR_SEPARATOR, SETTING_ARC_DIR,
				SETTING_ROOT, SETTING_LIB_DIR, SETTING_PROJECT_DIR,
				SETTING_TMP_DIR, SETTING_MUTANT_DIR};
		for (String s : ss) {
			if (setting.equals(s)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if the specified path is setup to run ARC. The path is
	 * initialized if and only if it is a directory and contains the
	 * {@link #ARC_CONFIG_FILE}.
	 * 
	 * @param path	the path to a directory to check.
	 * @return true if and only if the path specified is initialized for ARC.
	 * @see #initialize(String)
	 * @since 1.0
	 */
	public static boolean isInitialized(String path) {
		return path == null || path.isEmpty()? false : isInitialized(new File(path));
	}
	
	/**
	 * Checks if the specified directory is setup to run ARC. The directory is
	 * initialized if and only if it contains the {@link #ARC_CONFIG_FILE}.
	 * 
	 * @param dir	the directory to check.
	 * @return true if and only if the directory is initialized for ARC.
	 * @see #initialize(File)
	 * @since 1.0
	 */
	public static boolean isInitialized(File dir) {
		
		// Not a directory
		if (dir == null || !dir.isDirectory()) {
			return false;
		}
		
		// Check for the configuration file
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return false;
		}
		for (File f : files) {
			if (f.isFile() && ARC_CONFIG_FILE.equals(f.getName())) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Attempts to initialize a directory path to setup ARC.
	 * 
	 * @param path	the path to the directory to initialize.
	 * @return true if and only if the specified path is initialized.
	 * @see #isInitialized(String)
	 * @since 1.0
	 */
	public static boolean initialize(String path) {
		return path == null || path.isEmpty()? false : initialize(new File(path));
	}
	
	/**
	 * Attempts to initialize a directory to setup ARC.
	 * 
	 * @param dir	the directory to initialize.
	 * @return true if and only if the specified directory is initialized.
	 * @see #isInitialized(File)
	 * @since 1.0
	 */
	public static boolean initialize(File dir) {
		
		// Not a directory
		if (dir == null || !dir.isDirectory()) {
			return false;
		}
		
		// Create a settings file with the default settings
		String path = dir.getAbsolutePath();
		ARC arc = new ARC(path);
		arc.loadDefaultSettings();
		char ds = FileUtils.getDirectorySeparator(path);
		String config = path + ds + ARC_CONFIG_FILE;
		arc.writeToFile(config);
		
		return isInitialized(dir);
	}
}
