/* Name: FileWriter
 * Authors: Devon McGrath
 * Description: This class is responsible for handling file writing.
 */

package ca.sqrlab.arc.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code FileWriter} class a helper class responsible for writing and
 * appending data to files.
 */
public class FileWriter {

	/**
	 * Writes or appends data to a file. If the file does not exist, one will
	 * be created.
	 *
	 * @param path		the file path.
	 * @param data		the list of data, where each entry is a line.
	 * @param append	if true, data will be appended to the file.
	 * @return true if and only if no I/O error occurred during the write.
	 */
	public static <T> boolean write(String path, List<T> data,
			boolean append) {

		// Not valid path
		if (path == null || path.isEmpty()) {
			return false;
		}

		// Ensure the data exists
		if (data == null) {
			data = new ArrayList<>();
		}

		// Write/append to file
		boolean err = false;
		try(java.io.FileWriter fw = new java.io.FileWriter(path, append);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			// Write each line
			final String end = System.lineSeparator();
			for (T line : data) {
				out.print((line == null? "" : line) + end);
			}
		} catch (IOException e) {
			e.printStackTrace();
			err = true;
		}

		return !err;
	}
}
