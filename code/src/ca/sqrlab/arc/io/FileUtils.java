package ca.sqrlab.arc.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The {@code FileUtils} class provides a number of methods for working with
 * files on the local file-system.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public class FileUtils {
	
	/**
	 * Gets the separator character for directories based on the directory
	 * provided. If the system is UNIX based, this method will return '/'. If
	 * the operating system is Microsoft Windows, the method will return '\\'
	 * if the path appears to be absolute otherwise '/'.
	 * 
	 * @param dir	a directory/file path.
	 * @return the character which separates directories (either '/' or '\\').
	 */
	public static char getDirectorySeparator(String dir) {
		
		// No directory
		if (dir == null || dir.isEmpty()) {
			return '/';
		}
		
		// If on UNIX system, separator is /
		if (System.lineSeparator().length() == 1) {
			return '/';
		}
		
		return dir.indexOf("/") > 0? '/' : '\\';
	}
	
	/**
	 * Copies a file or directory from one location to another. If the source
	 * is a directory, the sub-directories and their contents can be
	 * recursively copied.
	 * 
	 * @param src		the source path to copy from.
	 * @param dest		the destination path to copy to.
	 * @param recursive	if true, the all of the source's files and
	 * 					sub-directories are copied.
	 */
	public static void copy(String src, String dest, boolean recursive) {

		// First check if the paths exist
		if (src == null || src.isEmpty() || dest == null || dest.isEmpty() ||
				src.equals(dest)) {
			return;
		}
		File s = new File(src), d = new File(dest);
		if (!s.exists() || !s.canRead()) {
			return;
		}

		// Source is a file
		char ds = getDirectorySeparator(src);
		if (s.isFile()) {
			String dp = d.isFile()? dest : dest + ds + s.getName();
		    InputStream is = null;
		    OutputStream os = null;
		    try {
				is = new FileInputStream(s);
				os = new FileOutputStream(dp);
				byte[] buffer = new byte[1024];
				int length = 0;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
		    } catch(IOException e) {
		    	e.printStackTrace();
		    }
		    
		    // Close the streams
		    finally {
		    	try {
		    		is.close();
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
		    	try {
		    		os.close();
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
		    }
		}
		
		// Source (and destination) is a directory
		else if (d.isDirectory()) {
			File[] files = s.listFiles();
			for (File f : files) {
				
				// File is directory
				if (f.isDirectory()) {
					File dp = new File(dest + ds + f.getName());
					if (!dp.exists()) {
						dp.mkdir();
					} if (recursive) {
						copy(f.getPath(), dp.getPath(), recursive);
					}
				}
				
				// File is a file
				else {
					copy(f.getPath(), d.getPath(), false);
				}
			}
		}
	}
	
	/**
	 * Removes (deletes) the path from the file system. If the path is a file,
	 * the file is deleted. If the path is a directory, the entire directory
	 * (including sub-directories) and all it's contents are removed.
	 * 
	 * @param path	the path to the directory or file to remove.
	 */
	public static void remove(String path) {
		
		// Nothing to do
		if (path == null || path.isEmpty()) {
			return;
		}
		File f = new File(path);
		if (!f.exists()) {
			return;
		}
		
		// File
		if (f.isFile()) {
			f.delete();
		}
		
		// Directory
		else {
			File[] contents = f.listFiles();
			for (File dc : contents) {
				if (dc.isDirectory()) {
					remove(dc.getPath());
				}
				dc.delete();
			}
			f.delete();
		}
	}
	
	/**
	 * Converts an arbitrary string into a valid, absolute, system path. If the
	 * path is null, empty, or is not a directory - then the current working
	 * directory is returned.
	 * 
	 * @param path	the directory path.
	 * @return an absolute version of the path passed or the current working
	 * directory.
	 * 
	 * @since 1.0
	 */
	public static String asValidPath(String path) {
		
		// Not a valid directory
		if (path == null || path.isEmpty()) {
			path = System.getProperty("user.dir");
		}
		File dir = new File(path);
		if (!dir.isDirectory()) {
			dir = new File(System.getProperty("user.dir"));
		}
		
		// Try to get the full path
		try {
			path = dir.getAbsoluteFile().getPath();
		} catch (SecurityException e) {
			e.printStackTrace();
			path = System.getProperty("user.dir");
		}
		
		// Remove any /. at the end of the path
		while (path.endsWith("/.") || path.endsWith("\\.")) {
			path = path.substring(0, path.length() - 2);
		}
		
		return path;
	}
	
	/**
	 * Finds all files and directories which match the regular expression
	 * provided in the source directory and optionally all sub-directories.
	 * 
	 * @param srcDir	the source directory path.
	 * @param regex		the regular expression to validate file/directory names.
	 * @param recursive	if true, all sub-directories will be checked.
	 * @return a list of file objects which match the criteria or an empty list.
	 * 
	 * @since 1.0
	 */
	public static List<File> find(String srcDir, String regex, boolean recursive) {
		
		// Not a valid directory
		List<File> files = new ArrayList<>();
		if (srcDir == null || srcDir.isEmpty()) {
			return files;
		}
		File dir = new File(srcDir);
		if (!dir.isDirectory()) {
			return files;
		}
		
		// Create the pattern
		Pattern pattern = null;
		if (regex != null && !regex.isEmpty() && !regex.equals("*")) {
			try {
				pattern = Pattern.compile(regex);
			} catch (Exception e) {
				e.printStackTrace();
				return files;
			}
		}
		
		find(files, dir, pattern, recursive);
		
		return files;
	}
	
	private static void find(List<File> files, File dir, Pattern pattern, boolean recursive) {
		
		File[] fs = dir.listFiles();
		if (fs == null || fs.length == 0) {
			return;
		}
		
		// No pattern
		if (pattern == null) {
			for (File f : fs) {
				files.add(f);
				if (recursive && f.isDirectory()) {
					find(files, f, pattern, recursive);
				}
			}
		}
		
		// A pattern exists
		else {
			for (File f : fs) {
				if (pattern.matcher(f.getName()).matches()) {
					files.add(f);
				}
				if (recursive && f.isDirectory()) {
					find(files, f, pattern, recursive);
				}
			}
		}
	}
}
