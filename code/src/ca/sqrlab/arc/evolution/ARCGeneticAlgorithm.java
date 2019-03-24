package ca.sqrlab.arc.evolution;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.ARCRunner;
import ca.sqrlab.arc.FinishListener;
import ca.sqrlab.arc.io.FileUtils;
import ca.sqrlab.arc.tools.ARCUtils;
import ca.sqrlab.arc.tools.compilation.AntCompiler;
import ca.sqrlab.arc.tools.compilation.ProjectCompiler;
import ca.sqrlab.arc.tools.monitoring.Logger;
import ca.sqrlab.arc.tools.monitoring.Phase;
import ca.sqrlab.arc.tools.mutation.TXLMutation;
import ca.sqrlab.arc.tools.testing.TestResult;
import ca.sqrlab.arc.tools.testing.TestStatus;
import ca.sqrlab.arc.tools.testing.TestingSummary;

/**
 * The {@code ARCGeneticAlgorithm} is responsible for evolving the target
 * project until either a solution is found or the maximum number of
 * generations has been run.
 * 
 * <p>This class should be instantiated using the {@link ARCRunner} class.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public class ARCGeneticAlgorithm {
	
	/** The ID which is used for the {@link #getOnFinish()} listener to
	 * indicate an event has occurred in the genetic algorithm. */
	public static final int GENETIC_ALGORITHM_ID = 314159;
	
	/** The minimum number of test-suite executions to run during extended
	 * validation. */
	public static final int MIN_VALIDATION_TESTS = 150;
	
	/** The default number of individuals for each generation. */
	public static final int DEFAULT_POPULATION_COUNT = 30;
	
	/** The default number of generations to run before it is assumed that no
	 * solution can be found. */
	public static final int DEFAULT_MAX_GENERATIONS = 30;
	
	/** The default number of times to test each individual. */
	public static final int DEFAULT_RUN_COUNT = 15;
	
	/** The ARC runner executing this genetic algorithm. */
	private ARCRunner ar;
	
	/** The current instance of ARC. */
	private ARC arc;
	
	/** The listener which receives events when the genetic algorithm finishes
	 * a phase. */
	private FinishListener onFinish;

	/** All the generations from the last run of the genetic algorithm. */
	private List<Generation> generations;
	
	/** The size of the population for each generation. */
	private int individualCount;
	
	/** The number of generations to run before the genetic algorithm decides
	 * that no solution can be found. */
	private int maxGenerations;
	
	/** The number of program executions to use for each individual. */
	private int runs;
	
	/** The flag indicating if a valid fix was found for the project. */
	private boolean foundFix;
	
	/** The individual which was found to fully pass all the tests in the
	 * test-suite, even after further evaluation. */
	private Individual solution;
	
	/** The list of mutant programs which have already been generated. */
	private List<Mutant> mutants;
	
	/**
	 * Creates an ARC genetic algorithm using the ARC runner executing the GA.
	 * 
	 * @param ar	the ARC runner executing this.
	 * @since 1.0
	 */
	public ARCGeneticAlgorithm(ARCRunner ar) {
		setARCRunner(ar);
	}
	
	/**
	 * Creates an ARC genetic algorithm using the ARC runner executing the GA
	 * and a listener.
	 * 
	 * @param ar		the ARC runner executing this.
	 * @param onFinish	the listener which will receive events at the end of
	 * 					a phase.
	 * 
	 * @see #getOnFinish()
	 * @since 1.0
	 */
	public ARCGeneticAlgorithm(ARCRunner ar, FinishListener onFinish) {
		setARCRunner(ar);
		this.onFinish = onFinish;
	}
	
	/**
	 * Runs the genetic algorithm and attempts to find a fix for the original
	 * program.
	 * 
	 * @param l	the logger to keep track of events.
	 * @return the logger which has all the messages from the genetic algorithm.
	 * 
	 * @see #foundFix()
	 * @since 1.0
	 */
	public Logger run(Logger l) {
		
		// Initialize the logger if necessary
		if (l == null) {
			l = new Logger();
		}
		l.newPhase("GA Initialization");
		
		this.generations = new ArrayList<>();
		this.foundFix = false;
		this.solution = null;
		this.mutants = new ArrayList<>();
		
		// Make sure that ARC and a project exists
		if (arc == null) {
			l.fatalError("There is no reference to ARC.");
			finishPhase(l);
			return l;
		} if (arc.getProject() == null) {
			l.fatalError("There is no project.");
			finishPhase(l);
			return l;
		}
		
		// Get GA parameters
		String ics = arc.getSetting(ARC.SETTING_POPULATION_COUNT);
		String mgs = arc.getSetting(ARC.SETTING_MAX_GENERATIONS);
		String rs = arc.getSetting(ARC.SETTING_RUN_COUNT);

		// Parse the population size
		if (ics == null || ics.isEmpty()) {
			l.warning("Missing config value for '" +
					ARC.SETTING_POPULATION_COUNT + "' - using default.");
			this.individualCount = DEFAULT_POPULATION_COUNT;
		} else {
			try {
				this.individualCount = Integer.parseInt(ics);
			} catch (NumberFormatException e) {
				l.warning("Invalid config value for '" +
						ARC.SETTING_POPULATION_COUNT +
						"' (expecting int) - using default.");
				this.individualCount = DEFAULT_POPULATION_COUNT;
			}
		}
		if (individualCount <= 0) {
			l.fatalError("The value specified for '" +
					ARC.SETTING_POPULATION_COUNT + "' is less than 1.");
			finishPhase(l);
			return l;
		}
		
		// Parse the max generations
		if (mgs == null || mgs.isEmpty()) {
			l.warning("Missing config value for '" +
					ARC.SETTING_MAX_GENERATIONS + "' - using default.");
			this.maxGenerations = DEFAULT_MAX_GENERATIONS;
		} else {
			try {
				this.maxGenerations = Integer.parseInt(mgs);
			} catch (NumberFormatException e) {
				l.warning("Invalid config value for '" +
						ARC.SETTING_MAX_GENERATIONS +
						"' (expecting int) - using default.");
				this.maxGenerations = DEFAULT_MAX_GENERATIONS;
			}
		}
		if (maxGenerations <= 0) {
			l.fatalError("The value specified for '" +
					ARC.SETTING_POPULATION_COUNT + "' is less than 1.");
			finishPhase(l);
			return l;
		}
		
		// Parse the number of runs
		if (rs == null || rs.isEmpty()) {
			l.warning("Missing config value for '" +
					ARC.SETTING_RUN_COUNT + "' - using default.");
			this.runs = DEFAULT_RUN_COUNT;
		} else {
			try {
				this.runs = Integer.parseInt(rs);
			} catch (NumberFormatException e) {
				l.warning("Invalid config value for '" +
						ARC.SETTING_RUN_COUNT +
						"' (expecting int) - using default.");
				this.runs = DEFAULT_RUN_COUNT;
			}
		}
		if (runs <= 0) {
			l.fatalError("The value specified for '" +
					ARC.SETTING_RUN_COUNT + "' is less than 1.");
			finishPhase(l);
			return l;
		}
		
		// Log some parameters
		l.debug("# of test-suite executions per individual: " + runs);
		l.debug("# of individuals per generation: " + individualCount);
		l.debug("Max generation before termination: " + maxGenerations);
		
		// Make all the directories for generation 0
		String dir00 = getIndividualDirectory(0, 0);
		if (!(new File(dir00).mkdirs())) {
			l.fatalError("Unable to create the directories for the first "
					+ "generation.");
			finishPhase(l);
			return l;
		}
		
		// Copy the original project
		if (!ARCUtils.copyProjectSourceFiles(
				arc, arc.getSetting(ARC.SETTING_PROJECT_DIR), dir00, l)) {
			l.fatalError("Unable to copy project to individual's directory.");
			finishPhase(l);
			return l;
		}
		
		// Test the original project
		Individual original = new Individual(0, 0, dir00);
		original.test(arc, runs);
		Generation g0 = new Generation(original);
		this.generations.add(g0);
		
		// Make sure that the program is actually buggy
		if (isFinalSolution(original, l)) {
			l.debug("The original project was found to have no bugs.");
			this.foundFix = true;
			this.solution = original;
			finishPhase(l);
			return l;
		}
		
		// Check if requested to stop
		if (ar != null && ar.shouldStop()) {
			l.fatalError("ARC was requested to stop.");
			finishPhase(l);
			return l;
		}
		
		// Add the first mutant
		List<File> lfiles = FileUtils.find(dir00, ".+\\.java", true);
		int n = lfiles.size();
		String[] files = new String[n];
		for (int i = 0; i < n; i ++) {
			files[i] = lfiles.get(i).getAbsolutePath();
		}
		Mutant m00 = new Mutant(files);
		this.mutants.add(m00);
		original.setRepresentation(m00);
		
		finishPhase(l);
		
		// Continuously evolve until a solution is found
		for (int i = 1; i <= maxGenerations; i ++) {
			l.newPhase("Generation " + i);
			
			// Evolve the population
			if (evolve(l)) {
				l.debug("Solution found.");
				this.foundFix = true;
				break;
			}
			
			// Check if there was a fatal error
			if (l.hasFatalError()) {
				finishPhase(l);
				return l;
			}
			
			// Check if requested to stop
			if (ar != null && ar.shouldStop()) {
				l.fatalError("ARC was requested to stop.");
				finishPhase(l);
				return l;
			}
			
			finishPhase(l);
		}
		
		// If no fix was found, tell the user
		if (!foundFix) {
			l.fatalError("No fix was found after " + maxGenerations +
					" generations.");
			finishPhase(l);
			return l;
		}
		
		return l;
	}
	
	/**
	 * Runs a generation in the genetic algorithm. First, it generates mutant
	 * copies of the best programs found so far. Next, it randomly selects a
	 * mutated program for each individual and evaluates the program.
	 * 
	 * @param l	the logger to keep track of any issues/events.
	 * @return true if and only if a valid program solution was found which
	 * passes all the tests (and is evaluated further).
	 * 
	 * @since 1.0
	 */
	private boolean evolve(Logger l) {
		
		int gen = generations.size();
		
		// Create the directories
		if (!makeGenerationDirs(gen)) {
			l.fatalError("Unable to create directories for the generation.");
			return false;
		}
		
		// Create the generation
		Individual[] population = new Individual[individualCount];
		for (int i = 0; i < individualCount; i ++) {
			population[i] = new Individual(i, gen, getIndividualDirectory(gen, i));
		}
		Generation g = new Generation(population);
		
		// Create the mutants
		l.debug("Generating mutants for the generation...");
		generateMutants(g, l);
		if (l.hasFatalError()) {
			return false;
		}
		this.generations.add(g);
		l.debug("Done.");
		finishPhase(l);
		
		// Check if requested to stop
		if (ar != null && ar.shouldStop()) {
			l.fatalError("ARC was requested to stop.");
			finishPhase(l);
			return false;
		}
		
		// Test the population
		int failed = 0;
		List<Individual> actualPop = g.getPopulation();
		for (Individual individual : actualPop) {
			
			l.debug("Generation " + individual.getGeneration() +
					", individual " + individual.getId());
			
			// Test the individual
			if (!individual.test(arc, runs)) {
				l.warning("Unable to run tests for individual " + individual);
				failed ++;
			}
			l.debug("Score: " + individual.getScore());
			
			// Check if it is the solution
			if (isFinalSolution(individual, l)) {
				this.solution = individual;
				return true;
			}
			
			// Check if requested to stop
			if (ar != null && ar.shouldStop()) {
				l.fatalError("ARC was requested to stop.");
				finishPhase(l);
				return false;
			}
			
			finishPhase(l);
		}
		if (failed == population.length) {
			l.fatalError("Unable to test any individual from the current generation.");
			return false;
		}
		
		// Log the best individual
		Individual best = g.getBestIndividual();
		if (best != null) {
			l.debug("Best individual in generation " + gen + " (with score " +
					best.getScore() + "): " + best);
		}
		
		return false;
	}
	
	/**
	 * Generates mutants for the best individuals found so far (or the base
	 * project). If for a given individual, the same mutation operator is
	 * selected, then no more mutations are added for that individual. Only one
	 * mutation operator per candidate (from {@link #getMutationCandidates()})
	 * will be applied to the program to avoid creating thousands of files.
	 * 
	 * @param g	the generation to generate mutants for.
	 * @param l	the logger to track the mutation process.
	 * 
	 * @since 1.0
	 */
	private void generateMutants(Generation g, Logger l) {
		
		// No generation
		if (g == null) {
			return;
		}
		
		// Determine all the possible individuals which can be mutated
		List<Individual> candidates = getMutationCandidates();
		int n = candidates.size();
		
		// Generate all the mutants for each candidate
		TXLMutation[] allMutations = TXLMutation.getAllMutations();
		for (Individual candidate : candidates) {
			for (TXLMutation m : allMutations) {
				mutateIndividual(candidate, m, l);
				if (l.hasFatalError()) {
					return;
				}
			}
		}
		
		// Check if requested to stop
		if (ar != null && ar.shouldStop()) {
			l.fatalError("ARC was requested to stop.");
			finishPhase(l);
			return;
		}
		
		// Create the population
		List<Individual> population = g.getPopulation();
		int popSize = population == null? 0 : population.size();
		for (int i = 0; i < popSize; i ++) {
			
			Individual individual = population.get(i);
			int startIdx = (i / 3) % n;
			
			// Check if any mutants were generated
			boolean foundMutant = createMutantProgram(candidates,
					individual, startIdx, l);
			
			// No valid mutations, but we created at least one individual
			if (!foundMutant && i > 0) {
				l.warning("Not enough mutations to create the entire "
						+ "population size (" + popSize + " mutants).");
				
				// Delete the extra individuals
				for (int j = i; j < popSize; j ++) {
					Individual toRemove = population.remove(i);
					FileUtils.remove(toRemove.getPath());
				}
				
				// Check if requested to stop
				if (ar != null && ar.shouldStop()) {
					l.fatalError("ARC was requested to stop.");
					finishPhase(l);
					return;
				}
				
				break;
			}
			
			// No valid mutant programs
			else if (!foundMutant) {
				l.fatalError("Unable to find valid mutant for individual " +
						individual.getId() + " from generation " +
						individual.getGeneration() + ".");
				return;
			}
			
			// Check if requested to stop
			if (ar != null && ar.shouldStop()) {
				l.fatalError("ARC was requested to stop.");
				finishPhase(l);
				return;
			}
		}
	}
	
	/**
	 * Creates a mutant of the specified individual using the specified
	 * mutation operator. If this individual was already mutated using the
	 * specified operator, this method does nothing.
	 * 
	 * @param individual	the individual to mutate.
	 * @param mutation		the mutation to apply to the individual.
	 * @param l				the logger to keep track of the mutation process.
	 * @return a string to the root directory where all the mutants are located.
	 * 
	 * @since 1.0
	 */
	private String mutateIndividual(Individual individual, TXLMutation mutation,
			Logger l) {
		
		// Arguments check
		if (individual == null || mutation == null) {
			return null;
		} if (l == null) {
			l = new Logger();
		}
		
		// Make sure there are actually files to mutate
		String[] javaFiles = arc.getProject().getSourceFiles();
		if (javaFiles == null || javaFiles.length == 0) {
			l.fatalError("No Java files to mutate in the project!");
			return null;
		}
		
		// Make sure the individual's directory actually exists
		String individualRoot = individual.getPath();
		if (individualRoot == null || !(new File(individualRoot)).isDirectory()) {
			l.fatalError("The individual's specified directory does not exist: '"
					+ individualRoot + "'.");
			return null;
		}
		
		// Get the TXL arguments
		String txlProg = arc.getSetting(ARC.SETTING_TXL);
		String operatorDir = arc.getSetting(ARC.SETTING_TXL_DIR);
		
		// Build the directory path for the mutant
		// i.e.: <mutant_dir>/<generation>/<id>/<operator>
		String ds = arc.getSetting(ARC.SETTING_DIR_SEPARATOR);
		String root = arc.getSetting(ARC.SETTING_MUTANT_DIR) + ds +
				individual.getGeneration() + ds + individual.getId() +
				ds + mutation.getMutationFile() + ds;
		
		// Check if this type of mutation has been run on this individual
		File dir = new File(root);
		if (dir.exists()) {
			return root;
		}
		
		// Create the root directory
		if (!dir.mkdirs()) {
			l.fatalError("Unable to create directory for mutation (path: '"
					+ root + "').");
			return null;
		}
		
		// Mutate all the files using the mutation operator
		if (!individualRoot.endsWith(ds)) {
			individualRoot += ds;
		}
		for (String jf : javaFiles) {
			
			// Get the argument's values for the mutation of the specific file
			String[] args = mutation.getArguments();
			int n = (args == null)? 0 : args.length;
			String[] argValues = new String[n];
			if (args != null) {
				for (int i = 0; i < n; i ++) {
					String arg = args[i];
					if (arg == null || arg.isEmpty()) {
						argValues[i] = "";
						continue;
					}
					
					// TODO: use static analysis to get the class, method, variable
					// information
					if (arg.equals(TXLMutation.ARG_SYNC_VAR)) {
						argValues[i] = "this";
					}
				}
			}
			
			// Get the absolute path to the source file
			String fullSrcPath = individualRoot + jf;
			File src = new File(fullSrcPath);
			if (!src.isFile()) {
				l.warning("Unable to find source file: '" + fullSrcPath + "'.");
				continue;
			}
			
			// Create the parent directory, if required
			String fullDstPath = root + jf;
			File dstDir = (new File(fullDstPath)).getParentFile();
			if (!dstDir.isDirectory() && !dstDir.mkdirs()) {
				l.fatalError("Failed to make mutant output directory: '" +
						dstDir.getAbsolutePath() + "'.");
				return null;
			}
			
			// Run TXL with the mutation operator
			String cmd = mutation.getCommand(txlProg, operatorDir, fullSrcPath,
					src.getName(), dstDir.getAbsolutePath(), argValues);
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (Exception e) {
				e.printStackTrace();
				l.fatalError("Failed to mutate '" + jf +
						"' using the TXL mutation operator: " + mutation + "");
				l.fatalError(e.getLocalizedMessage());
				return null;
			}
		}
		
		return root;
	}
	
	/**
	 * Creates a mutant program for the specified individual based on the
	 * mutated source files found in the mutant directory. If a mutant program
	 * is found, the individual's mutation will be updated.
	 * 
	 * @param candidates			all the candidate individuals.
	 * @param individual			the individual to create.
	 * @param startIdx				the first candidate index to attempt to
	 * 								create a program with.
	 * @param l						the logger to track events.
	 * @return true if and only if a mutant program was found which can be
	 * compiled.
	 * 
	 * @since 1.0
	 */
	private boolean createMutantProgram(List<Individual> candidates,
			Individual individual, int startIdx, Logger l) {
		
		// Check arguments
		if (candidates == null || individual == null || candidates.isEmpty() ||
				startIdx < 0 || startIdx >= candidates.size()) {
			return false;
		} if (l == null) {
			l = new Logger();
		}
		
		// Get the project files
		String[] javaFiles = arc.getProject().getSourceFiles();
		if (javaFiles == null || javaFiles.length == 0) {
			return false;
		}
		
		// Get settings used over and over again
		String ds = arc.getSetting(ARC.SETTING_DIR_SEPARATOR);
		String mdirs = arc.getSetting(ARC.SETTING_MUTANT_DIR);
		String projectDir = arc.getSetting(ARC.SETTING_PROJECT_DIR);
		if (!projectDir.endsWith(ds)) {
			projectDir += ds;
		}
		TXLMutation[] allMutations = TXLMutation.getAllMutations();
		String individualPath = individual.getPath();
		
		int i = startIdx, n = candidates.size();
		do {
			Individual source = candidates.get(i);
			String srcPath = source.getPath();
			
			// Determine which operators to bias towards
			TestingSummary summary = source.getTestSummary();
			List<TestResult> dataraces = summary.getResultsFor(TestStatus.DATA_RACE);
			List<TestResult> deadlocks = summary.getResultsFor(TestStatus.DEADLOCK);
			int dataraceCount = (dataraces == null)? 0 : dataraces.size();
			int deadlockCount = (deadlocks == null)? 0 : deadlocks.size();
			int total = dataraceCount + deadlockCount;
			double dataraceChance = total == 0? 0.5 : ((double) dataraceCount) / total;
			boolean useDataraceMutation = (Math.random() <= dataraceChance);
			
			// Check if any mutants were generated
			String mutantDir = mdirs + ds + source.getGeneration()
				+ ds + source.getId();
			List<File> files = FileUtils.find(mutantDir, ".*\\.java.*", true);
			if (files.isEmpty()) {
				i = (i + startIdx) % n;
				continue;
			}
			
			// Keep trying to create a program until successful or no files
			while (!files.isEmpty()) {
				
				File mutant = files.remove((int) (Math.random() * files.size()));
				
				// Determine which mutation was used
				File mdir = new File(mutantDir);
				File f = mutant;
				TXLMutation m = null;
				while (!f.getParentFile().equals(mdir)) {
					f = f.getParentFile();
					if (f == null) {
						break;
					}
				}
				if (f != null) {
					String dname = f.getName();
					for (TXLMutation mutation : allMutations) {
						if (dname.equals(mutation.getMutationFile())) {
							m = mutation;
							break;
						}
					}
				}
				
				// Check if the mutant is the correct one
				if (m == null || (useDataraceMutation && !m.fixesDataraces()) ||
						(!useDataraceMutation && !m.fixesDeadlocks())) {
					continue;
				}
				
				// Determine if a mutant has already been seen before
				String apath = mutant.getAbsolutePath();
				Mutant newRep = source.mutate(apath, javaFiles);
				if (mutants.indexOf(newRep) >= 0) {
					continue;
				}
				
				// Reconstruct the new individual
				if (!ARCUtils.copyProjectSourceFiles(arc,
						srcPath, projectDir, null)) {
					continue;
				}
				
				// Determine which source file it is
				String relPath = null, mname = mutant.getName();
				for (String jf : javaFiles) {
					if (jf == null || jf.isEmpty()) {
						continue;
					}
					String name = (new File(jf)).getName();
					if (mname.contains(name)) {
						relPath = jf;
						break;
					}
				}
				if (relPath == null) {
					continue;
				}
				
				// Copy the mutated file and compile it
				FileUtils.copy(mutant.getAbsolutePath(), projectDir + relPath,
						false);
				ProjectCompiler compiler = new AntCompiler(projectDir,
						arc.getSetting(ARC.SETTING_PROJECT_COMPILE_CMD),
						arc.getSetting(ARC.SETTING_ANT));
				Logger compileLog = compiler.compile();
				if (compileLog.hasFatalError()) {
					try {
						mutant.delete();
					} catch (Exception e) {}
				}
				
				// Compiled successfully
				else {
					
					// Copy over the valid program
					if (!ARCUtils.copyProjectSourceFiles(
							arc, projectDir, individualPath, null)) {
						continue; // failed to copy
					}
					
					individual.setRepresentation(newRep);
					individual.setSource(source);
					this.mutants.add(newRep);

					return true;
				}
			}
			
			i = (i + startIdx) % n;
		} while (i != startIdx);
		
		return false;
	}
	
	/**
	 * Checks to see if an individual is the final solution (i.e. passes all
	 * tests and passes additional test-suite executions). The individual will
	 * only be tested further if all the initial tests were
	 * {@link TestStatus#SUCCESS}.
	 * 
	 * @param individual	the individual to check.
	 * @param l				the logger to keep track of events.
	 * @return true if and only if ALL tests pass, including the additional
	 * verification ones.
	 * 
	 * @since 1.0
	 */
	private boolean isFinalSolution(Individual individual, Logger l) {
		
		// No individual
		if (individual == null) {
			return false;
		}
		
		// Check the test summary
		TestingSummary summary = individual.getTestSummary();
		if (summary == null) {
			return false;
		}
		int n = summary.getNumberOfTestsRun();
		if (n == 0) {
			return false;
		}
		List<TestResult> successes = summary.getResultsFor(TestStatus.SUCCESS);
		if (successes == null || successes.size() != n) { // there were failures
			return false;
		}
		
		if (l == null) {
			l = new Logger();
		}
		
		// Run more extensive tests on the individual
		final int TEST_COUNT = MIN_VALIDATION_TESTS;
		l.debug("Evaluating potential solution: " + individual + " against " +
				TEST_COUNT + " test-suite executions.");
		individual.test(arc, TEST_COUNT);
		
		// Check the results
		summary = individual.getTestSummary();
		if (summary == null) {
			l.debug("Failed to re-test.");
			return false;
		}
		n = summary.getNumberOfTestsRun();
		if (n != TEST_COUNT) {
			l.debug("Failed to re-test fully. Only ran " + n + " tests.");
			return false;
		}
		successes = summary.getResultsFor(TestStatus.SUCCESS);
		l.debug("Successes: " + (successes == null? 0 : successes.size()) + "/" + n);
		if (successes == null || successes.size() != n) { // there were failures
			return false;
		}
		
		return true;
	}
	
	/**
	 * Gets all the individuals which performed better than the original
	 * program. If no individuals meet that criteria, the original program is
	 * returned.
	 * 
	 * @return the list of mutation candidates from best to worst.
	 * 
	 * @since 1.0
	 */
	private List<Individual> getMutationCandidates() {
		
		Individual original = generations.get(0).getPopulation().get(0);
		int n = generations.size();
		
		// Determine all the possible individuals which can be mutated
		List<Individual> candidates = new ArrayList<>();
		for (int i = n - 1; i >= 1; i --) {
			candidates.addAll(generations.get(i).getBetter(original));
		}
		if (candidates.isEmpty()) { // no candidates found
			
			// Add the original so we can ensure all the distance 1 mutants are
			// explored
			candidates.add(original);
			
			// Try to find ones which perform the same as the original
			for (int i = n - 1; i >= 1; i --) {
				candidates.addAll(generations.get(i).getBetterOrSame(original));
			}
		}
		
		n = candidates.size();
		if (n == 1) {
			return candidates;
		}
		
		// Sort the candidates from best to worst
		for (int i = 0; i < n - 1; i ++) {
			int bi = i;
			float bs = candidates.get(i).getScore();
			for (int j = i + 1; j < n; j ++) {
				float ts = candidates.get(j).getScore();
				if (ts > bs) {
					bs = ts;
					bi = j;
				}
			}
			
			if (bi != i) {
				Individual tmp = candidates.get(i);
				candidates.set(i, candidates.get(bi));
				candidates.set(bi, tmp);
			}
		}
		
		return candidates;
	}
	
	/**
	 * Notifies the finish listener that a phase of the genetic algorithm has
	 * completed.
	 * 
	 * @param l	the current logger for the GA.
	 * 
	 * @since 1.0
	 */
	private void finishPhase(Logger l) {
		if (onFinish != null) {
			List<Phase> phases = l.getPhases();
			this.onFinish.onFinish(GENETIC_ALGORITHM_ID,
					phases.get(phases.size() - 1));
		}
	}
	
	/**
	 * Makes all the directories for the specified generation and its
	 * individuals based on the population size.
	 * 
	 * @param generationNum	the number of the generation to create.
	 * @return true if and only if the directories were created successfully.
	 * 
	 * @since 1.0
	 */
	private boolean makeGenerationDirs(int generationNum) {
		
		// No directory to place the generation
		String gd = getGenerationDirectory(generationNum);
		if (gd == null) {
			return false;
		}
		
		// Create the generation directory
		if (!(new File(gd)).mkdirs()) {
			return false;
		}
		
		// Create all the directories for the individuals
		for (int i = 0; i < individualCount; i ++) {
			if (!(new File(getIndividualDirectory(generationNum, i))).mkdir()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Sets the current instance of the ARC runner which is executing this
	 * genetic algorithm.
	 * 
	 * @param ar	the ARC runner.
	 * @since 1.0
	 */
	public void setARCRunner(ARCRunner ar) {
		if (ar == null) {
			this.ar = null;
			this.arc = null;
		} else {
			this.ar = ar;
			this.arc = ar.getArc();
		}
	}
	
	/**
	 * Gets the listener which is called when a phase is finished in the
	 * genetic algorithm.
	 * 
	 * @return the on finish listener.
	 * 
	 * @see #GENETIC_ALGORITHM_ID
	 * @since 1.0
	 */
	public FinishListener getOnFinish() {
		return onFinish;
	}

	/**
	 * Sets the listener which receives events when a phase in the genetic
	 * algorithm is finished.
	 * 
	 * @param onFinish	the listener.
	 * 
	 * @see #GENETIC_ALGORITHM_ID
	 * @since 1.0
	 */
	public void setOnFinish(FinishListener onFinish) {
		this.onFinish = onFinish;
	}

	/**
	 * Gets the value of the flag which indicates if the genetic algorithm
	 * found a fix for the program.
	 * 
	 * @return true if and only if a solution was found.
	 * @see #getSolution()
	 * @since 1.0
	 */
	public boolean foundFix() {
		return foundFix;
	}
	
	/**
	 * Gets the individual which was found to pass all tests in the project's
	 * test-suite; including after further evaluation.
	 * 
	 * @return the individual representing the fixed project or null, if no
	 * solution was found during the last GA run.
	 * 
	 * @see #foundFix()
	 * @since 1.0
	 */
	public Individual getSolution() {
		return solution;
	}
	
	/**
	 * Gets the generations produced by this genetic algorithm.
	 * 
	 * @return the generations.
	 * @since 1.0
	 */
	public List<Generation> getGenerations() {
		return generations;
	}

	/**
	 * Gets the max number of individuals allowed in a single generation.
	 * 
	 * @return the individuals per generation.
	 * @since 1.0
	 */
	public int getIndividualCount() {
		return individualCount;
	}

	/**
	 * Gets the maximum number of generations allowed before ARC determines
	 * that a solution cannot be found.
	 * 
	 * @return the max number of generations.
	 * @since 1.0
	 */
	public int getMaxGenerations() {
		return maxGenerations;
	}

	/**
	 * Gets the number of test-suite executions which will be performed on
	 * any given individual.
	 * 
	 * @return the number of runs per individual.
	 * @since 1.0
	 */
	public int getRuns() {
		return runs;
	}

	/**
	 * Gets the directory for a generation.
	 * 
	 * @param generationNum	the generation number.
	 * @return the path to a directory for the specified generation.
	 * 
	 * @see #getIndividualDirectory(int, int)
	 * @since 1.0
	 */
	public String getGenerationDirectory(int generationNum) {
		return getGenerationDirectory(arc, generationNum);
	}
	
	/**
	 * Gets the directory for an individual in the specified generation.
	 * 
	 * @param generationNum	the generation number.
	 * @param individualNum	the number of the individual.
	 * @return the path to a directory for the specified individual.
	 * 
	 * @see #getGenerationDirectory(int)
	 * @since 1.0
	 */
	public String getIndividualDirectory(int generationNum, int individualNum) {
		return getIndividualDirectory(arc, generationNum, individualNum);
	}
	
	/**
	 * Gets the directory for a generation.
	 * 
	 * @param arc			the ARC to get the directory info from.
	 * @param generationNum	the generation number.
	 * @return the path to a directory for the specified generation.
	 * 
	 * @see #getIndividualDirectory(ARC, int, int)
	 * @since 1.0
	 */
	public static String getGenerationDirectory(ARC arc, int generationNum) {
		
		// No ARC
		if (arc == null) {
			return null;
		}
		
		String tmpDir = arc.getSetting(ARC.SETTING_TMP_DIR);
		String ds = arc.getSetting(ARC.SETTING_DIR_SEPARATOR);
		
		return tmpDir + ds + generationNum;
	}
	
	/**
	 * Gets the directory for an individual in the specified generation.
	 * 
	 * @param arc			the ARC to get the directory info from.
	 * @param generationNum	the generation number.
	 * @param individualNum	the number of the individual.
	 * @return the path to a directory for the specified individual.
	 * 
	 * @see #getGenerationDirectory(ARC, int)
	 * @since 1.0
	 */
	public static String getIndividualDirectory(ARC arc,
			int generationNum, int individualNum) {
		
		// No ARC
		if (arc == null) {
			return null;
		}
		
		String tmpDir = arc.getSetting(ARC.SETTING_TMP_DIR);
		String ds = arc.getSetting(ARC.SETTING_DIR_SEPARATOR);
		
		return tmpDir + ds + generationNum + ds + individualNum;
	}
}
