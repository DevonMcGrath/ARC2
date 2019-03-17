package ca.sqrlab.arc.tools.instrumentation;

import java.io.File;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.Project;
import ca.sqrlab.arc.io.ProcessResult;
import ca.sqrlab.arc.tools.monitoring.Logger;

/**
 * The {@code CFlashInstrumentor} uses C-FLASH to instrument projects with
 * noise (random thread delays) at the source code level.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public class CFlashInstrumentor extends Instrumentor {
	
	/** The name of the TXL file which does the annotations. */
	public static final String ANNOTATE_FILE = "annotate.txl";
	
	/** The name of the TXL file which does the noising. */
	public static final String NOISE_FILE = "noise.txl";
	
	/** The process path for TXL to use during execution. */
	private String txl;
	
	/** The C-FLASH TXL file directory (i.e. the one which contains the TXL
	 * files to mutate source code). */
	private String txlFileDir;
	
	/**
	 * Constructs the instrumentor with the default TXL process path and the
	 * specified directory path for the C-FLASH TXL files.
	 * 
	 * @param txlFileDir	the directory which contains the TXL files required
	 * 						for C-FLASH to work.
	 * @since 1.0
	 */
	public CFlashInstrumentor(String txlFileDir) {
		this(txlFileDir, null);
	}
	
	/**
	 * Constructs the instrumentor with the specified TXL process path and the
	 * specified directory path for the C-FLASH TXL files.
	 * 
	 * @param txlFileDir	the directory which contains the TXL files required
	 * 						for C-FLASH to work.
	 * @param txl			the TXL process path.
	 * @since 1.0
	 */
	public CFlashInstrumentor(String txlFileDir, String txl) {
		this.txlFileDir = txlFileDir;
		setTxl(txl);
	}

	@Override
	protected void checkDependencies(Logger result) {
		
		// Check TXL
		if (txl == null || txl.isEmpty()) {
			result.fatalError("The TXL executable path is not set.");
			return;
		}
		
		// Check for the TXL directory
		if (txlFileDir == null || txlFileDir.isEmpty() ||
				!(new File(txlFileDir)).isDirectory()) {
			result.fatalError("The source TXL file to do the instrumentation"
					+ " does not exist at the specified path: " + txlFileDir);
			return;
		}
		
		// Check for files
		String[] files = {ANNOTATE_FILE, NOISE_FILE};
		String ds = File.pathSeparatorChar == ':'? "/" : "\\";
		String base = txlFileDir;
		if (!base.endsWith(ds)) {
			base += ds;
		}
		for (String f : files) {
			if (!(new File(base + f)).isFile()) {
				result.fatalError("Missing file '" + f +
						"', in directory: " + base);
			}
		}
	}

	@Override
	protected void runInstrumentation(Logger result, Project project) {
		
		// Check if the project has any source files
		String[] sourceFiles = project.getSourceFiles();
		if (sourceFiles == null || sourceFiles.length == 0) {
			result.warning("The project has no files.");
			return;
		}
		
		// Build the paths
		String ds = File.pathSeparatorChar == ':'? "/" : "\\";
		String projectRoot = project.getSetting(ARC.SETTING_PROJECT_DIR);
		String base = txlFileDir;
		if (!base.endsWith(ds)) {
			base += ds;
		} if (!projectRoot.endsWith(ds)) {
			projectRoot += ds;
		}
		String annotateFile = base + ANNOTATE_FILE;
		String noiseFile = base + NOISE_FILE;
		
		// Noise each of the files
		for (String sf : sourceFiles) {
			
			// Nothing to do
			if (sf == null || sf.isEmpty() || !sf.endsWith(".java")) {
				continue;
			}
			
			// Make sure the file exists
			String path = projectRoot + sf;
			File f = new File(path);
			if (!f.isFile()) {
				result.warning("Failed to find file '" + sf
						+ "' in project directory: " + projectRoot);
				continue;
			}
			File cwd = f.getParentFile();
			
			try {
				
				// Annotate the file
				// txl -q /path/to/JavaFile.java -o /path/to /...ANNOTATE_FILE - -count 1
				String cmdStart = txl + " -q " + f.getName()
					+ " -o " + f.getName() + " ";
				String annotateCmd = cmdStart + annotateFile + " - -count 1";
				ProcessResult pr = new ProcessResult(
						Runtime.getRuntime().exec(annotateCmd, null, cwd));
				pr.readStreams();
				result.debug("Annotate STDOUT: '" + pr.getSTDOUT() + "'");
				result.debug("Annotate STDERR: '" + pr.getSTDERR() + "'");
				
				// Noise the file
				pr = new ProcessResult(Runtime.getRuntime().exec(cmdStart + noiseFile
						+ " - -start 1 -end " + Integer.MAX_VALUE, null, cwd));
				pr.readStreams();
				result.debug("Noise STDOUT: '" + pr.getSTDOUT() + "'");
				result.debug("Noise STDERR: '" + pr.getSTDERR() + "'");
				result.debug("Successfully noised '" + sf + "'.");
				
			} catch (Exception e) {
				result.fatalError("Failed to noise file '" + sf
						+ "' in project directory: " + projectRoot +
						". " + e.getLocalizedMessage());
				break;
			}
		}
	}

	/**
	 * Gets the TXL process path which this instrumentor will use during the
	 * instrumentation process (after {@link #instrument(Project)} is called).
	 * 
	 * @return the TXL process path.
	 * @see #setTxl(String)
	 * @since 1.0
	 */
	public String getTxl() {
		return txl;
	}

	/**
	 * Sets the TXL process path to use as TXL. If the value provided is null
	 * or an empty string, the default TXL process path
	 * ({@code /usr/local/bin/txl}) will be used.
	 * 
	 * @param txl	the TXL process path.
	 * @see #getTxl()
	 * @since 1.0
	 */
	public void setTxl(String txl) {
		if (txl == null || txl.isEmpty()) {
			txl = "/usr/local/bin/txl";
		}
		this.txl = txl;
	}

	/**
	 * Gets the directory path to the C-FLASH TXL files used for annotation and
	 * noising of Java source files.
	 * 
	 * @return the C-FLASH TXL directory path.
	 * @see #setTxlFileDir(String)
	 * @since 1.0
	 */
	public String getTxlFileDir() {
		return txlFileDir;
	}

	/**
	 * Sets the directory path to the C-FLASH TXL files.
	 * 
	 * @param txlFileDir	the directory which contains the TXL files required
	 * 						for C-FLASH to work.
	 * @see #getTxlFileDir()
	 * @since 1.0
	 */
	public void setTxlFileDir(String txlFileDir) {
		this.txlFileDir = txlFileDir;
	}
}
