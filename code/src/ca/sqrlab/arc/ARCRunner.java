package ca.sqrlab.arc;

import java.io.File;
import java.util.List;

import ca.sqrlab.arc.evolution.ARCGeneticAlgorithm;
import ca.sqrlab.arc.evolution.Individual;
import ca.sqrlab.arc.io.FileUtils;
import ca.sqrlab.arc.java.JavaFile;
import ca.sqrlab.arc.tools.ARCUtils;
import ca.sqrlab.arc.tools.AntBuildFile;
import ca.sqrlab.arc.tools.SettingsManager;
import ca.sqrlab.arc.tools.instrumentation.CFlashInstrumentor;
import ca.sqrlab.arc.tools.monitoring.Logger;
import ca.sqrlab.arc.tools.monitoring.Phase;
import ca.sqrlab.arc.tools.mutation.TXLMutation;
import ca.sqrlab.arc.tools.testing.TestResult;
import ca.sqrlab.arc.tools.testing.TestRunner;
import ca.sqrlab.arc.tools.testing.TestStatus;
import ca.sqrlab.arc.tools.testing.TestingSummary;

/**
 * The {@code ARCRunner} class is responsible for executing ARC and ensuring
 * all programs/files exist and all required values exist. This is the main
 * workhorse for ARC.
 * 
 * <p>This class starts by checking all the settings in ARC and the project it
 * is fixing. Once complete, the genetic algorithm begins. Since it is likely
 * this class is run from the UI, it can (and should) be run asynchronously via
 * the {@link #startARC()} method.
 * 
 * @author Devon McGrath
 * @see ARC
 * @see Project
 * @see ARCGeneticAlgorithm
 * @since 1.0
 */
public class ARCRunner extends Thread {
	
	/** The regular expression for the name of a Java file. */
	public static final String JAVA_FILE_REGEX = "(_[_\\$a-zA-Z]+|"
			+ "[\\$a-zA-Z]+)[_\\$a-zA-Z0-9]*\\.java";
	
	/** The ID passed to the {@link #getOnFinish()} listener when the ARC
	 * runner has completed its execution. */
	public static final int ARC_FINISHED_ID = 1000;
	
	/** The ID passed to the {@link #getOnFinish()} listener when the ARC
	 * runner finishes a phase. */
	public static final int ARC_PHASE_FINISHED_ID = 1001;
	
	/** A flag which marks if the run method should terminate early. */
	private boolean shouldStop;
	
	/** A flag indicating if this ARC runner is finished executing. */
	private boolean isFinished;
	
	/** A flag indicating if ARC found a valid solution. */
	private boolean foundFix;
	
	/** The execution time, which is only equal to the amount of milliseconds
	 * ARC took to finish if ARC has stopped. Otherwise, this value represents
	 * the millisecond that the ARC runner started
	 * (using {@link System#currentTimeMillis()}). */
	private long executionTime;
	
	/** The execution time, in milliseconds, ARC executed the genetic
	 * algorithm for. */
	private long gaExecutionTime;
	
	/** The instance of ARC with all the settings. */
	private ARC arc;
	
	/** The reference to the genetic algorithm. */
	private ARCGeneticAlgorithm ga;
	
	/** The logger for ARC to keep track of all events. */
	private Logger l;
	
	/** The directory path to the root of ARC. */
	private String arcPath;
	
	/** The directory path to the root of the project for ARC to fix. */
	private String projectPath;

	/** The listener which will receive updates every time a phase is completed
	 * in ARC. */
	private FinishListener onFinish;
	
	/** Additional settings for ARC during its execution. */
	private SettingsManager extraSettings;
	
	/**
	 * Creates an ARC runner with the default ARC and project directories.
	 * 
	 * @since 1.0
	 */
	public ARCRunner() {
		this(null, null);
	}
	
	/**
	 * Creates an ARC runner instance with the specified ARC and project
	 * directory paths.
	 * 
	 * @param arcPath		the ARC root path.
	 * @param projectPath	the project root path.
	 * @since 1.0
	 */
	public ARCRunner(String arcPath, String projectPath) {
		try {
			super.setName(getClass().getSimpleName() + " Thread");
		} catch (Exception e) {}
		setArcPath(arcPath);
		setProjectPath(projectPath);
		this.isFinished = true;
		this.l = new Logger();
	}
	
