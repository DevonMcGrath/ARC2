package ca.sqrlab.arc.tools.mutation;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TXLMutation {
	
	public static final String ARG_OUT_FILE = "-outfile";
	
	public static final String ARG_OUT_DIR = "-outdir";
	
	public static final String ARG_CLASS = "-class";
	
	public static final String ARG_METHOD = "-method";
	
	public static final String ARG_VAR = "-var";
	
	public static final String ARG_SYNC_VAR = "-syncvar";

	public static final TXLMutation MUTATION_ASAT =
			new TXLMutation("ASAT_RND.Txl", "ASAT", true, true, ARG_SYNC_VAR);
	
	public static final TXLMutation MUTATION_ASIM =
			new TXLMutation("ASIM_RND.Txl", "ASIM", true, true);
	
	public static final TXLMutation MUTATION_ASM =
			new TXLMutation("ASM_V.Txl", "ASM", true, true, ARG_SYNC_VAR);
	
	public static final TXLMutation MUTATION_CSO =
			new TXLMutation("CSO.Txl", "CSO", false, true);
	
	public static final TXLMutation MUTATION_EXSB =
			new TXLMutation("EXSB.Txl", "EXSB", true, true);
	
	public static final TXLMutation MUTATION_EXSA =
			new TXLMutation("EXSA.Txl", "EXSA", true, true);
	
	public static final TXLMutation MUTATION_RSAS =
			new TXLMutation("RSAS.Txl", "RSAS", false, true);
	
	public static final TXLMutation MUTATION_RSAV =
			new TXLMutation("RSAV.Txl", "RSAV", false, true);
	
	public static final TXLMutation MUTATION_RSIM =
			new TXLMutation("RSIM.Txl", "RSIM", false, true);
	
	public static final TXLMutation MUTATION_RSM =
			new TXLMutation("RSM.Txl", "RSM", false, true);
	
	public static final TXLMutation MUTATION_SHSA =
			new TXLMutation("SHSA.Txl", "SHSA", false, true);
	
	public static final TXLMutation MUTATION_SHSB =
			new TXLMutation("SHSB.Txl", "SHSB", false, true);
	
	private String mutationFile;
	
	private String mutationName;
	
	private boolean fixesDataraces;
	
	private boolean fixesDeadlocks;
	
	private String[] arguments;
	
	public TXLMutation(String mutationFile, String mutationName,
			boolean fixesDataraces, boolean fixesDeadlocks, String... args) {
		this.mutationFile = mutationFile;
		this.mutationName = mutationName;
		this.fixesDataraces = fixesDataraces;
		this.fixesDeadlocks = fixesDeadlocks;
		if (args == null) {
			this.arguments = new String[0];
		} else {
			this.arguments = args;
		}
	}

	public boolean isTXLFile() {
		return mutationFile != null && mutationFile.toLowerCase().endsWith(".txl");
	}
	
	public boolean exists(String operatorDir) {
		
		// Make sure it is a TXL file
		if (!isTXLFile()) {
			return false;
		}
		
		// Make sure the argument is actually a directory
		if (operatorDir == null || operatorDir.isEmpty()) {
			return false;
		}
		File dir = new File(operatorDir);
		if (!dir.exists() || !dir.isDirectory()) {
			return false;
		}
		
		// Check the files in the directory
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isFile() && f.getName().equals(mutationFile)) {
				return true;
			}
		}
		
		return false;
	}
	
	public String getMutationFile() {
		return mutationFile;
	}

	public String getMutationName() {
		return mutationName;
	}
	
	public String[] getArguments() {
		return arguments;
	}

	public boolean fixesDataraces() {
		return fixesDataraces;
	}

	public boolean fixesDeadlocks() {
		return fixesDeadlocks;
	}
	
	/**
	 * Gets the command to execute this TXL mutation operator.
	 * 
	 * @param txl			the command to run the TXL process.
	 * @param txlDir		the directory path of this TXL mutation operator.
	 * @param sourceFile	the path to the source file.
	 * @param outFile		the name of the output file.
	 * @param outDir		the directory path where the result is placed.
	 * @param args			any additional arguments.
	 * @return the command to execute.
	 * 
	 * @see #getArguments()
	 * @since 1.0
	 */
	public String getCommand(String txl, String txlDir, String sourceFile,
			String outFile, String outDir, String... args) {
		
		// No source/out file
		if (sourceFile == null || sourceFile.isEmpty() ||
				!(new File(sourceFile)).isFile()) {
			return null;
		} if (outFile == null || outFile.isEmpty()) {
			return null;
		}
		
		// No output directory
		if (outDir == null || outDir.isEmpty()) {
			return null;
		}
		
		// If no TXL directory, use CWD
		if (txlDir == null || txlDir.isEmpty()) {
			txlDir = ".";
		}
		
		// Make sure the TXL file exists
		File dir = new File(txlDir);
		if (!dir.isDirectory()) {
			return null;
		}
		File[] files = dir.listFiles();
		File f = null;
		for (File file : files) {
			if (file.isFile() && file.getName().equals(mutationFile)) {
				f = file;
				break;
			}
		}
		if (f == null) {
			return null;
		}
		
		// If TXL isn't defined, use the default
		if (txl == null || txl.isEmpty()) {
			txl = "txl";
		}
		
		// Correct any paths
		String txlFile = f.getAbsolutePath();
		if (txlFile.indexOf(" ") >= 0) {
			txlFile = '"' + txlFile + '"';
		} if (sourceFile.indexOf(" ") >= 0) {
			sourceFile = '"' + sourceFile + '"';
		} if (outDir.indexOf(" ") >= 0) {
			outDir = '"' + outDir + '"';
		} if (outFile.indexOf(" ") >= 0) {
			outFile = '"' + outFile + '"';
		}
		
		// Build the command
		String cmd = txl + " " + sourceFile + " " + txlFile + " - " + ARG_OUT_FILE
				+ " " + outFile + " " + ARG_OUT_DIR + " " + outDir;
		
		// Add any arguments
		if (args == null) {
			args = new String[0];
		}
		int n = Math.min(args.length, arguments.length);
		for (int i = 0; i < n; i ++) {
			String v = args[i];
			if (v.indexOf(" ") >= 0) {
				v = '"' + v + '"';
			}
			cmd += " " + arguments[i] + " " + v;
		}
		
		return cmd;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[mutationName='" + mutationName
			+ "', mutationFile='" + mutationFile + "', fixesDataraces="
			+ fixesDataraces + ", fixesDeadlocks=" + fixesDeadlocks + "]";
	}
	
	/**
	 * Gets an array of all the mutation operator fields which are declared in
	 * this class.
	 * 
	 * @return all the mutation operators in this class or an empty array if
	 * there are none.
	 * 
	 * @since 1.0
	 */
	public static TXLMutation[] getAllMutations() {
		
		// Use reflection to get all the TXL mutations declared
		Field[] fields = TXLMutation.class.getDeclaredFields();
		int n = fields.length;
		List<TXLMutation> mutations = new ArrayList<>();
		for (int i = 0; i < n; i ++) {
			Field f = fields[i];
			try {
				Object v = f.get(null);
				if (v != null && v instanceof TXLMutation) {
					mutations.add((TXLMutation) v);
				}
			} catch (Exception e) {}
		}
		
		// Convert the list to an array
		n = mutations.size();
		TXLMutation[] result = new TXLMutation[n];
		for (int i = 0; i < n; i ++) {
			result[i] = mutations.get(i);
		}
		
		return result;
	}
	
	/**
	 * Gets a random mutation from the mutation operators declared in this
	 * class.
	 * 
	 * @return a random mutation operator, or null if no mutation operators
	 * exist.
	 * 
	 * @see #getRandomDataraceMutation()
	 * @see #getRandomDeadlockMutation()
	 * @see #getAllMutations()
	 * @since 1.0
	 */
	public static TXLMutation getRandomMutation() {
		
		// No mutations
		TXLMutation[] all = TXLMutation.getAllMutations();
		if (all == null || all.length == 0) {
			return null;
		}
		
		return all[(int) (Math.random() * all.length)];
	}

	/**
	 * Gets a random mutation operator which can be used to fix deadlocks.
	 * 
	 * @return a random operator to fix deadlocks, or null if none exist.
	 * 
	 * @see #getRandomDataraceMutation()
	 * @see #getRandomMutation()
	 * @since 1.0
	 */
	public static TXLMutation getRandomDeadlockMutation() {
		
		// No mutations
		TXLMutation[] all = TXLMutation.getAllMutations();
		if (all == null || all.length == 0) {
			return null;
		}
		
		// Count the number of operators which fix deadlocks
		int count = 0;
		for (TXLMutation m : all) {
			if (m.fixesDeadlocks) {
				count ++;
			}
		}
		if (count == 0) {
			return null;
		}
		
		// Get a random one
		int opn = (int) (Math.random() * count);
		for (TXLMutation m : all) {
			if (m.fixesDeadlocks) {
				if (opn <= 0) {
					return m;
				}
				opn --;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets a random mutation operator which can be used to fix data races.
	 * 
	 * @return a random operator to fix data races, or null if none exist.
	 * 
	 * @see #getRandomDeadlockMutation()
	 * @see #getRandomMutation()
	 * @since 1.0
	 */
	public static TXLMutation getRandomDataraceMutation() {
		
		// No mutations
		TXLMutation[] all = TXLMutation.getAllMutations();
		if (all == null || all.length == 0) {
			return null;
		}
		
		// Count the number of operators which fix data races
		int count = 0;
		for (TXLMutation m : all) {
			if (m.fixesDataraces) {
				count ++;
			}
		}
		if (count == 0) {
			return null;
		}
		
		// Get a random one
		int opn = (int) (Math.random() * count);
		for (TXLMutation m : all) {
			if (m.fixesDataraces) {
				if (opn <= 0) {
					return m;
				}
				opn --;
			}
		}
		
		return null;
	}
}
