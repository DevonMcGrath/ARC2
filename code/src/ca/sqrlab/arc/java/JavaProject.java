package ca.sqrlab.arc.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JavaProject {
	
	private String path;
	
	private List<JavaFile> javaFiles;
	
	public JavaProject() {
		setPath(null);
	}
	
	public JavaProject(String path) {
		setPath(path);
	}
	
	public JavaProject(File dir) {
		setPath(dir != null? dir.getPath() : null);
	}
	
	private void getJavaFiles(File dir) {
		
		// Not a directory
		if (dir == null || !dir.isDirectory()) {
			return;
		}
		
		// Add the files
		File[] files = dir.listFiles();
		if (files != null) {
			
			// Add the files from the current directory
			for (File f : files) {
				if (f.isFile()) {
					JavaFile jf = JavaFile.fromFile(f);
					if (jf != null && jf.isInCorrectDirectory()) {
						this.javaFiles.add(jf);
					}
				}
			}
			
			// Add the files in sub-directories
			for (File f : files) {
				if (f.isDirectory()) {
					getJavaFiles(f);
				}
			}
		}
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		
		// Set the path
		this.path = path;
		
		// Get the Java files
		this.javaFiles = new ArrayList<>();
		if (path != null) {
			getJavaFiles(new File(path));
		}
	}
	
	public List<JavaFile> getJavaFiles() {
		return javaFiles;
	}
	
	public List<JavaFile> getMainJavaFiles() {
		
		// Nothing to do
		List<JavaFile> files = new ArrayList<>();
		if (javaFiles == null || javaFiles.isEmpty()) {
			return files;
		}
		
		// Check each of the files
		for (JavaFile f : javaFiles) {
			if (f.hasMain()) {
				files.add(f);
			}
		}
		
		return files;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaProject other = (JavaProject) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[path='" + path + "']";
	}
}
