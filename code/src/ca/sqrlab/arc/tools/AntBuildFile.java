package ca.sqrlab.arc.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The {@code AntBuildFile} class provides functionality to parse a build file
 * used by Ant.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public class AntBuildFile {
	
	/** The project name found in the build file. */
	private String name;
	
	/** The project description found in the build file. */
	private String description;
	
	/** The base directory path found in the build file. */
	private String baseDir;
	
	/** The path to the last build file which was parsed. */
	private String path;
	
	/** The targets found in the build file. */
	private List<AntTarget> targets;
	
	/** The paths found in the build file. */
	private List<AntPath> paths;
	
	/** The properties found in the build file. */
	private SettingsManager properties;

	/**
	 * Constructs an empty ant build file.
	 * @since 1.0
	 */
	public AntBuildFile() {
		this.properties = new SettingsManager();
		this.targets = new ArrayList<>();
		this.paths = new ArrayList<>();
	}
	
	/**
	 * Constructs an ant build file from the specified build file path.
	 * 
	 * @param buildFilePath	the path to the build file.
	 * @since 1.0
	 */
	public AntBuildFile(String buildFilePath) {
		this();
		loadBuildFile(buildFilePath);
	}
	
	/**
	 * Constructs an ant build file from the specified file.
	 * 
	 * @param buildFile	the build file to load.
	 * @since 1.0
	 */
	public AntBuildFile(File buildFile) {
		this();
		if (buildFile != null) {
			loadBuildFile(buildFile.getAbsolutePath());
		}
	}
	
	/**
	 * Loads a build file and gets the targets, paths, and other meta-info
	 * specified in the file.
	 * 
	 * @param filePath	the path to the build file.
	 * @return true if and only if the file was parsed.
	 * @since 1.0
	 */
	public boolean loadBuildFile(String filePath) {
		
		// Check if it exists
		if (filePath == null || filePath.isEmpty()) {
			return false;
		}
		File f = new File(filePath);
		if (!f.isFile() || !f.canRead()) {
			return false;
		}
		
		this.path = filePath;
		
		// Read the file
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
		
		// Clean the file before parsing
		data = data.replaceAll("\\s+", " ").replaceAll(">[ ]+<", "><")
				.replaceAll("<!--.*?-->", "").replaceAll(">\\s+", ">")
				.replaceAll("\\s+<", "<").replaceAll("><", ">\n<")
				.replaceAll("\\s*=\\s*", "=");
		
		// Break into lines
		this.properties.clearSettings();
		this.targets.clear();
		this.paths.clear();
		String[] lines = data.split("\n");
		
		// Parse all the lines
		this.baseDir = ".";
		boolean inPath = false, inTarget = false, inCP = false;
		AntPath path = null;
		AntTarget target = null;
		for (String l : lines) {
			l = l.trim();
			if (l.isEmpty()) {
				continue;
			}
			
			// Adding to path
			if (inPath) {
				
				// End of path
				if (l.indexOf("</path>") >= 0) {
					this.paths.add(path);
					inPath = false;
				}
				
				// Another path element
				else if (l.indexOf("<pathelement ") >= 0) {
					String[] parts = l.split("location=\"", 2);
					if (parts.length < 2) {
						continue;
					}
					String p = parts[1].split("\"")[0];
					if (!inCP) {
						path.paths.add(p);
					} else {
						target.paths.add(p);
					}
				}
				
				// A reference to another path
				else if (l.indexOf("<path ") >= 0) {
					String[] parts = l.split("refid=\"", 2);
					if (parts.length < 2) {
						continue;
					}
					String r = parts[1].split("\"")[0];
					if (!inCP) {
						path.refs.add(r);
					} else {
						target.refs.add(r);
					}
				}
			}
			
			// Start of a path
			else if (l.indexOf("<path ") >= 0) {
				String[] parts = l.split("id=\"", 2);
				path = new AntPath();
				if (parts.length >= 2) {
					path.id = parts[1].split("\"", 2)[0];
				}
				inPath = l.indexOf("/>") < 0;
				if (!inPath) {
					this.paths.add(path);
				}
			}
			
			// More in the target
			else if (inTarget) {
				
				// End of target
				if (l.indexOf("</target>") >= 0) {
					inTarget = false;
					inCP = false;
					this.targets.add(target);
				}
				
				// Check if classpath
				else if (l.indexOf("<classpath ") >= 0) {
					String[] parts = l.split("refid=\"", 2);
					if (parts.length >= 2) {
						target.refs.add(parts[1].split("\"", 2)[0]);
					}
					inCP = l.indexOf("/>") < 0;
				}
			}
			
			// Start of a target
			else if (l.indexOf("<target ") >= 0) {
				target = new AntTarget();
				String[] parts = l.split("name=\"", 2);
				if (parts.length >= 2) {
					target.name = parts[1].split("\"", 2)[0];
				}
				parts = l.split("depends=\"", 2);
				if (parts.length >= 2) {
					target.depends = parts[1].split("\"", 2)[0];
				}
				inTarget = l.indexOf("/>") < 0;
			}
			
			// Line is a description
			else if (l.indexOf("<description") >= 0) {
				String[] parts = l.split(">", 2);
				if (parts.length < 2) {
					continue;
				}
				this.description = parts[1].split("<", 2)[0];
			}
			
			// Line is a property
			else if (l.indexOf("<property ") >= 0) {
				
				// Get the name
				String[] parts = l.split("name=\"", 2);
				if (parts.length < 2) {
					continue;
				}
				String pname = parts[1].split("\"", 2)[0];
				
				// Get the value
				parts = l.split("location=\"", 2);
				if (parts.length < 2) {
					continue;
				}
				String pvalue = parts[1].split("\"", 2)[0];
				
				this.properties.setSetting(pname, pvalue);
			}
			
			// Line is the project info
			else if (l.indexOf("<project ") >= 0) {
				
				// Get the name
				String[] parts = l.split("name=\"", 2);
				if (parts.length >= 2) {
					this.name = parts[1].split("\"", 2)[0];
				}
				
				// Get the base directory
				parts = l.split("basedir=\"", 2);
				if (parts.length >= 2) {
					this.baseDir = parts[1].split("\"", 2)[0];
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Gets the complete classpath for the target of the specified name. If the
	 * target depends on any other targets, those classpaths are added
	 * recursively. If the resulting classpath contains any property names,
	 * those values are replaced with the actual values found in the
	 * properties.
	 * 
	 * @param targetName	the name of the target.
	 * @return the complete classpath for the target.
	 * @see #getPath(String)
	 * @see SettingsManager#formatWithSettings(String)
	 * @since 1.0
	 */
	public String getClassPath(String targetName) {
		
		// No target
		if (targetName == null || targetName.isEmpty()) {
			return "";
		} if (targets == null || targets.isEmpty()) {
			return "";
		}
		
		// Find the target
		AntTarget t = null;
		for (AntTarget tmp : targets) {
			if (tmp != null && targetName.equals(tmp.name)) {
				t = tmp;
				break;
			}
		}
		if (t == null) {
			return "";
		}
		
		// Get any dependent classpath
		String cp = "", sep = File.pathSeparator;
		if (!targetName.equals(t.depends)) {
			cp = getClassPath(t.depends);
		}
		
		// Add any additional paths
		List<String> pl = t.paths;
		if (pl != null) {
			for (String p : pl) {
				if (p.isEmpty()) {
					continue;
				}
				if (!cp.isEmpty()) {
					cp += sep;
				}
				cp += p;
			}
		}
		
		// Get any reference paths
		pl = t.refs;
		if (pl != null) {
			for (String p : pl) {
				if (p.isEmpty()) {
					continue;
				}
				
				// Get the reference path
				String idp = getPath(p);
				if (idp.isEmpty()) {
					continue;
				}
				if (!cp.isEmpty()) {
					cp += sep;
				}
				cp += idp;
			}
		}
		if (cp.isEmpty()) {
			return cp;
		}
		
		// If there are properties, use them
		if (properties != null) {
			cp = properties.formatWithSettings(cp);
		}
		
		// Remove any duplicate path elements
		String parts[] = cp.split(sep), ncp = "";
		List<String> elems = new ArrayList<>();
		for (String p : parts) {
			if (!elems.contains(p)) {
				elems.add(p);
				ncp += sep + p;
			}
		}
		cp = ncp.substring(1);
		
		return cp;
	}
	
	/**
	 * Gets the full classpath for a path of the given ID. If the path
	 * references any other paths, they will be added recursively. Any path
	 * elements which use properties will have their values replaced with the
	 * actual value of the properties.
	 * 
	 * @param id	the path ID.
	 * @return the complete classpath for the path.
	 * @see #getClassPath(String)
	 * @see SettingsManager#formatWithSettings(String)
	 * @since 1.0
	 */
	public String getPath(String id) {
		
		// Invalid ID
		if (id == null || id.isEmpty()) {
			return "";
		}
		
		// No paths
		if (paths == null || paths.isEmpty()) {
			return "";
		}
		
		// Find the path
		AntPath p = null;
		for (AntPath path : paths) {
			if (path != null && id.equals(path.id)) {
				p = path;
				break;
			}
		}
		if (p == null) {
			return "";
		}
		
		// Add any additional paths
		String cp = "", sep = File.pathSeparator;
		List<String> pl = p.paths;
		if (pl != null) {
			for (String pv : pl) {
				if (pv.isEmpty()) {
					continue;
				}
				if (!cp.isEmpty()) {
					cp += sep;
				}
				cp += pv;
			}
		}
		
		// Get any reference paths
		pl = p.refs;
		if (pl != null) {
			for (String pv : pl) {
				if (pv.isEmpty()) {
					continue;
				}
				
				// Get the reference path
				String idp = getPath(pv);
				if (idp.isEmpty()) {
					continue;
				}
				if (!cp.isEmpty()) {
					cp += sep;
				}
				cp += idp;
			}
		}
		if (cp.isEmpty()) {
			return cp;
		}
		
		// If there are properties, use them
		if (properties != null) {
			cp = properties.formatWithSettings(cp);
		}
		
		// Remove any duplicate path elements
		String parts[] = cp.split(sep), ncp = "";
		List<String> elems = new ArrayList<>();
		for (String path : parts) {
			if (!elems.contains(path)) {
				elems.add(path);
				ncp += sep + path;
			}
		}
		cp = ncp.substring(1);
		
		return cp;
	}
	
	/**
	 * Finds all targets with names matching the regular expression provided.
	 * If the pattern is null/empty/ or a single '*', all targets are matched
	 * and this method becomes equivalent to {@link #getTargets()}.
	 * 
	 * @param regex	the regular expression to match target names against.
	 * @return the list of targets matching the regular expression.
	 * @since 1.0
	 */
	public List<AntTarget> getTargetsMatching(String regex) {
		
		List<AntTarget> res = new ArrayList<>();
		if (targets == null || targets.isEmpty()) {
			return res;
		}
		
		// No real matcher
		if (regex == null || regex.isEmpty() || regex.equals("*")) {
			for (AntTarget t : targets) {
				if (t != null) {
					res.add(t);
				}
			}
		}
		
		// There is a matcher, use it
		else {
			for (AntTarget t : targets) {
				if (t != null && Pattern.matches(regex, t.name)) {
					res.add(t);
				}
			}
		}
		
		return res;
	}
	
	/**
	 * Attempts to find a target which can be used to compile the project.
	 * 
	 * @return a target whose name is similar to either 'compile' or 'build',
	 * or null if no such target was found.
	 * @see #getTargetsMatching(String)
	 * @see #getTestTarget()
	 * @since 1.0
	 */
	public AntTarget getCompileTarget() {
		
		// No targets
		if (targets == null || targets.isEmpty()) {
			return null;
		}
		
		// Check for specific target names
		String[] patterns = {"compile", "build", ".*compile.*", ".*build.*"};
		for (String pattern : patterns) {
			List<AntTarget> res = getTargetsMatching(pattern);
			if (!res.isEmpty()) {
				return res.get(0);
			}
		}
		
		return null;
	}
	
	/**
	 * Attempts to find a target which corresponds to testing the project.
	 * 
	 * @return a target whose name is 'test' or has 'test' in the name, or null
	 * if no such target was found.
	 * @see #getTargetsMatching(String)
	 * @see #getCompileTarget()
	 * @since 1.0
	 */
	public AntTarget getTestTarget() {
		
		// No targets
		if (targets == null || targets.isEmpty()) {
			return null;
		}
		
		// Check for specific target names
		String[] patterns = {"test", ".*test.*"};
		for (String pattern : patterns) {
			List<AntTarget> res = getTargetsMatching(pattern);
			if (!res.isEmpty()) {
				return res.get(0);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the name of the project which was found in the build file.
	 * 
	 * @return the project name.
	 * @see #getDescription()
	 * @since 1.0
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the description found in the build file.
	 * 
	 * @return the project description.
	 * @see #getName()
	 * @since 1.0
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the base directory which was parsed from the build file.
	 * 
	 * @return the base directory path.
	 * @since 1.0
	 */
	public String getBaseDir() {
		return baseDir;
	}

	/**
	 * Gets the path to the last build file which was successfully parsed.
	 * 
	 * @return the file system path to the build file.
	 * @see #loadBuildFile(String)
	 * @since 1.0
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Gets the targets from the build file.
	 * 
	 * @return the build file targets.
	 * @since 1.0
	 */
	public List<AntTarget> getTargets() {
		return targets;
	}

	/**
	 * Gets the paths from the build file which were not declared within a target.
	 * 
	 * @return the build file paths.
	 * @since 1.0
	 */
	public List<AntPath> getPaths() {
		return paths;
	}

	/**
	 * Gets the properties which were part of the ant build file. These
	 * properties may appear in various locations within the ant build file.
	 * For example, if there is a property 'jar', it may be referenced as
	 * ${jar}.
	 * 
	 * @return the properties of the build file.
	 * @since 1.0
	 */
	public SettingsManager getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=" + name + ", description="
				+ description + ", baseDir=" + baseDir + ", path=" + path +
				", targets=" + targets + ", paths=" + paths + ", properties="
				+ properties + "]";
	}
	
	/**
	 * The {@code AntTarget} class represents a target in the ant build file.
	 * It contains the target name, the name of the target it depends on, any
	 * paths which make up the classpath, and any references to paths.
	 * 
	 * @author Devon McGrath
	 * @see AntPath
	 * @since 1.0
	 */
	public static class AntTarget {
		
		/** The name of the target. */
		private String name;
		
		/** The name of the target that this target depends on. */
		private String depends;
		
		/** The list of path elements which make up the classpath. */
		private List<String> paths;
		
		/** The list of path IDs which make up the classpath. */
		private List<String> refs;
		
		/**
		 * Constructs an empty target.
		 * @since 1.0
		 */
		public AntTarget() {
			this.paths = new ArrayList<>();
			this.refs = new ArrayList<>();
		}
		
		/**
		 * Gets the name of the ant target. The name is used by ant at the
		 * command line to perform the tasks in the target.
		 * 
		 * @return the target name.
		 * @since 1.0
		 */
		public String getName() {
			return name;
		}

		/**
		 * Gets the name of the target which this target depends on.
		 * 
		 * @return the dependent target.
		 * @since 1.0
		 */
		public String getDepends() {
			return depends;
		}

		/**
		 * Gets the direct path elements which make up part of the classpath
		 * for the target.
		 * 
		 * @return the paths.
		 * @since 1.0
		 */
		public List<String> getPaths() {
			return paths;
		}

		/**
		 * Gets the list of path IDs which make up part of the classpath for
		 * the target.
		 * 
		 * @return the reference IDs.
		 * @since 1.0
		 */
		public List<String> getReferences() {
			return refs;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[name=" + name + ", depends="
					+ depends + ", paths=" + paths + ", refs=" + refs + "]";
		}
	}
	
	/**
	 * The {@code AntPath} class represents a path in the ant build file. It is
	 * comprised of a unique ID, path elements, and references to other paths.
	 * 
	 * @author Devon McGrath
	 * @see AntTarget
	 * @since 1.0
	 */
	public static class AntPath {
		
		/** The unique ID of the path. */
		private String id;
		
		/** The path elements which are part of this path. */
		private List<String> paths;
		
		/** The references to any other paths that are part of this one. */
		private List<String> refs;
		
		/**
		 * Constructs an empty path.
		 * @since 1.0
		 */
		public AntPath() {
			this.paths = new ArrayList<>();
			this.refs = new ArrayList<>();
		}
		
		/**
		 * Gets the unique ID which identifies this path.
		 * 
		 * @return the path ID.
		 * @since 1.0
		 */
		public String getId() {
			return id;
		}

		/**
		 * Gets the path elements which make up this path.
		 * 
		 * @return the path elements in this path.
		 * @since 1.0
		 */
		public List<String> getPaths() {
			return paths;
		}

		/**
		 * Gets the list of reference IDs to any other paths which are part of
		 * this path.
		 * 
		 * @return the path references.
		 * @since 1.0
		 */
		public List<String> getReferences() {
			return refs;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[id=" + id + ", paths=" +
					paths + ", refs=" + refs + "]";
		}
	}
}
