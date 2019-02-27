package ca.sqrlab.arc;

/**
 * The {@code FinishListener} interface provides an
 * {@link #onFinish(int, Object)} method which is used to denote the end of a
 * process/task.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public interface FinishListener {

	/**
	 * When a process is finished, this method is called by the class which
	 * completed the task. It is up to the caller of this method to ensure the
	 * ID does not collide with any other callers of this method. The object
	 * may be any object, including null.
	 * 
	 * @param id	the integer value identifying the process which finished.
	 * @param obj	the relevant object for the process or null.
	 * @since 1.0
	 */
	public void onFinish(int id, Object obj);
}