	/**
	 * Starts running ARC asynchronously. Note: this method should be used
	 * instead of {@link #start()}.
	 * 
	 * @see #startARCSync()
	 * @see #stopARC()
	 * @since 1.0
	 */
	public void startARC() {
		this.isFinished = false;
		this.shouldStop = false;
		this.foundFix = false;
		this.executionTime = System.currentTimeMillis();
		this.gaExecutionTime = 0;
		this.ga = null;
		this.l = new Logger();
		start();
	}
	
	/**
	 * Starts running ARC synchronously. This should only be used when ARC
	 * does not require any user interface. Signaling ARC to stop via
	 * {@link #stopARC()} works, however, it must be done from an alternate
	 * thread than the caller of this method.
	 * 
	 * @see #startARC()
	 * @see #stopARC()
	 * @since 1.0
	 */
	public void startARCSync() {
		this.isFinished = false;
		this.shouldStop = false;
		this.foundFix = false;
		this.executionTime = System.currentTimeMillis();
		this.gaExecutionTime = 0;
		this.ga = null;
		this.l = new Logger();
		run();
	}
	
	@Override
	public void run() {

		this.l.newPhase("ARC Initialization");
		
		// Check if ARC is initialized in the directory
		l.debug("Checking if ARC directory '" + arcPath + "' is initialized...");
		if (!ARC.isInitialized(arcPath)) {
			l.fatalError("ARC directory is not initialized.");
			stopExecuting();
			return;
		}
		l.debug("ARC directory is initialized.");
		
		// Check if the project directory is initialized
		l.debug("Checking if project directory '" + projectPath + "' is initialized...");
		if (!Project.isInitialized(projectPath)) {
			l.fatalError("Project directory is not initialized.");
			stopExecuting();
			return;
		}
		l.debug("Project directory is initialized. Checking for build file...");
		Project p = new Project(projectPath);
		if (!p.isValidProject()) {
			l.fatalError("Missing build file in project directory.");
			stopExecuting();
			return;
		}
		p.loadSettings();
		
		// Create ARC
		this.arc = new ARC(arcPath, p);
		this.arc.loadSettings();
		this.arc.updateSettings(extraSettings);
		AntBuildFile bf = p.getBuildFile();
		
		// Check if the TXL operator directory is defined
		String libDir = arc.getSetting(ARC.SETTING_LIB_DIR);
		String txl = arc.getSetting(ARC.SETTING_TXL_DIR);
		if (txl == null || txl.isEmpty() || !(new File(txl)).isDirectory()) {
			
			// Unable to find TXL directory
			List<File> txlFiles = FileUtils.find(libDir, ".+\\.txl", true);
			if (txlFiles.isEmpty()) {
				l.fatalError("Invalid directory path for '" + ARC.SETTING_TXL_DIR
						+ "' and unable to find suitable TXL directory.");
				stopExecuting();
				return;
			}
			
			// Found a TXL directory
			else {
				txl = txlFiles.get(0).getParent();
				this.arc.setSetting(ARC.SETTING_TXL_DIR, txl);
				l.warning("The directory path value for '" + ARC.SETTING_TXL_DIR
						+ "' was not specified. Using '" + txl +
						"' as the TXL directory.");
			}
		}


		// Check if program was requested to terminate
		if (shouldStop) {
			l.fatalError("ARC was requested to stop.");
			stopExecuting();
			return;
		}
		
		startNewPhase("File Checking");
		
		// Check if JUnit is defined
		String junit = arc.getSetting(ARC.SETTING_JUNIT_JAR);
		if (junit == null || junit.isEmpty() || !(new File(junit)).isFile()) {
			
			// Attempt to find JUnit in the lib directory
			List<File> files = FileUtils.find(libDir, "junit.*\\.jar", true);
			for (File f : files) {
				if (f.isFile()) {
					l.warning("Dynamically found the JUnit jar, since the "
							+ "config setting '" + ARC.SETTING_JUNIT_JAR +
							"' either did not exist or was not valid.");
					l.warning("Using '" + f.getAbsolutePath() + "' as JUnit.");
					arc.setSetting(ARC.SETTING_JUNIT_JAR, f.getAbsolutePath());
					break;
				}
			}
		}
		
		// Check if Hamcrest is defined (JUnit requires this)
		junit = arc.getSetting(ARC.SETTING_HAMCREST_JAR);
		if (junit == null || junit.isEmpty() || !(new File(junit)).isFile()) {
			
			// Attempt to find JUnit in the lib directory
			List<File> files = FileUtils.find(libDir, "hamcrest.*\\.jar", true);
			for (File f : files) {
				if (f.isFile()) {
					l.warning("Dynamically found the Hamcrest jar, since the "
							+ "config setting '" + ARC.SETTING_HAMCREST_JAR +
							"' either did not exist or was not valid.");
					l.warning("Using '" + f.getAbsolutePath() + "' as JUnit.");
					arc.setSetting(ARC.SETTING_HAMCREST_JAR, f.getAbsolutePath());
					break;
				}
			}
		}
		
		// Check that all the TXL mutation operator files exist
		TXLMutation[] mutations = TXLMutation.getAllMutations();
		for (TXLMutation m : mutations) {
			if (!m.exists(txl)) {
				l.fatalError("Missing TXL mutation operator '" +
						m.getMutationFile() + "'");
			}
		}
		if (l.hasFatalError()) {
			stopExecuting();
			return;
		}
		
		// Check for required files
		checkFiles();
		if (l.hasFatalError()) {
			stopExecuting();
			return;
		}
		
		// Get the source files
		if (!getProjectSourceFiles()) {
			stopExecuting();
			return;
		}
		
		// Check the programs required (i.e. TXL, JAVA, JAVAC, ANT)
		startNewPhase("Program Checking");
		checkPrograms();
		if (l.hasFatalError()) {
			stopExecuting();
			return;
		}
		
		// Check for compile command
		String cc = arc.getSetting(ARC.SETTING_PROJECT_COMPILE_CMD);
		if (cc == null || cc.isEmpty()) {
			l.warning("No compile target specified (" +
					ARC.SETTING_PROJECT_COMPILE_CMD +
					"). Will acquire dynamically.");
			
			// Try to find a compile target in the build file
			AntBuildFile.AntTarget comp = bf.getCompileTarget();
			if (comp == null) {
				l.fatalError("Could not find a compile/build target.");
				stopExecuting();
				return;
			}
			l.warning("Will use '" + comp.getName() + "' as the compile target.");
			this.arc.setSetting(ARC.SETTING_PROJECT_COMPILE_CMD, comp.getName());
		}
		
		// Check if program was requested to terminate
		if (shouldStop) {
			l.fatalError("ARC was requested to stop.");
			stopExecuting();
			return;
		}
		
		// Initialize directories
		startNewPhase("Directory Initialization");
		if (!initializeDirectories()) {
			stopExecuting();
			return;
		}
		
		// Check if program was requested to terminate
		if (shouldStop) {
			l.fatalError("ARC was requested to stop.");
			stopExecuting();
			return;
		}
		
		startNewPhase("Dynamic Variable Allocation");
		
		// Check if the classpath is missing
		String cp = arc.getSetting(ARC.SETTING_PROJECT_CLASSPATH);
		if (cp == null || cp.isEmpty()) {
			
			// Get the test command/target
			String tc = arc.getSetting(ARC.SETTING_PROJECT_TEST_CMD);
			AntBuildFile.AntTarget test = null;
			if (tc == null || tc.isEmpty()) { // no test command either
				test = bf.getTestTarget();
				if (test != null) {
					l.warning("No test target specified with '" +
							ARC.SETTING_PROJECT_TEST_CMD + "'. Will use '" +
							test.getName() + "' as the test target.");
					p.setSetting(ARC.SETTING_PROJECT_TEST_CMD, test.getName());
				}
			} else { // test command found, find the actual target object
				List<AntBuildFile.AntTarget> targets = bf.getTargets();
				if (targets != null) {
					for (AntBuildFile.AntTarget t : targets) {
						if (tc.equals(t.getName())) {
							test = t;
							break;
						}
					}
				}
				
				// Not found
				if (test == null) {
					l.fatalError("The test target '" + tc + "' is not valid. "
							+ "There is no target with that name. Please "
							+ "ensure the setting '" + ARC.SETTING_PROJECT_TEST_CMD
							+ "' is valid in the project config.");
					stopExecuting();
					return;
				}
			}
			
			// No test target
			if (test == null) {
				l.fatalError("No way of getting the classpath for testing. "
						+ "Either include '" + ARC.SETTING_PROJECT_CLASSPATH +
						"' or '" + ARC.SETTING_PROJECT_TEST_CMD +
						"' to ensure the classpath can be retrieved.");
				stopExecuting();
				return;
			}
			
			// Get the classpath
			cp = bf.getClassPath(test.getName());
			l.warning("Dynamically acquired classpath for testing: '" + cp +
					"'. To avoid this, ensure '" + ARC.SETTING_PROJECT_CLASSPATH
					+ "' is in the project config.");
			p.setSetting(ARC.SETTING_PROJECT_CLASSPATH, cp);
		}
		
		// Check if the test suite is missing
		String ts = arc.getSetting(ARC.SETTING_PROJECT_TESTSUITE);
		if (ts == null || ts.isEmpty()) {
			
			l.warning("Missing config setting '" +
					ARC.SETTING_PROJECT_TESTSUITE +
					"'. Will attempt to acquire value dynamically.");
			
			// Check for a test directory
			List<File> res = FileUtils.find(
					arc.getSetting(ARC.SETTING_PROJECT_DIR), "test[s]?", true);
			File val = null;
			for (File f : res) {
				if (f.isDirectory()) {
					val = f;
					break;
				}
			}
			if (val == null) {
				l.fatalError("Could not find suitable value for '" +
						ARC.SETTING_PROJECT_TESTSUITE + "'.");
				stopExecuting();
				return;
			}
			
			// Check for a Java file
			res = FileUtils.find(val.getAbsolutePath(),
					JAVA_FILE_REGEX, true);
			val = null;
			for (File f : res) {
				if (f.isFile()) {
					val = f;
					break;
				}
			}
			if (val == null) {
				l.fatalError("Could not find suitable value for '" +
						ARC.SETTING_PROJECT_TESTSUITE + "'.");
				stopExecuting();
				return;
			}
			
			// Get the correct path
			JavaFile jf = JavaFile.fromFile(val);
			ts = jf.getPackageName();
			if (!ts.isEmpty()) {
				ts += ".";
			}
			ts += jf.getClassName();
			l.warning("Using '" + ARC.SETTING_PROJECT_TESTSUITE +
					"' value of '" + ts + "'");
			this.arc.setSetting(ARC.SETTING_PROJECT_TESTSUITE, ts);
		}
		
		// Get the C-FLASH TXL file directory
		if (!getCFlashTxlDir()) {
			stopExecuting();
			return;
		}
		
		// Get the timeout for the program
		if (!getTimeout()) {
			stopExecuting();
			return;
		}
		
		// Start the genetic algorithm
		if (onFinish != null) {
			List<Phase> phases = l.getPhases();
			this.onFinish.onFinish(ARC_PHASE_FINISHED_ID,
					phases.get(phases.size() - 1));
		}
		this.ga = new ARCGeneticAlgorithm(this, onFinish);
		this.gaExecutionTime = System.currentTimeMillis();
		this.ga.run(l);
		this.gaExecutionTime = System.currentTimeMillis() - gaExecutionTime;
		this.foundFix = ga.foundFix();
		
		// ARC found a solution, copy it
		if (foundFix) {
			setOutput(ga);
		}
		
		// Stop executing, ARC is done
		stopExecuting();
	}
	
