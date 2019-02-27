package ca.sqrlab.arc.tools.testing;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.Project;
import ca.sqrlab.arc.io.FileUtils;
import ca.sqrlab.arc.io.ProcessResult;

public class TestRunner {
	
	private ARC arc;
	
	public TestRunner(ARC arc) {
		setARC(arc);
	}
	
	private TestResult execute(boolean isFunctional) {
		
		long t = System.currentTimeMillis();
		TestResult tr = new TestResult();
		if (arc == null || arc.getProject() == null) {
			tr.setStatus(TestStatus.INVALID);
			tr.addError("Error: ARC is not setup properly to execute.");
			return tr;
		}
		
		// Get some arguments
		String pd = arc.getSetting(ARC.SETTING_PROJECT_DIR);
		String java = arc.getSetting(ARC.SETTING_JAVA);
		String mems = arc.getSetting(Project.PROJECT_TEST_MB);
		String cp = arc.getSetting(ARC.SETTING_PROJECT_CLASSPATH);
		String ts = arc.getSetting(ARC.SETTING_PROJECT_TESTSUITE);
		if (ts == null || ts.isEmpty()) { // no JUnit test suite
			tr.setStatus(TestStatus.INVALID);
			tr.addError("Error: no project test suite, ensure '" +
					ARC.SETTING_PROJECT_TESTSUITE +
					"' is in the project config file.");
			return tr;
		}
		if (cp == null) {
			cp = pd;
		}
		
		// Add some extra dependencies to the classpath
		String[] toAdd = {ARC.SETTING_JUNIT_JAR, ARC.SETTING_HAMCREST_JAR};
		for (String setting : toAdd) {
			String sv = arc.getSetting(setting);
			if (sv != null && !sv.isEmpty()) {
				cp += File.pathSeparatorChar + sv;
			}
		}
		
		// Parse the memory size for the JVM
		int mem = ARC.DEFAULT_PROGRAM_MB;
		if (mems != null && !mems.isEmpty()) {
			try {
				mem = Integer.parseInt(mems);
				
				// Cannot run the JVM with no memory
				if (mem <= 0) {
					tr.setStatus(TestStatus.INVALID);
					tr.addError("Error: program memory size is less " +
							"than 1MB, unable to run. Check '" +
							Project.PROJECT_TEST_MB + "' in the project/ARC config.");
					return tr;
				}
			} catch (Exception e) {
				e.printStackTrace();
				tr.addWarning("Warning: unable to parse '" +
						Project.PROJECT_TEST_MB + "'.");
			}
		}
		mems = "-Xmx" + mem + "m";
		
		// Build the command
		String cmd = java + " " + mems + " -cp " + cp +
				" org.junit.runner.JUnitCore " + ts;
		tr.setCommand(cmd);
		
		// Start the process
		ProcessBuilder pb = new ProcessBuilder(java, mems, "-cp", cp,
				"org.junit.runner.JUnitCore", ts);
		pb.directory(new File(arc.getSetting(ARC.SETTING_PROJECT_DIR)));
		try {
			runProcess(pb, tr, isFunctional);
		} catch (IOException e) {
			tr.setStatus(TestStatus.INVALID);
			tr.addError("Error: execution of the process failed. " +
					e.getLocalizedMessage());
		}
		
		// Set the total execution time
		tr.setExecutionTimeMillis(System.currentTimeMillis() - t);
		
		return tr;
	}
	
	public TestResult[] execute(int runs, boolean isFunctional) {
		
		// Invalid number of runs or no ARC
		if (runs <= 0 || arc == null) {
			return new TestResult[0];
		}
		
		// Remove ConTest logs
		String ad = arc.getARCDirectory();
		char ds = FileUtils.getDirectorySeparator(ad);
		File contestLogDir = new File(ad + ds + "com_ibm_contest" + ds + "instLogs");
		if (contestLogDir.isDirectory()) {
			FileUtils.remove(contestLogDir.getPath());
			contestLogDir.mkdirs();
		}
		
		// Execute the tests
		TestResult[] results = new TestResult[runs];
		results[0] = execute(isFunctional);
		if (results[0].getStatus() == TestStatus.INVALID) {
			TestResult[] err = {results[0]};
			return err;
		}
		for (int i = 1; i < runs; i ++) {
			results[i] = execute(isFunctional);
		}
		
		return results;
	}
	
