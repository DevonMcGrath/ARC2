package ca.sqrlab.arc.tools.instrumentation;

import java.io.File;
import java.util.List;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.Project;
import ca.sqrlab.arc.io.FileUtils;
import ca.sqrlab.arc.io.ProcessResult;
import ca.sqrlab.arc.java.JavaFile;
import ca.sqrlab.arc.java.JavaProject;
import ca.sqrlab.arc.tools.monitoring.Logger;

public class ConTestInstrumentor extends Instrumentor {
	
	private String contestJar;
	
	private String cfparseJar;
	
	private String projectClassDir;
	
	private String javaPath;
	
	public ConTestInstrumentor(String contestJar,
			String cfparseJar, String projectClassDir) {
		this.contestJar = contestJar;
		this.cfparseJar = cfparseJar;
		this.projectClassDir = projectClassDir;
		setJavaPath(null);
	}
	
	public ConTestInstrumentor(String contestJar,
			String cfparseJar, String projectClassDir, String javaPath) {
		this.contestJar = contestJar;
		this.cfparseJar = cfparseJar;
		this.projectClassDir = projectClassDir;
		setJavaPath(javaPath);
	}

	@Override
	protected void runInstrumentation(Logger result, Project project) {
		
		File dir = new File(projectClassDir);
		
		// Get all the files
		String projectRoot = project.getSetting(ARC.SETTING_PROJECT_DIR);
		JavaProject jp = new JavaProject(projectRoot);
		List<JavaFile> files = jp.getJavaFiles();
		String classes = "";
		for (JavaFile f : files) {
			classes += f.getClassfileName() + " ";
		}
		if (files.isEmpty()) {
			result.fatalError("No input files in directory '" +
					projectRoot + "'.");
			return;
		}
		
		// Try to start the process
		Process p = null;
		char s = File.pathSeparatorChar;
		String cmd = javaPath + " -cp ." + s + cfparseJar + s +
				contestJar + " com.ibm.contest.instrumentation.Instrument " + classes;
		result.debug("Instrumentation Command: " + cmd);
		try {
			p = Runtime.getRuntime().exec(cmd, null, dir);
		} catch (Exception e) {
			result.fatalError(
					"Instrument process failed: " + e.getLocalizedMessage());
			result.setFatalError(true);
			return;
		}
		
		// Determine if the instrumentation was successful
		ProcessResult pr = new ProcessResult(p);
		pr.readStreams();
		char ds = FileUtils.getDirectorySeparator(projectClassDir);
		String stdout = pr.getSTDOUT(), stderr = pr.getSTDERR(), msg = "";
		for (JavaFile f : files) {
			if (!(new File(projectClassDir + ds +
					f.getClassfileName() + "_backup")).isFile()) {
				msg += "Missing instrumented class: '" + f.getClassName() + "'\n";
			}
		}
		result.debug("STDOUT='" + stdout + "'");
		result.debug("STDERR='" + stderr + "'");
		if (!msg.isEmpty()) { // an error occurred
			result.fatalError(msg.substring(0, msg.length() - 1));
			result.setFatalError(true);
		} else { // all classes were instrumented
			result.debug("Instrumentation successful.");
		}
	}

	@Override
	protected void checkDependencies(Logger result) {
		
		// Check ConTest files
		String fatalError = "";
		boolean missingConTest = false, missingCfparse = false;
		if (contestJar == null || contestJar.isEmpty() ||
				!(new File(contestJar)).isFile()) {
			missingConTest = true;
		}
		if (cfparseJar == null || cfparseJar.isEmpty() ||
				!(new File(cfparseJar)).isFile()) {
			missingCfparse = true;
		}
		
		// Determine error message, if any
		if (missingConTest && missingCfparse) {
			fatalError = "Missing ConTest.jar and cfparse.jar.";
		} else if (missingConTest) {
			fatalError = "Missing ConTest.jar";
		} else if (missingCfparse) {
			fatalError = "Missing cfparse.jar";
		}
		
		// Check the project class directory
		if (projectClassDir == null || projectClassDir.isEmpty() ||
				!(new File(projectClassDir)).isDirectory()) {
			if (!fatalError.isEmpty()) {
				fatalError += "\n";
			}
			fatalError += "Project Class Directory '" + projectClassDir +
					"' does not exist.";
		}
		
		if (!fatalError.isEmpty()) {
			result.fatalError(fatalError);
		}
	}

	public String getContestJar() {
		return contestJar;
	}

	public void setContestJar(String contestJar) {
		this.contestJar = contestJar;
	}

	public String getCfparseJar() {
		return cfparseJar;
	}

	public void setCfparseJar(String cfparseJar) {
		this.cfparseJar = cfparseJar;
	}

	public String getJavaPath() {
		return javaPath;
	}

	public void setJavaPath(String javaPath) {
		if (javaPath == null || javaPath.isEmpty()) {
			javaPath = "java";
		}
		this.javaPath = javaPath;
	}
}