	/**
	 * Sets flags to mark the end of execution for ARC and gets the total
	 * execution time from calling {@link #startARC()} until now. In addition,
	 * the {@link FinishListener#onFinish(int, Object)} method is called if it
	 * is defined.
	 * 
	 * @since 1.0
	 */
	private void stopExecuting() {
		this.executionTime = System.currentTimeMillis() - executionTime;
		this.isFinished = true;
		this.shouldStop = true;
		if (onFinish != null) {
			this.onFinish.onFinish(ARC_FINISHED_ID, this);
		}
	}
	
	/**
	 * Starts a new phase of logging and signals the on finish listener that a
	 * new phase has begun.
	 * 
	 * @param name	the name of the new phase.
	 * @see FinishListener
	 * @since 1.0
	 */
	private void startNewPhase(String name) {
		if (onFinish != null) {
			List<Phase> phases = l.getPhases();
			this.onFinish.onFinish(ARC_PHASE_FINISHED_ID,
					phases.get(phases.size() - 1));
		}
		this.l.newPhase(name);
	}
	
	/**
	 * Checks that all required programs to run ARC are installed. If any
	 * program is not installed, a fatal error is logged.
	 * 
	 * @since 1.0
	 */
	private void checkPrograms() {
		
		// Check programs
		String[] progNames = {ARC.SETTING_JAVA, ARC.SETTING_JAVAC,
				ARC.SETTING_TXL, ARC.SETTING_ANT};
		for (String p : progNames) {
			String pn = arc.getSetting(p);
			if (pn == null || pn.isEmpty()) {
				l.fatalError("Failed to find value for " + p.toLowerCase() + ". " +
						"Ensure '" + p + "' is set in the config.");
			} else {
				try {
					Runtime.getRuntime().exec(pn);
					l.debug("Found " + p.toLowerCase() + " (value=" + pn + ").");
				} catch (Exception e) {
					l.fatalError("Failed to find program: " + p.toLowerCase() +
							" (value=" + pn + ")!");
				}
			}
		}
	}
	
