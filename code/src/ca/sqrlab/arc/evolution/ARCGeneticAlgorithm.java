package ca.sqrlab.arc.evolution;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.sqrlab.arc.ARC;
import ca.sqrlab.arc.ARCRunner;
import ca.sqrlab.arc.FinishListener;
import ca.sqrlab.arc.io.FileUtils;
import ca.sqrlab.arc.tools.ARCUtils;
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
		
		// Check if requested to stop
		if (ar != null && ar.shouldStop()) {
			l.fatalError("ARC was requested to stop.");
			finishPhase(l);
			return l;
		}
		
		finishPhase(l);
		
		// Continuously evolve until a solution is found
		for (int i = 1; i <= maxGenerations; i ++) {
			l.newPhase("Generation " + i);
			
			// Evolve the population
			if (evolve(l)) {
				l.debug("Solution found.");
				foundFix = true;
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
		// TODO implement genetic algorithm
		
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
		generateMutants(l);
		if (l.hasFatalError()) {
			return false;
		}
		
		
		this.generations.add(g);
		
		return false; // TODO
	}
	
	/**
	 * Generates mutants for the best individuals found so far (or the base
	 * project). If for a given individual, the same mutation operator is
	 * selected, then no more mutations are added for that individual. Only one
	 * mutation operator per candidate (from {@link #getMutationCandidates()})
	 * will be applied to the program to avoid creating thousands of files.
	 * 
	 * @param l	the logger to track the mutation process.
	 * 
	 * @return the individuals which mutants were generated for.
	 * 
	 * @since 1.0
	 */
	private List<Individual> generateMutants(Logger l) {
		
		// Determine all the possible individuals which can be mutated
		List<Individual> candidates = getMutationCandidates();
		final float USE_BEST = 0.75f;
		int n = candidates.size();
		int count = (int) ((USE_BEST * n) + 0.5f);
		
		// Make sure there is at least one candidate
		if (count < 1) {
			count = 1;
		}
		
		// Remove mutations for older candidates which are no longer used
		for (int i = count; i < n; i ++) {
			Individual invalid = candidates.remove(count);
			removeMutantsFor(invalid);
		}
		n = candidates.size();
		
		final int MAX_TRIES = 10;
		
		// Get the new mutants for each candidate
		List<TXLMutation> mutations = new ArrayList<>();
		for (Individual individual : candidates) {
			
			// Determine which operators to bias towards
			TestingSummary summary = individual.getTestSummary();
			List<TestResult> dataraces = summary.getResultsFor(TestStatus.DATA_RACE);
			List<TestResult> deadlocks = summary.getResultsFor(TestStatus.DEADLOCK);
			int dataraceCount = (dataraces == null)? 0 : dataraces.size();
			int deadlockCount = (deadlocks == null)? 0 : deadlocks.size();
			int total = dataraceCount + deadlockCount;
			
			TXLMutation m = null;
			int attempt = 0;
			File[] children = null;
			
			// Ensure that we explore multiple mutations if the first few do
			// not generate any mutants
			while (attempt++ < MAX_TRIES) {
				
				// If we don't have information, just get a random operator
				if (total == 0) {
					m = TXLMutation.getRandomMutation();
				}
				
				// Otherwise, bias the operator
				else {
					double dataraceChance = ((double) dataraceCount) / total;
					if (Math.random() <= dataraceChance) {
						m = TXLMutation.getRandomDataraceMutation();
					} else {
						m = TXLMutation.getRandomDeadlockMutation();
					}
					
					// If for some reason no operator, try to get a random one
					if (m == null) {
						m = TXLMutation.getRandomMutation();
					}
				}
				
				// Failed to find an operator
				if (m == null) {
					l.fatalError("Unable to find mutation operator to use on individual "
							+ individual.getId() + " from generation "
							+ individual.getGeneration() + ".");
					return null;
				}
				
				// Generate the mutants
				String dir = mutateIndividual(individual, m, l);
				if (l.hasFatalError()) {
					return null;
				}
				
				// Check for the directory
				if (dir == null || dir.isEmpty() || !(new File(dir)).isDirectory()) {
					l.fatalError("Unable to mutate individual " +
							individual.getId() + " from generation " +
							individual.getGeneration() + " using " + m + ".");
					return null;
				}
				
				// Check if any mutants were generated
				children = (new File(dir)).listFiles();
				if (children != null && children.length > 0) {
					break;
				}
			}
			mutations.add(m);
			
			// No valid mutations
			if (children == null || children.length == 0) {
				l.fatalError("Unable to mutate individual " +
						individual.getId() + " from generation " +
						individual.getGeneration() + ".");
				return null;
			}
		}
		
		// TODO
		
		return candidates;
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
		return null;// TODO
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
		for (int i = 1; i < n; i ++) {
			candidates.addAll(generations.get(i).getBetter(original));
		}
		if (candidates.isEmpty()) { // no candidates found
			
			// Try to find ones which perform the same as the original
			for (int i = 1; i < n; i ++) {
				candidates.addAll(generations.get(i).getBetterOrSame(original));
			}
			if (candidates.isEmpty()) { // none found still; add the original
				candidates.add(original);
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
	 * Removes all the mutated versions of the program for the specified
	 * individual. If this individual was never mutated, then this method
	 * does nothing.
	 * 
	 * @param individual	the individual to remove mutants for.
	 * 
	 * @since 1.0
	 */
	private void removeMutantsFor(Individual individual) {
		
		// Nothing to do
		if (individual == null) {
			return;
		}
		
		String slash = arc.getSetting(ARC.SETTING_DIR_SEPARATOR);
		String base = arc.getSetting(ARC.SETTING_MUTANT_DIR);
		if (base == null || base.isEmpty()) {
			return;
		}
		
		// Remove the appropriate directory
		FileUtils.remove(base + slash + individual.getGeneration() +
				slash + individual.getId());
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
	 */
	public boolean foundFix() {
		return foundFix;
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
