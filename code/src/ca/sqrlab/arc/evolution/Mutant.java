package ca.sqrlab.arc.evolution;

import java.util.Arrays;

/**
 * The {@code Mutant} class represents a mutant of the original project (or for
 * generalization purposes, the original project). Mutant {@code A} is not
 * equal to another mutant {@code B} if and only if there exists one or more
 * {@link #getFiles()} paths which are different.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public class Mutant {
	
	/** The paths to all of the Java files which create this mutant. */
	private String[] files;
	
	public Mutant() {
		setFiles(null);
	}
	
	public Mutant(String[] files) {
		setFiles(files);
	}
	
	public Mutant copy() {
		
		Mutant copy = new Mutant();
		int n = files == null? 0 : files.length;
		String[] cfiles = new String[n];
		for (int i = 0; i < n; i ++) {
			cfiles[i] = files[i];
		}
		copy.setFiles(cfiles);
		
		return copy;
	}

	public String[] getFiles() {
		return files;
	}

	public Mutant setFiles(String[] files) {
		if (files == null) {
			files = new String[0];
		}
		this.files = files;
		sortFiles();
		return this;
	}
	
	/**
	 * Sorts the file paths in ascending order.
	 * 
	 * @since 1.0
	 */
	private void sortFiles() {
		
		// Nothing to do
		if (files == null || files.length < 2) {
			return;
		}
		
		// Sort the files
		int n = files.length;
		for (int i = 0; i < n; i ++) {
			int min = i;
			for (int j = i + 1; j < n; j ++) {
				if (files[j].compareTo(files[min]) < 0) {
					min = j;
				}
			}
			
			if (min != i) {
				String tmp = files[min];
				this.files[min] = files[i];
				this.files[i] = tmp;
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(files);
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
		Mutant other = (Mutant) obj;
		if (!Arrays.equals(files, other.files))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		
		String out = getClass().getSimpleName() + "[files=[";
		if (files == null) {
			return out + "]]";
		}
		
		// Add the files
		String middle = "";
		for (String f : files) {
			middle += f + ", ";
		}
		if (files.length > 0) {
			middle = middle.substring(0, middle.length() - 2);
		}
		
		return out + middle + "]]";
	}
}