	/**
	 * Checks that all the required files exist where they are said to be
	 * according to the relevant setting. If one or more files is missing, a
	 * fatal error will be logged.
	 * 
	 * @since 1.0
	 */
	private void checkFiles() {
		
		// Check files
		String[] files = {ARC.SETTING_JUNIT_JAR, ARC.SETTING_HAMCREST_JAR};
		for (String f : files) {
			String s = arc.getSetting(f);
			if (s == null || s.isEmpty()) { // no entry
				l.fatalError("Missing value: unable to find entry for '" + f +
						"' in the config.");
			} else if ((new File(s)).isFile()) { // valid file
				l.debug("Found valid file entry for '" + f +
						"', will use '" + s + "'");
			} else { // not a file
				l.fatalError("The entry '" + f + "' which points to '" +
						s + "' is not a valid file.");
			}
		}
	}
	
	/**
	 * Dynamically finds all the Java source files for a given project.
	 * Note: if {@link ARC#SETTING_PROJECT_SOURCE_DIR} is not set, this method
	 * will attempt to dynamically find all non-JUnit Java source files.
	 * 
	 * @return true if and only if at least one source file was found.
	 * @since 1.0
	 */
	private boolean getProjectSourceFiles() {
		
		// Check for the source directory
		Project p = arc.getProject();
		String ds = arc.getSetting(ARC.SETTING_DIR_SEPARATOR);
		String prDir = p.getDirectory();
		String srcDir = arc.getSetting(ARC.SETTING_PROJECT_SOURCE_DIR);
		File pr = new File(prDir);
		boolean exists = true;
		if (srcDir == null || srcDir.isEmpty()) {
			exists = false;
		} else {
			File f = new File(srcDir);
			if (!f.isDirectory()) {
				f = new File(prDir + ds + srcDir);
				if (!f.isDirectory()) {
					exists = false;
				} else {
					srcDir = f.getAbsolutePath();
				}
			} else {
				srcDir = f.getAbsolutePath();
			}
		}
		
		// Determine source files dynamically
		List<File> files = null;
		final String JAVA_FILE_REGEX = ".+\\.java";
		if (!exists) {
			l.warning("Invalid or missing project config setting '" +
					ARC.SETTING_PROJECT_SOURCE_DIR + "'. Will attempt to "
							+ "find non-test Java files dynamically.");
			files = FileUtils.find(prDir, JAVA_FILE_REGEX, true);
			
			// Remove any files in a test directory
			String[] invalidDirs = {"test", "tests"};
			int n = files.size();
			for (int i = 0; i < n; i ++) {
				File f = files.get(i);
				while (f != null && !f.equals(pr)) {
					f = f.getParentFile();
					String name = f.getName();
					for (String invalidDir : invalidDirs) {
						if (invalidDir.equalsIgnoreCase(name)) {
							f = null;
							files.remove(i--);
							n--;
							break;
						}
					}
				}
			}
		} else {
			files = FileUtils.find(srcDir, JAVA_FILE_REGEX, true);
		}
		
		// Check if there were no files
		if (files == null || files.isEmpty()) {
			l.fatalError("No Java files found for the project.");
			return false;
		}
		
		// Get the relative directories
		int n = files.size();
		String[] sourceFiles = new String[n];
		for (int i = 0; i < n; i ++) {
			File f = files.get(i);
			String relPath = f.getName();
			f = f.getParentFile();
			while (f != null && !f.equals(pr)) {
				relPath = f.getName() + ds + relPath;
				f = f.getParentFile();
			}
			sourceFiles[i] = relPath;
			l.debug("Project Java File Found: \"" + relPath + "\"");
		}
		this.arc.getProject().setSourceFiles(sourceFiles);
		
		return true;
	}
	
