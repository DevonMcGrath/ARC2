/* Name: FileReader
 * Authors: Devon McGrath
 * Description: This class reads files and returns their contents.
 */

package ca.sqrlab.arc.io;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * The {@code FileReader} class is responsible for reading and parsing data
 * for the back-end to use.
 */
public class FileReader {

	/**
	 * Reads a file from the specified path and gets the lines from the file.
	 * Note: if the system uses \r\n to indicate a new line, then both the
	 * \r and \n characters are removed from each line.
	 *
	 * @param path	the path to the file to read.
	 * @return the list of lines in the file.
	 */
	public static List<String> read(String path) {

		String line = null;
		List<String> lines = new ArrayList<>();

		// Not valid path
		if (path == null || path.isEmpty()) {
			return lines;
		}

		// Attempt to open the file
		try(java.io.FileReader fr = new java.io.FileReader(path);
				BufferedReader br = new BufferedReader(fr)) {

			// Read the file line by line
			while ((line = br.readLine()) != null) {

				// Remove \r characters at the end of lines
				if (!line.isEmpty() && line.charAt(line.length() - 1) == '\r') {
					line = line.substring(0, line.length() - 1);
				}

				lines.add(line);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}

		return lines;
	}
}
