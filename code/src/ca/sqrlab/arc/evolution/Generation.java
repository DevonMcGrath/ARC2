package ca.sqrlab.arc.evolution;

import java.util.ArrayList;
import java.util.List;

public class Generation {
	
	private List<Individual> population;
	
	public Generation() {}
	
	public Generation(List<Individual> population) {
		this.population = population;
	}
	
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
	
	public List<Individual> getPopulation() {
		return population;
	}
}