	/**
	 * Initializes all directories required for ARC to run.
	 * 
	 * @return true if and only if all directories were created successfully.
	 * @since 1.0
	 */
	private boolean initializeDirectories() {
		
		// Check if there is an old ARC run, if so, delete it
		File ad = new File(arc.getARCDirectory());
		l.debug("ARC Directory: " + ad.getAbsolutePath());
		if (ad.exists()) {
			l.debug("Removing old ARC directory.");
			FileUtils.remove(ad.getAbsolutePath());
			if (ad.exists()) {
				l.fatalError("Could not remove old ARC directory.");
				stopExecuting();
				return false;
			}
		}
		
		// Make directories
		l.debug("Creating ARC directories...");
		String[] settings = {ARC.SETTING_ARC_DIR, ARC.SETTING_PROJECT_DIR,
				ARC.SETTING_TMP_DIR, ARC.SETTING_MUTANT_DIR};
		String[] dirNames = {"ARC", "project", "temporary", "mutation"};
		int n = settings.length;
		for (int i = 0; i < n; i ++) {
			File dir = new File(arc.getSetting(settings[i]));
			String end = dirNames[i] + " directory '"
					+ dir.getAbsolutePath() + "'.";
			if (dir.isDirectory() || dir.mkdirs()) {
				l.debug("Created " + end);
			} else {
				l.fatalError("Could not create " + end);
				return false;
			}
		}
		
		// Copy the project over
		if (!ARCUtils.copyOriginalProject(arc, l)) {
			return false;
		}
		
		// Compile the project
		if (onFinish != null) {
			List<Phase> phases = l.getPhases();
			this.onFinish.onFinish(ARC_PHASE_FINISHED_ID,
					phases.get(phases.size() - 1));
		}
		if (!ARCUtils.compile(arc, l)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Determines a timeout limit in milliseconds for the execution of the
	 * test program. It multiplies the timeout multiplier by the average
	 * execution time to get the timeout.
	 * 
	 * @return true if and only if the timeout was able to be set.
	 * @see ARC#SETTING_TIMEOUT_MULTIPLIER
	 * @see ARC#SETTING_TIMEOUT_MILLIS
	 * @since 1.0
	 */
	private boolean getTimeout() {
		
		// Run some tests to determine a good timeout dynamically
		TestRunner trunner = new TestRunner(arc);
		l.debug("Acquiring timeout limit dynamically...");
		TestingSummary summary = trunner.execute(15, true);
		TestResult[] trs = summary.getResults();
		if (trs == null || trs.length == 0) {
			l.fatalError("Could not dynamically attain timeout.");
			return false;
		}
		
		// Determine the result of the tests
		float mult = 10;
		try {
			mult = Float.parseFloat(arc.getSetting(
					ARC.SETTING_TIMEOUT_MULTIPLIER));
		} catch (Exception e) {
			l.warning("Could not parse as float: '" +
					ARC.SETTING_TIMEOUT_MULTIPLIER + "'.");
		}
		long average = 0;
		int n = 0, invalid = 0;
		String res = "";
		for (TestResult re : trs) {
			average += re.getExecutionTimeMillis();
			n++;
			res += "<b>Test Result " + n + ":</b> Passed: " + re.successes +
					" Failed: " + re.failures + " Tests: " + re.tests +
					" Status: " + re.getStatus() + "\n";
			List<String> errors = re.getErrors();
			int en = 1;
			if (errors != null) {
				for (String err : errors) {
					res += ">> Error " + en + ": " + err + "\n";
					en ++;
				}
			}
			if (re.getStatus() == TestStatus.INVALID) {
				invalid ++;
			}
		}
		l.debug(res);
		if (invalid == trs.length) {
			l.fatalError("All tests were invalid.");
			return false;
		}
		
		// Update the timeout
		average /= trs.length;
		l.debug("Average execution time for " + trs.length + " tests: " +
				average + "ms");
		average = (long) (average * mult);
		l.debug("Max program execution time: " + average + "ms");
		this.arc.setSetting(ARC.SETTING_TIMEOUT_MILLIS, "" + average);
		
		return true;
	}
	
	/**
	 * Gets the C-FLASH TXL directory path, if it does not exist already.
	 * 
	 * @return true if and only if the C-FLASH TXL directory is valid or a
	 * valid directory was found.
	 * @since 1.0
	 */
	private boolean getCFlashTxlDir() {
		
		// Check if the setting exists
		String dir = arc.getSetting(ARC.SETTING_CFLASH_TXL_DIR);
		String ds = arc.getSetting(ARC.SETTING_DIR_SEPARATOR);
		String[] files = {CFlashInstrumentor.ANNOTATE_FILE,
				CFlashInstrumentor.NOISE_FILE};
		boolean find = false;
		if (dir == null || dir.isEmpty()) {
			l.warning("No setting for '" + ARC.SETTING_CFLASH_TXL_DIR +
					"'. Will acquire dynamically...");
			find = true;
		}
		
		// Check if it is a directory
		else if (!new File(dir).isDirectory()) {
			l.warning("Invalid setting for '" + ARC.SETTING_CFLASH_TXL_DIR +
					"'. Will acquire dynamically...");
			find = true;
		}
		
		// Valid, but check for files
		else {
			String base = dir;
			if (!base.endsWith(ds)) {
				base += ds;
			}
			for (String f : files) {
				if (!(new File(base + f)).isFile()) {
					l.warning("C-FLASH missing file: " + f);
					find = true;
				}
			}
			
			// Missing a file
			if (find) {
				l.warning("Setting value for '" + ARC.SETTING_CFLASH_TXL_DIR +
					"' (" + dir + ") did not contain all the required files. "
							+ "Will acquire dynamically...");
			}
		}
		
		// Try to find C-Flash
		if (find) {
			
			// Search the LIB folder
			String lib = arc.getSetting(ARC.SETTING_LIB_DIR);
			List<File> cff = FileUtils.find(lib,
					CFlashInstrumentor.NOISE_FILE.replace(".", "\\."), true);
			if (cff == null || cff.isEmpty()) {
				l.fatalError("Unable to find C-FLASH TXL directory.");
				return false;
			}
			
			// Validate that the other files are there too
			File file = cff.get(0);
			dir = file.getParentFile().getAbsolutePath();
			String base = dir;
			boolean valid = true;
			if (!base.endsWith(ds)) {
				base += ds;
			}
			for (String f : files) {
				if (!(new File(base + f)).isFile()) {
					valid = false;
				}
			}
			
			
			if (!valid) { // Failed to find at least one file
				l.fatalError("Unable to find C-FLASH TXL directory.");
			} else { // success
				l.debug("C-FLASH TXL directory: '" + dir + "'.");
				this.arc.setSetting(ARC.SETTING_CFLASH_TXL_DIR, dir);
			}
			
			return valid;
		}
		
		return true;
	}
	
	/**
	 * Creates a copy of the solution which the genetic algorithm found. If no
	 * solution was found, then this method does nothing.
	 * 
	 * @param ga	the genetic algorithm, after it was executed.
	 * @since 1.0
	 */
	private void setOutput(ARCGeneticAlgorithm ga) {
		
		// Nothing to do
		if (ga == null || !ga.foundFix() || ga.getSolution() == null) {
			return;
		}
		
		startNewPhase("Output");
		String dir = arc.getSetting(ARC.SETTING_OUTPUT_DIR);
		if (dir == null || dir.isEmpty()) {
			dir = arc.formatWithSettings(ARC.DEFAULT_OUTPUT_DIR);
		}
		Individual solution = ga.getSolution();
		l.debug("Solution: " + solution);
		l.debug("Output Directory: '" + dir + "'.");
		
		// Check if the directory needs to be created
		File o = new File(dir);
		if (!o.isDirectory() && !o.mkdirs()) {
			l.fatalError("Unable to create output directory.");
			return;
		}
		
		// Copy the entire project first
		FileUtils.copy(arc.getSetting(ARC.SETTING_PROJECT_DIR), dir, true);
		
		// Now, copy the source files
		ARCUtils.copyProjectSourceFiles(arc, solution.getPath(), dir, null);
	}
	
	/**
	 * Sets a flag to indicate to ARC that it should stop. It does not
	 * guarantee it will stop immediately.
	 * 
	 * @see #startARC()
	 * @since 1.0
	 */
	public void stopARC() {
		this.shouldStop = true;
	}
	
	/**
	 * Gets the value of the flag which determines if ARC should stop
	 * executing. If true, there is no guarantee that ARC is finished. To check
	 * if ARC is or is not running, use {@link #isFinished()}.
	 * 
	 * @return true if ARC was requested to stop, false otherwise.
	 * @since 1.0
	 */
	public boolean shouldStop() {
		return shouldStop;
	}

	/**
	 * Gets the flag which indicates whether or not ARC is executing.
	 * 
	 * @return true if and only if ARC is not running.
	 * @since 1.0
	 */
	public boolean isFinished() {
		return isFinished;
	}
	
	/**
	 * Gets the flag which indicates if ARC found a valid solution which has
	 * no bugs.
	 * 
	 * @return true if and only if a valid solution was found.
	 * @since 1.0
	 */
	public boolean foundFix() {
		return foundFix;
	}

	/**
	 * Gets the execution time for ARC. If ARC is running, the execution time
	 * is the current amount of time ARC has been running for. Otherwise, it is
	 * the total time it took for ARC to complete.
	 * 
	 * @return the execution time, in milliseconds.
	 * @see #isFinished()
	 * @since 1.0
	 */
	public long getExecutionTime() {
		return isFinished? executionTime :
			System.currentTimeMillis() - executionTime;
	}
	
	/**
	 * Gets the current instance of ARC, with all of its settings.
	 * 
	 * @return arc.
	 * @since 1.0
	 */
	public ARC getArc() {
		return arc;
	}

	/**
	 * Gets the path to the ARC directory which it is being ran from.
	 * 
	 * @return the ARC directory path.
	 * @since 1.0
	 */
	public String getArcPath() {
		return arcPath;
	}

	/**
	 * Sets the directory path to ARC. If the path is null, empty, or does not
	 * exist, it is converted into the current working directory. Any path
	 * passed is converted into the absolute path.
	 * 
	 * @param arcPath	the path to ARC.
	 * @since 1.0
	 */
	public void setArcPath(String arcPath) {
		this.arcPath = FileUtils.asValidPath(arcPath);
	}

	/**
	 * Gets the path to the project directory. The project in this directory
	 * will be fixed (if possible) by ARC.
	 * 
	 * @return the project directory path.
	 * @since 1.0
	 */
	public String getProjectPath() {
		return projectPath;
	}

	/**
	 * Sets the directory path to the project. If the path is null, empty, or
	 * does not exist, it is converted into the current working directory. Any
	 * path passed is converted into the absolute path.
	 * 
	 * @param projectPath	the path to the project.
	 * @since 1.0
	 */
	public void setProjectPath(String projectPath) {
		this.projectPath = FileUtils.asValidPath(projectPath);
	}

	/**
	 * Gets the listener which is used by this class to denote the end of a
	 * phase in ARC or when ARC completely finishes.
	 * 
	 * @return the on finish listener.
	 * @since 1.0
	 */
	public FinishListener getOnFinish() {
		return onFinish;
	}

	/**
	 * Sets the listener which will receive events when ARC finishes a phase
	 * and when ARC completes.
	 * 
	 * @param onFinish	the on finish listener.
	 * @see #ARC_PHASE_FINISHED_ID
	 * @see #ARC_FINISHED_ID
	 * @since 1.0
	 */
	public void setOnFinish(FinishListener onFinish) {
		this.onFinish = onFinish;
	}
	
	/**
	 * Gets the logger used by this runner to log any status messages.
	 * 
	 * @return the ARC logger.
	 * @since 1.0
	 */
	public Logger getLogger() {
		return l;
	}

	/**
	 * Gets the execution time in milliseconds of the ARC genetic algorithm. If
	 * the genetic algorithm hasn't started executing yet, the value returned
	 * is 0. Should the GA still be executing, the value returned is the result
	 * from {@link System#currentTimeMillis()} just before it was started.
	 * 
	 * @return the GA execution time in milliseconds.
	 * @since 1.0
	 */
	public long getGAExecutionTime() {
		return gaExecutionTime;
	}

	/**
	 * Gets the reference to the genetic algorithm which ARC is executing or
	 * has executed during its last run.
	 * 
	 * @return the genetic algorithm.
	 * @since 1.0
	 */
	public ARCGeneticAlgorithm getGeneticAlgorithm() {
		return ga;
	}

	/**
	 * Gets the additional settings ARC will use during its execution.
	 * 
	 * @return the settings.
	 * @see #setExtraSettings(SettingsManager)
	 * @since 1.0
	 */
	public SettingsManager getExtraSettings() {
		return extraSettings;
	}

	/**
	 * Sets any additional settings for ARC to use during its execution. Note:
	 * this must be called before ARC is started.
	 * 
	 * @param extraSettings	additional settings.
	 * @return a reference to this ARC runner.
	 * 
	 * @see #getExtraSettings()
	 * @since 1.0
	 */
	public ARCRunner setExtraSettings(SettingsManager extraSettings) {
		this.extraSettings = extraSettings;
		return this;
	}
}
