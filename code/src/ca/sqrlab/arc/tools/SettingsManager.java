package ca.sqrlab.arc.tools;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.sqrlab.arc.io.FileReader;
import ca.sqrlab.arc.io.FileWriter;

/**
 * The {@code SettingsManager} class acts as a way to store settings in a
 * key-value pair format. Settings values can be dependent on other values and
 * are formatted when retrieved.
 * <p>
 * 
 * <b>Formatting</b><br>
 * 
 * Settings which rely on other settings' values are denoted with
 * ${<em>[other_setting]</em>}. For example, imagine the following settings:<br>
 * ROOT=./src<br>
 * TESTS=${ROOT}/test<br>
 * When ROOT is retrieved with {@link #getSetting(String)}, it will return
 * "./src". However, when TESTS is retrieved - since it is dependent on ROOT -
 * it will be formatted and return "./src/test". If a variable in the
 * formatting does not exist, it will be replaced with an empty string.
 * <p>
 * 
 * <b>Settings File Format</b><br>
 * 
 * Settings can be loaded from a file with {@link #parseFromFile(String)}. The
 * following rules are applied when parsing:<ul>
 * <li>Leading whitespace characters are ignored for each line</li>
 * <li>Lines starting with '#' are ignored.</li>
 * <li>Empty lines are ignored.</li>
 * <li>Each setting is a key-value pair in the form
 * <em>[key]</em>=<em>[value]</em></li>
 * <li>If the setting name exists without any value (i.e. no '=' character),
 * it is added to the settings with a value of "1"</li>
 * </ul>
 * Setting names are not allowed to end with whitespace (as it is removed) or
 * an '=' character. A settings file can also be created through
 * {@link #writeToFile(String)}.
 * <p>
 * 
 * @author Devon McGrath
 * @since ARC1.0
 * @version 1.0
 */
public class SettingsManager {
	
	/** The key-value pairs which represent the settings. */
	protected TreeMap<String, String> settings;
	
	/**
	 * Constructs a settings manager with no instructions.
	 */
	public SettingsManager() {
		this.settings = new TreeMap<>();
	}
	
	/**
	 * Constructs a settings manager from a settings file.
	 * 
	 * @param settingsFile	the path to the settings file.
	 */
	public SettingsManager(String settingsFile) {
		this.settings = new TreeMap<>();
		parseFromFile(settingsFile);
	}
	
	/**
	 * <b><em>parseFromFile</b></em>
	 * <p>
	 * Parses settings from a settings file and adds the settings from the
	 * file to the current settings (overwriting any settings with the same
	 * name).
	 * 
	 * @param path	the path to the settings file.
	 * @return the number of settings added/updated, or -1 if a file error
	 * occurred.
	 * 
	 * @see {@link #writeToFile(String)}
	 */
	public int parseFromFile(String path) {
		
		// Nothing to do
		if (path == null || path.isEmpty()) {
			return 0;
		}
		File f = new File(path);
		if (!f.isFile()) {
			return 0;
		}
		
		// Get the file data
		List<String> lines = FileReader.read(path);
		if (lines.isEmpty()) {
			return 0;
		}
		
		// Put all the settings in the the map
		int added = 0;
		while (!lines.isEmpty()) {
			String line = lines.remove(0);
			if (line.isEmpty()) {
				continue;
			}
			
			// Clean the line
			while (!line.isEmpty()) {
				char s = line.charAt(0);
				if (s == ' ' || s == '\t' || s == '\0') {
					line = line.substring(1);
				} else {
					break;
				}
			}
			if (line.isEmpty()) {
				continue;
			}
			
			// Parse the line
			if (line.charAt(0) == '#') { // comment line
				continue;
			}
			int idx = line.indexOf('=');
			if (idx < 0) { // a boolean value
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				setSetting(line, "1");
				added ++;
			} else if (idx + 1 < line.length()) { // regular setting
				String setting = line.substring(0, idx).trim();
				String value = line.substring(idx + 1);
				if (value.isEmpty()) {
					continue;
				}
				setSetting(setting, value);
				added ++;
			}
		}
		
		return added;
	}
	
	/**
	 * <b><em>writeToFile</em></b>
	 * <p>
	 * Writes the settings to a file. If there are no settings, this method
	 * will create and empty file.
	 * 
	 * @param path	the path to the file to write to.
	 * @return true if and only if the settings were written to the specified
	 * file.
	 * @see {@link #parseFromFile(String)}
	 */
	public boolean writeToFile(String path) {
		
		// Nothing to do
		if (path == null || path.isEmpty()) {
			return false;
		}
		
		// Build the output
		if (settings == null) {
			return false;
		}
		Set<String> keys = settings.keySet();
		if (keys == null) {
			return false;
		}
		List<String> lines = new ArrayList<>();
		lines.add("# Auto-generated settings file");
		lines.add("");
		for (String k : keys) {
			lines.add(k + "=" + settings.get(k));
		}
		
		return FileWriter.write(path, lines, false);
	}
	
