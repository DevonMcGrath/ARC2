package ca.sqrlab.arc.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaFile {
	
	private String path;
	
	private String packageName;
	
	private String className;
	
	private boolean hasMain;
	
	public JavaFile() {
		
	}
	
	public boolean isInCorrectDirectory() {
		
		// Not a Java file
		if (!isJavaFile(path)) {
			return false;
		}
		
		// No package
		if (packageName == null || packageName.isEmpty()) {
			return true;
		}
		
		// Check the previous directories
		String[] parts = packageName.split("\\.");
		int n = parts.length;
		File dir = new File(path).getParentFile();
		for (int i = n - 1; i >= 0; i --) {
			if (dir == null || !parts[i].equals(dir.getName())) {
				return false;
			}
			dir = dir.getParentFile();
		}
		
		return true;
	}
	
	public String getClassfileName() {
		return className + ".class";
	}
	
	public String getJavaFileName() {
		return className + ".java";
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public boolean hasMain() {
		return hasMain;
	}

	public void setHasMain(boolean hasMain) {
		this.hasMain = hasMain;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[path='" + path + "', package='" +
				packageName + "', class='" + className + "', hasMain=" + hasMain + "]";
	}
	
	public static boolean isJavaFile(String path) {
		
		// Nothing to do
		if (path == null || path.isEmpty() || !path.endsWith(".java")) {
			return false;
		}
		File f = new File(path);
		if (!f.isFile()) {
			return false;
		}
		
		// Check the file name more intensely
		String fn = f.getName();
		return Pattern.compile(
				"(_[_\\$a-zA-Z]+|[\\$a-zA-Z]+)[_\\$a-zA-Z0-9]*\\.java")
				.matcher(fn).matches();
	}

	public static JavaFile fromFile(String path) {
		
		// Not a Java file
		if (!isJavaFile(path)) {
			return null;
		}

		// Make sure we can read the file
		File f = new File(path);
		if (!f.canRead()) {
			return null;
		}
		String fn = f.getName();
		
		// Parse the file
		JavaFile file = new JavaFile();
		file.path = path;
		file.className = fn.substring(0, fn.length() - 5);
		String data = "";
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = is.read(buffer)) > 0) {
				buffer = Arrays.copyOf(buffer, length);
				data += new String(buffer);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Close the file
		finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {}
			}
		}
		
		// Get the package name
		Pattern packagePattern = Pattern.compile(
				"package\\s+[$a-zA-Z_]+[$a-zA-Z_0-9]*(\\.[$a-zA-Z_]+[$a-zA-Z_0-9]*)*\\s*;");
		Matcher m = packagePattern.matcher(data);
		if (m.find()) {
			file.packageName = data.substring(m.start(), m.end())
					.substring(8).replaceAll("[\\s;]+", "");
		} else {
			file.packageName = "";
		}
		
		// Check for the main method
		Pattern mainMethodPattern = Pattern.compile(
				"public\\s+static\\s+void\\s+main\\(" +
				"String(\\.\\.\\.|\\[\\])\\s+[$_a-zA-Z]+[$_a-zA-Z0-9]*\\)");
		m = mainMethodPattern.matcher(data);
		file.hasMain = m.find();
		
		return file;
	}

	public static JavaFile fromFile(File file) {
		return file == null? null : fromFile(file.getPath());
	}
	
	public static boolean isValidClassName(String name) {
		if (name == null) {
			return false;
		}
		return Pattern.compile("(_[_\\$a-zA-Z]+|[\\$a-zA-Z]+)[_\\$a-zA-Z0-9]*")
				.matcher(name).matches();
	}
}
