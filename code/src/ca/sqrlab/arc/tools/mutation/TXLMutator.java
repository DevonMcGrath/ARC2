package ca.sqrlab.arc.tools.mutation;

import ca.sqrlab.arc.java.JavaProject;
import ca.sqrlab.arc.tools.ProcessStatus;

public class TXLMutator extends Mutator {
	
	private TXLMutation mutation;
	
	public TXLMutator(TXLMutation mutation) {
		this.mutation = mutation;
	}

	@Override
	protected void runMutation(JavaProject project, ProcessStatus result) {
		// TODO Auto-generated method stub
		
	}

	public TXLMutation getMutation() {
		return mutation;
	}

	public void setMutation(TXLMutation mutation) {
		this.mutation = mutation;
	}
}
