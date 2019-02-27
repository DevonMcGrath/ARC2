package ca.sqrlab.arc.evolution;

public class Mutation {

	private String name;
	
	public Mutation() {
		this("");
	}
	
	public Mutation(String name) {
		setName(name);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if (name == null || name.isEmpty()) {
			name = "(UNKNOWN)";
		}
	}
}
