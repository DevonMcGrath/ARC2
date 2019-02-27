package ca.sqrlab.arc.tools.mutation;

import java.io.File;

import ca.sqrlab.arc.java.JavaProject;
import ca.sqrlab.arc.tools.ProcessStatus;

public abstract class Mutator {
	
	public ProcessStatus mutate(JavaProject project) {
		
		// Check that the project is valid
		ProcessStatus result = new ProcessStatus();
		if (project == null) {
			result.setFatalError(true);
			result.setFatalErrorMessage("Error: no project.");
		} else {
			
			// Not a valid directory
			String path = project.getPath();
			if (path != null && (new File(path).isDirectory())) {
				result.setFatalError(true);
				result.setFatalErrorMessage("Error: the path '" + path +
						"' is not valid.");
			}
			
			// No error, run the mutation
			else {
				runMutation(project, result);
			}
		}
		
		return result;
	}
	
	protected abstract void runMutation(JavaProject project, ProcessStatus result);

}
