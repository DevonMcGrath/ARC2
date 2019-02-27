package ca.sqrlab.arc.tools.monitoring;

/**
 * The {@code MessageType} enum represents a type of message to allow for
 * message filtering. For example, it may be useful to see all messages and
 * others just when something fails.
 * 
 * @author Devon McGrath
 * @see Message
 * @since 1.0
 */
public enum MessageType {
	
	/** The highest priority message type. This message type indicates a type
	 * of failure that cannot be recovered from. */
	FATAL_ERROR(0,	"[FATAL ERROR]"),
	
	/** The second highest priority message type. This message type indicates
	 * some kind of failure, however, the program was able to continue. */
	ERROR(1,		"[ERROR]"),
	
	/** The third highest priority message type. This message type indicates
	 * that something may be missing so the final results may differ from the
	 * expected results. */
	WARNING(2,		"[WARNING]"),
	
	/** The lowest priority message type. This message type is purely
	 * informational and does not indicate any abnormal behaviour. */
	DEBUG(3,		"[DEBUG]");
	
	/** The priority of the message. The lower the value, the higher the
	 * priority. */
	public final int priority;
	
	/** The prefix which may be prepended to a message to indicate its type. */
	public final String prefix;
	
	private MessageType(int priority, String prefix) {
		this.priority = priority;
		this.prefix = prefix;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[priority=" + priority +
				", prefix='" + prefix + "']";
	}
}