	private void runProcess(ProcessBuilder pb, TestResult tr, boolean isFunctional)
			throws IOException {
		
		// Start the process
		final int SLEEP_INTERVAL = 150;
		Process p = pb.start();
		long t = System.currentTimeMillis();
		String tms = arc.getSetting(ARC.SETTING_TIMEOUT_MILLIS);
		int millisRemaining = 300000;
		if (tms != null && !tms.isEmpty()) {
			try {
				millisRemaining = Integer.parseInt(tms);
			} catch (Exception e) {
				e.printStackTrace();
				tr.addWarning("Warning: value for '" + ARC.SETTING_TIMEOUT_MILLIS +
						"' could not be parsed to an integer.");
			}
		}
		tr.addInfo("Max program execution time: " + millisRemaining + "ms");
		
		// Wait for the program to finish or run out of time
		while (millisRemaining > 0) {
			try {
				Thread.sleep(SLEEP_INTERVAL);
			} catch (Exception e) {e.printStackTrace();}
			if (!p.isAlive()) {
				t = System.currentTimeMillis() - t - SLEEP_INTERVAL / 2;
				break;
			}
			millisRemaining -= SLEEP_INTERVAL;
		}
		
		// If the process is running, kill it
		if (p.isAlive()) {
			p.destroy();
			t = System.currentTimeMillis() - t;
		}
		
		// Get the info
		ProcessResult pr = new ProcessResult(p);
		pr.readStreams();
		String stdout = pr.getSTDOUT();
		tr.addInfo("STDOUT='" + stdout + "'");
		tr.addError("STDERR='" + pr.getSTDERR() + "'");
		tr.addInfo("Exit code: " + p.exitValue());
		if (stdout == null) {
			stdout = "";
		}
		
		// Process finished
		if (millisRemaining > 0) {
		
			// Find the number of tests and failures
			Pattern pattern = Pattern.compile("Tests run: (\\d+),\\s+Failures: (\\d+)");
			Matcher m = pattern.matcher(stdout);
			boolean parsingErr = false;
			if (m.find()) {
				try {
					tr.tests = Integer.parseInt(m.group(1));
					tr.failures = Integer.parseInt(m.group(2));
				} catch (Exception e) {
					e.printStackTrace();
					tr.addError("Error: unable to fully parse # of tests and failures.");
					parsingErr = true;
				}
			} else {
				tr.addWarning("Warning: unable to locate # of tests and failures.");
			}
			
			// Find the number of successes
			pattern = Pattern.compile("OK \\((\\d+) test");
			m = pattern.matcher(stdout);
			if (m.find()) {
				try {
					tr.successes = Integer.parseInt(m.group(1));
				} catch (Exception e) {
					e.printStackTrace();
					tr.addError("Error: unable to fully parse # of successes.");
					parsingErr = true;
				}
			} else {
				tr.addWarning("Warning: unable to locate # of successes.");
			}
			
			// Failed to parse
			if (parsingErr) {
				tr.addInfo("Unknown: could not evaluate result.");
				tr.setStatus(TestStatus.UNKNOWN);
			}
			
			// Some tests have failed, so assume data race bug
			else if (tr.tests > 0 && tr.failures > 0) {
				tr.addInfo("Error: data race detected, some tests failed.");
				tr.setStatus(TestStatus.DATA_RACE);
			}
			
			// No tests completed, so assume deadlock
			else if (tr.tests == 0 && tr.successes == 0) {
				tr.addInfo("Error: deadlock detected, no tests or successes.");
				tr.setStatus(TestStatus.DEADLOCK);
			}
			
			// There were some successful tests
			else if (tr.successes > 0 || (tr.tests > 0 && tr.failures == 0)) {
				int totalSuccesses = tr.tests > 0? tr.tests : tr.successes;
				
				// Some error occurred
				if (totalSuccesses == 0) {
					tr.addError("Error: no tests executed.");
					tr.setStatus(TestStatus.FAILED);
				}
				
				// Some successful tests
				else {
					tr.addInfo("Success: execution was successful.");
					tr.setStatus(TestStatus.SUCCESS);
				}
			}
		}
		
		// Process did not finish in time and has Java-detected deadlock
		else if (stdout.indexOf("Java-level deadlock:") >= 0) {
			tr.addInfo("Error: Java-level deadlock detected.");
			tr.setStatus(TestStatus.DEADLOCK);
		}
		
		// Process did not finish in time
		else {
			if (isFunctional) {
				tr.addInfo("Error: timeout (process did not finish in time).");
				tr.setStatus(TestStatus.TIMEOUT);
			} else { // not enough info to determine bug type, assume deadlock
				tr.addInfo("Error: deadlock/timeout (process did not finish in time).");
				tr.setStatus(TestStatus.DEADLOCK);
			}
		}
		
		tr.setProgramTimeMillis(t);
	}
	
	public ARC getARC() {
		return arc;
	}
	
	public void setARC(ARC arc) {
		this.arc = arc;
	}
}
