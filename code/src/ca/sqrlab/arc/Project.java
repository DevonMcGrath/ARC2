package ca.sqrlab.arc;

import java.io.File;

import ca.sqrlab.arc.io.FileUtils;
import ca.sqrlab.arc.tools.AntBuildFile;
import ca.sqrlab.arc.tools.SettingsManager;

public class Project extends SettingsManager {
	
	public static final String PROJECT_CONFIG_FILE = "arc-config.txt";
	
	public static final String PROJECT_CONFIG_FILE_REGEX = "arc\\-config\\.txt";
	
	public static final String PROJECT_BUILD_FILE = "build.xml";
	
	public static final String PROJECT_TEST_MB = "PROJECT_TEST_MB";
	
	public static final String PROJECT_CLASSPATH = "PROJECT_CLASSPATH";
	
	public static final String PROJECT_TESTSUITE = "PROJECT_TESTSUITE";
	
	private String directory;
	
	private AntBuildFile buildFile;
	
	public Project(String directory) {
		super();
		this.buildFile = new AntBuildFile();
		setDirectory(directory);
	}
	
	public int loadSettings() {
		
		// Not a valid project, so don't load anything
		if (!isValidProject()) {
			return -1;
		}
		
		// Parse the settings file
		String d = getSetting(ARC.SETTING_ORIGINAL_PROJECT_DIR);
		char dirSep = FileUtils.getDirectorySeparator(d);
		clearSettings();
		int parsed = parseFromFile(d + dirSep + PROJECT_CONFIG_FILE);
		super.setSetting(ARC.SETTING_ORIGINAL_PROJECT_DIR, d);
		
		return parsed;
	}
	
	public boolean isValidProject() {
		
		// Check that the project directory exists
		String d = getSetting(ARC.SETTING_ORIGINAL_PROJECT_DIR);
		if (d == null || d.isEmpty() || !(new File(d)).isDirectory()) {
			return false;
		}
		
		// Check that certain files exist in the directory
		String[] toCheck = {PROJECT_CONFIG_FILE, PROJECT_BUILD_FILE};
		char dirSep = FileUtils.getDirectorySeparator(d);
		for (String filename : toCheck) {
			if (!(new File(d + dirSep + filename)).isFile()) {
				return false;
			}
		}
		
		return true;
	}
	
	public AntBuildFile getBuildFile() {
		return buildFile;
	}
	
	@Override
	public String setSetting(String setting, String value) {
		if (setting == null || setting.isEmpty() || isStaticSetting(setting)) {
			return null;
		}
		return super.setSetting(setting, value);
	}
	
	public String getDirectory() {
		return directory;
	}
	
	public void setDirectory(String directory) {
		
		// Update the directory
		directory = FileUtils.asValidPath(directory);
		super.setSetting(ARC.SETTING_ORIGINAL_PROJECT_DIR, directory);
		this.directory = directory;
		
		// Attempt to read the build file
		this.buildFile = new AntBuildFile(directory +
				FileUtils.getDirectorySeparator(directory) + PROJECT_BUILD_FILE);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"[directory='" + directory + "', " + super.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((directory == null)? 0 : directory.hashCode());
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
		Project other = (Project) obj;
		if (directory == null) {
			if (other.directory != null)
				return false;
		} else if (!directory.equals(other.directory))
			return false;
		return true;
	}
	
	/**
	 * Checks if a setting name is reserved as a static setting. Static
	 * settings cannot be changed.
	 * 
	 * @param setting	the name of the setting
	 * @return true if and only if the name of the setting is reserved.
	 * 
	 * @see ARC#isStaticSetting(String)
	 * @see ARC#SETTING_ORIGINAL_PROJECT_DIR
	 * @since 1.0
	 */
	public static boolean isStaticSetting(String setting) {
		
		// Not a valid setting
		if (setting == null || setting.isEmpty()) {
			return false;
		}
		
		// Check if it is a static setting
		final String[] ss = {ARC.SETTING_ORIGINAL_PROJECT_DIR};
		for (String s : ss) {
			if (setting.equals(s)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isInitialized(String path) {
		return path == null || path.isEmpty()? false : isInitialized(new File(path));
	}
	
	public static boolean isInitialized(File dir) {
		
		// Not a directory
		if (dir == null || !dir.isDirectory()) {
			return false;
		}
		
		// Check for the configuration file
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return false;
		}
		for (File f : files) {
			if (f.isFile() && PROJECT_CONFIG_FILE.equals(f.getName())) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean hasBuildFile(String path) {
		return path == null || path.isEmpty()? false : hasBuildFile(new File(path));
	}
	
	public static boolean hasBuildFile(File dir) {
		
		// Not a directory
		if (dir == null || !dir.isDirectory()) {
			return false;
		}
		
		// Check for the build file
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return false;
		}
		for (File f : files) {
			if (f.isFile() && PROJECT_BUILD_FILE.equals(f.getName())) {
				return true;
			}
		}
		
		return false;
	}
}