	/**
	 * <b><em>printSettings</em></b>
	 * <p>
	 * Prints the settings in an easy to read format to a specified stream.
	 * 
	 * @param stream	the stream to print to.
	 * 
	 * @see {@link #printSettings()}
	 */
	public void printSettings(PrintStream stream) {
		
		// Nothing to print to
		if (stream == null) {
			return;
		}
		
		// Get the keys
		if (settings == null) {
			stream.println("NULL");
			return;
		}
		Set<String> keys = settings.keySet();
		if (keys == null || keys.isEmpty()) {
			stream.println("EMPTY");
			return;
		}
		
		// Print each of the key-value pairs
		for (String k : keys) {
			String v1 = settings.get(k), v2 = getSetting(k);
			stream.println(k + "=" + v1 + " (evaluates to '" + v2 + "')");
		}
	}
	
	/**
	 * <b><em>printSettings</em></b>
	 * <p>
	 * Prints the settings in an easy to read format to the standard output
	 * stream.
	 * 
	 * @see {@link #printSettings(PrintStream)}
	 */
	public void printSettings() {
		printSettings(System.out);
	}

	/**
	 * <b><em>clearSettings</em></b>
	 * <p>
	 * Clears all the stored settings.
	 * 
	 * @see {@link #setSetting(String, String)}, {@link #getSetting(String)},
	 * {@link #getSettings()}, {@link #clearSettings()}
	 */
	public void clearSettings() {
		this.settings.clear();
	}
	
	/**
	 * <b><em>setSetting</em></b>
	 * <p>
	 * Sets a specific setting to a specified value.
	 * 
	 * @param setting	the name of the setting.
	 * @param value		the value of the setting.
	 * @return the previous value of the setting, or null if it did not exist.
	 * 
	 * @see {@link #getSetting(String)}, {@link #getSettings()}, {@link #clearSettings()}
	 */
	public String setSetting(String setting, String value) {
		if (setting == null || setting.isEmpty() || value == null) {
			return null;
		}
		return settings.put(setting, value);
	}
	
	/**
	 * <b><em>getSetting</em></b>
	 * <p>
	 * Gets the value of a setting. If the setting depends on other values,
	 * it will be formatted. That is, calling {@link #setSetting(String, String)}
	 * then {@link #getSetting(String)} with the same setting name may not return
	 * the value to which it was set to.
	 * <p>
	 * For example, if there are two settings: ROOT=./src and TESTS=${ROOT}/test
	 * - calling this method with TESTS would return "./src/test" instead of
	 * "${ROOT}/test".
	 * 
	 * @param setting	the name of the setting.
	 * @return the formatted value for the setting or null if it does not exist.
	 * 
	 * @see {@link #getSettings()}, {@link #setSetting(String, String)},
	 * {@link #clearSettings()}
	 */
	public String getSetting(String setting) {
		
		// Nothing to do
		if (setting == null || setting.isEmpty()) {
			return null;
		}
		
		// Get the setting
		String v = settings.get(setting);
		if (v == null || v.isEmpty()) {
			return v;
		}
		
		// Format the setting
		Pattern p = Pattern.compile("\\$\\{[^}]+\\}");
		Matcher m = p.matcher(v);
		int idx = 0;
		String res = v;
		while (m.find(idx)) {
			idx = m.end();
			setting = v.substring(m.start() + 2, idx - 1);
			String sv = getSetting(setting);
			res = res.replace("${" + setting + "}", sv == null? "" : sv);
		}
		
		return res;
	}
	
	/**
	 * <b><em>formatWithSettings</em></b>
	 * <p>
	 * Formats an arbitrary string with the settings in this object.
	 * 
	 * @param value	the value to format.
	 * @return the formatted string.
	 */
	public String formatWithSettings(String value) {
		
		// Nothing to do
		if (value == null || value.isEmpty()) {
			return value;
		}
		
		// Format the setting
		Pattern p = Pattern.compile("\\$\\{[^}]+\\}");
		Matcher m = p.matcher(value);
		int idx = 0;
		String res = value;
		while (m.find(idx)) {
			idx = m.end();
			String setting = value.substring(m.start() + 2, idx - 1);
			String sv = getSetting(setting);
			res = res.replace("${" + setting + "}", sv == null? "" : sv);
		}
		
		return res;
	}
	
	/**
	 * <b><em>getSettings</em></b>
	 * <p>
	 * Gets all the raw, unformatted settings as a map. Note: modifications to
	 * this map will also make modifications to the settings. That is, the map
	 * is not a clone of the settings.
	 * 
	 * @return the settings.
	 * 
	 * @see {@link #getSetting(String)}, {@link #setSetting(String, String)},
	 * {@link #clearSettings()}
	 */
	public TreeMap<String, String> getSettings() {
		return settings;
	}
	
	@Override
	public String toString() {
		
		// Get the keys
		String out = getClass().getSimpleName() + "[";
		if (settings == null) {
			return out + "NULL]";
		}
		Set<String> keys = settings.keySet();
		if (keys == null || keys.isEmpty()) {
			return out + "EMPTY]";
		}
		
		// Add all the key value pairs
		for (String k : keys) {
			out += k + "='" + settings.get(k) + "', ";
		}
		out = out.substring(0, out.length() - 2);
		
		return out + "]";
	}
}
