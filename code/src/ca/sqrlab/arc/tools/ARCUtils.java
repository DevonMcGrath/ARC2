package ca.sqrlab.arc.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.Project;
import ca.sqrlab.arc.io.FileUtils;
import ca.sqrlab.arc.tools.compilation.AntCompiler;
import ca.sqrlab.arc.tools.compilation.ProjectCompiler;
import ca.sqrlab.arc.tools.monitoring.Logger;

/**
 * The {@code ARCUtils} class contains various methods that are useful in
 * different stages of ARC.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public class ARCUtils {

	/**
	 * Compiles the program in the ARC project directory. The compiler
	 * determines if it was successful, as per its implementation.
	 * 
	 * @param arc	the current instance of ARC.
	 * @param l		the logger to document the process.
	 * @return true if and only if the program was successfully compiled.
	 * 
	 * @see ProjectCompiler
	 * @see ARC#SETTING_PROJECT_DIR
	 * @since 1.0
	 */
	public static boolean compile(ARC arc, Logger l) {
		
		// Create the compiler
		String projectRoot = arc.getSetting(ARC.SETTING_PROJECT_DIR);
		String compileTarget = arc.getSetting(ARC.SETTING_PROJECT_COMPILE_CMD);
		String antPath = arc.getSetting(ARC.SETTING_ANT);
		ProjectCompiler compiler = new AntCompiler(projectRoot, compileTarget, antPath);
		
		// Try to compile
		Logger result = compiler.compile();
		if (l != null) {
			l.setFatalError(result.hasFatalError());
			l.getPhases().addAll(result.getPhases());
		}
		
		return !result.hasFatalError();
	}
	
	/**
	 * Attempts to copy the original project to the working project directory.
	 * 
	 * @param arc	the current instance of ARC.
	 * @param l		the logger to document the process.
	 * @return true if and only if the original project was copied successfully.
	 * 
	 * @since 1.0
	 */
	public static boolean copyOriginalProject(ARC arc, Logger l) {
		String src = arc.getSetting(ARC.SETTING_ORIGINAL_PROJECT_DIR);
		String dst = arc.getSetting(ARC.SETTING_PROJECT_DIR);
		return copyProject(src, dst, l);
	}
	
	/**
	 * Attempts to copy a project from a source directory to a destination
	 * directory.
	 * 
	 * @param src	the source directory.
	 * @param dst	the destination directory.
	 * @param l		the logger to document the process.
	 * @return true if and only if the project was copied successfully.
	 * 
	 * @since 1.0
	 */
	public static boolean copyProject(String src, String dst, Logger l) {
		
		if (l == null) {
			l = new Logger();
		}
		
		l.debug("Copying project: src='" + src + "', dest='" + dst + "'");
		if (src == null || dst == null || src.isEmpty() || dst.isEmpty()) {
			l.fatalError("Invalid source/destination pair.");
			return false;
		} if (src.equals(dst)) { // trivial case
			l.debug("Source and destination are the same.");
			return true;
		}
		File srcDir = new File(src);
		File destDir = new File(dst);
		if (!srcDir.isDirectory()) {
			l.fatalError("Source is not a directory.");
			return false;
		} if (!destDir.isDirectory()) {
			l.fatalError("Destination is not a directory.");
			return false;
		}
		
		// Clean the directory
		FileUtils.remove(dst);
		try {
			destDir.mkdirs();
		} catch (Exception e) {}
		
		// Copy the project
		if (!destDir.isDirectory()) {
			l.fatalError("Could not copy project.");
			return false;
		} else {
			FileUtils.copy(src, dst, true);
		}
		
		// Check to make sure it was copied properly
		Project p = new Project(dst);
		if (!p.isValidProject()) {
			l.fatalError("Could not copy project.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Attempts to copy only the project's source files from one root directory
	 * to another. Both the source and destination directories must exist. If
	 * the destination directories do not exist, they will be created.
	 * 
	 * @param arc	the current instance of ARC.
	 * @param src	the source root directory (e.g. the project directory).
	 * @param dst	the destination root directory.
	 * @param l		the logger to keep track of events.
	 * @return true if and only if no error occurred while copying files or
	 * creating directories.
	 * 
	 * @see Project#getSourceFiles()
	 * @since 1.0
	 */
	public static boolean copyProjectSourceFiles(ARC arc,
			String src, String dst, Logger l) {
		
		if (l == null) {
			l = new Logger();
		}
		
		l.debug("Copying project source files: src='" + src +
				"', dest='" + dst + "'");
		
		// Check project
		if (arc == null) {
			l.fatalError("No ARC.");
			return false;
		}
		Project project = arc.getProject();
		if (project == null) {
			l.fatalError("No project.");
			return false;
		}
		String[] sourceFiles = project.getSourceFiles();
		if (sourceFiles == null || sourceFiles.length == 0) {
			l.debug("No Java source files to copy.");
			return true;
		}
		
		// Check directories
		if (src == null || dst == null || src.isEmpty() || dst.isEmpty()) {
			l.fatalError("Invalid source/destination pair.");
			return false;
		} if (src.equals(dst)) { // trivial case
			l.debug("Source and destination are the same.");
			return true;
		}
		File srcDir = new File(src);
		File destDir = new File(dst);
		if (!srcDir.isDirectory()) {
			l.fatalError("Source is not a directory.");
			return false;
		} if (!destDir.isDirectory()) {
			l.fatalError("Destination is not a directory.");
			return false;
		}
		
		List<File> valid = new ArrayList<>();
		String ds = arc.getSetting(ARC.SETTING_DIR_SEPARATOR);
		if (!src.endsWith(ds)) {
			src += ds;
		} if (!dst.endsWith(ds)) {
			dst += ds;
		}
		
		// Check the source directory and copy if valid
		for (String relPath : sourceFiles) {
			if (relPath == null || relPath.isEmpty()) {
				continue;
			}

			// Check if the source file is there
			String path = src + relPath;
			File f = new File(path);
			if (!f.isFile()) {
				l.warning("No file found at path '" + path + "'.");
				continue;
			}
			valid.add(f);
			
			// Check if the destination directory is there
			path = dst + relPath;
			f = new File(path).getParentFile();
			if (f != null && !f.isDirectory()) {
				if (!f.mkdirs()) {
					l.fatalError("Unable to create directory to place file '"
							+ path + "' in.");
					return false;
				}
			}
			
			// Copy the file
			FileUtils.copy(src + relPath, f.getAbsolutePath(), false);
			if (!(new File(path)).isFile()) {
				l.fatalError("Unable to copy file '" + relPath + "'.");
				return false;
			}
		}
		if (valid.isEmpty()) {
			l.warning("No valid source files to copy.");
			return true;
		}
		
		return true;
	}
}
