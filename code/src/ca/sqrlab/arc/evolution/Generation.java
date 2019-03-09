package ca.sqrlab.arc.evolution;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Generation} class represents a generation within the
 * {@link ARCGeneticAlgorithm}. It is simply a container for the entire
 * population and provides some convenience methods such as - for example -
 * {@link #getBetter(Individual)}.
 * 
 * @author Devon McGrath
 * @see Individual
 * @since 1.0
 */
public class Generation {
	
	/** The individuals in this generation. */
	private List<Individual> population;
	
	/**
	 * Constructs a generation with no individuals.
	 * @since 1.0
	 */
	public Generation() {}
	
	/**
	 * Constructs a generation with the specified population.
	 * @param population	the individuals in the generation.
	 * @since 1.0
	 */
	public Generation(List<Individual> population) {
		this.population = population;
	}
	
	/**
	 * Constructs a generation with the specified population.
	 * @param population	the individuals in the generation.
	 * @since 1.0
	 */
	public Generation(Individual... population) {
		this.population = new ArrayList<>();
		if (population != null && population.length > 0) {
			int n = population.length;
			for (int i = 0; i < n; i ++) {
				Individual program = population[i];
				if (program == null) {
					continue;
				}
				this.population.add(program);
			}
		}
	}

	/**
	 * Gets the individual with the best fitness score in the population.
	 * 
	 * @return the best individual or null if there are no individuals in the
	 * population.
	 * @since 1.0
	 */
	public Individual getBestIndividual() {
		
		// No individuals
		if (population == null || population.isEmpty()) {
			return null;
		}
		
		// Get the best individual
		int idx = 0, n = population.size();
		float score = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < n; i ++) {
			Individual program = population.get(i);
			float tmp = program.getScore();
			if (tmp > score) {
				score = tmp;
				idx = i;
			}
		}
		
		return population.get(idx);
	}
	
	/**
	 * Gets a list of individuals which have better fitness scores than the
	 * control individual.
	 * 
	 * @param control	the control individual to compare scores to.
	 * @return a list of individuals better than the control or an empty list
	 * if none exist or the control is null.
	 * 
	 * @see #getBetterOrSame(Individual)
	 * @since 1.0
	 */
	public List<Individual> getBetter(Individual control) {
		
		// No population or invalid individual
		List<Individual> result = new ArrayList<>();
		if (population == null || control == null) {
			return result;
		}
		
		// Add all the individuals who are better
		float score = control.getScore();
		for (Individual i : population) {
			if (i.getScore() > score) {
				result.add(i);
			}
		}
		
		return result;
	}
	
	/**
	 * Gets a list of individuals which have fitness scores better or the same
	 * as the control individual.
	 * 
	 * @param control	the control individual to compare scores to.
	 * @return a list of individuals better or the same as the control or an
	 * empty list if none exist or the control is null.
	 * 
	 * @see #getBetter(Individual)
	 * @since 1.0
	 */
	public List<Individual> getBetterOrSame(Individual control) {
		
		// No population or invalid individual
		List<Individual> result = new ArrayList<>();
		if (population == null || control == null) {
			return result;
		}
		
		// Add all the individuals who are better or the same
		float score = control.getScore();
		for (Individual i : population) {
			if (i.getScore() >= score) {
				result.add(i);
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the individuals associated with this generation.
	 * 
	 * @return the population.
	 * @see #setPopulation(List)
	 * @since 1.0
	 */
	public List<Individual> getPopulation() {
		return population;
	}
	
	/**
	 * Sets the individuals associated with this generation.
	 * 
	 * @param population	the new population.
	 * @return a reference to this generation.
	 * @see #getPopulation()
	 * @since 1.0
	 */
	public Generation setPopulation(List<Individual> population) {
		this.population = population;
		return this;
	}
	
	@Override
	public String toString() {
		final int s = population == null? 0 : population.size();
		return getClass().getSimpleName() + "[size=" + s + ", population="
				+ population + "]";
	}
}
