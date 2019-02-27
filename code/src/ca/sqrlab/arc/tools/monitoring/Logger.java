package ca.sqrlab.arc.tools.monitoring;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Logger} class stores messages about a process/task. Processes can
 * arbitrarily be broken down into different phases. The logger adds messages
 * to the latest phase in the process. Initially, the logger has no phases
 * unless constructed with {@link #Logger(String)}. Otherwise, no messages can
 * be added until {@link #newPhase(String)} is called.
 * 
 * <p>The logger provides different message types, which can be added via
 * {@link #debug(String)}, {@link #warning(String)}, {@link #error(String)},
 * and {@link #fatalError(String)}. Each of these correspond to a
 * {@link MessageType}. If a fatal error is logged, it will also set a flag to
 * indicate that a fatal error has occurred (which can be accessed via
 * {@link #hasFatalError()}).
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public class Logger {
	
	/** The phases of the process/task being logged. This is never null. */
	private List<Phase> phases;
	
	/** The flag indicating if a fatal error has occurred, which is initially
	 * set to false. It is set to true once {@link #fatalError(String)} is
	 * called. */
	private boolean hasFatalError;

	/**
	 * Constructs a logger with no phases. Before messages can be added to this
	 * logger, {@link #newPhase(String)} must be called.
	 * @since 1.0
	 */
	public Logger() {
		this.phases = new ArrayList<>();
	}
	
	/**
	 * Constructs a logger with the specified phase name.
	 * 
	 * @param phaseName	the name of the phase.
	 * @since 1.0
	 */
	public Logger(String phaseName) {
		this();
		newPhase(phaseName);
	}
	
	/**
	 * Adds a new phase to the logger with a specified name. Any new messages
	 * added will be added to this phase.
	 * 
	 * @param name	the name of the phase.
	 * @since 1.0
	 */
	public synchronized void newPhase(String name) {
		this.phases.add(new Phase(name));
	}
	
	/**
	 * Adds a message with the specified type and text to the last phase of the
	 * logger. If the logger has no phases, the message will not be added.
	 * 
	 * @param type	the type of message.
	 * @param text	the text of the message.
	 * @see #newPhase(String)
	 * @since 1.0
	 */
	private synchronized void addMessage(MessageType type, String text) {
		int n = phases.size();
		if (n > 0) {
			Message m = new Message(type, text);
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			if (stack != null && stack.length > 3) {
				StackTraceElement e = stack[3];
				m.setCaller(e.getClassName() + "." +
						e.getMethodName() + "(...): line " + e.getLineNumber());
			}
			this.phases.get(n - 1).addMessage(m);
		}
	}
	
	/**
	 * Logs a fatal error and sets the fatal error flag to true.
	 * 
	 * @param text	the message text.
	 * @see #hasFatalError()
	 * @see MessageType#FATAL_ERROR
	 * @since 1.0
	 */
	public void fatalError(String text) {
		addMessage(MessageType.FATAL_ERROR, text);
		this.hasFatalError = true;
	}
	
	/**
	 * Logs an error message.
	 * 
	 * @param text	the message text.
	 * @see MessageType#ERROR
	 * @since 1.0
	 */
	public void error(String text) {
		addMessage(MessageType.ERROR, text);
	}
	
	/**
	 * Logs a warning message.
	 * 
	 * @param text	the message text.
	 * @see MessageType#WARNING
	 * @since 1.0
	 */
	public void warning(String text) {
		addMessage(MessageType.WARNING, text);
	}
	
	/**
	 * Logs a debug message.
	 * 
	 * @param text	the message text.
	 * @see MessageType#DEBUG
	 * @since 1.0
	 */
	public void debug(String text) {
		addMessage(MessageType.DEBUG, text);
	}
	
	/**
	 * Clears this loggers phases and sets the fatal error flag to false.
	 * Before any new messages can be added, {@link #newPhase(String)} must be
	 * called.
	 * 
	 * @since 1.0
	 */
	public void clear() {
		this.phases.clear();
		this.hasFatalError = false;
	}

	/**
	 * Gets the phases of the process which have been captured by this logger.
	 * The phases of the logger will never be null, even if explicitly set to
	 * null.
	 * 
	 * @return the phases.
	 * @see #newPhase(String)
	 * @see #setPhases(List)
	 * @since 1.0
	 */
	public List<Phase> getPhases() {
		return phases;
	}

	/**
	 * Sets the phases for the logger. If the specified list of phases is null,
	 * the phases will be set to an empty list.
	 * 
	 * @param phases	the new phases for the logger.
	 * @see #getPhases()
	 * @since 1.0
	 */
	public void setPhases(List<Phase> phases) {
		
		if (phases == null) {
			phases = new ArrayList<>();
		}
		this.phases = phases;
		
		// Check for a fatal error
		this.hasFatalError = false;
		for (Phase p : phases) {
			if (p == null) {
				continue;
			}
			List<Message> messages = p.getMessages();
			if (messages == null) {
				continue;
			}
			
			// Check the messages in the phase
			for (Message m : messages) {
				if (m != null && m.getType() == MessageType.FATAL_ERROR) {
					hasFatalError = true;
					break;
				}
			}
			if (hasFatalError) {
				break;
			}
		}
	}

	/**
	 * Gets the flag which indicates whether or not a fatal error has occurred.
	 * This flag is only set when one or more messages captured by the logger
	 * is a fatal error.
	 * 
	 * @return true if there is at least one fatal error.
	 * @see #fatalError(String)
	 * @since 1.0
	 */
	public boolean hasFatalError() {
		return hasFatalError;
	}
	
	/**
	 * Sets the fatal error flag. This method should only be called if messages
	 * where added externally, e.g. through {@link #getPhases()}.
	 * 
	 * @param hasFatalError	the new value for the fatal error flag.
	 * @since 1.0
	 */
	public void setFatalError(boolean hasFatalError) {
		this.hasFatalError = hasFatalError;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[fatalError=" + hasFatalError +
				", phases=" + phases + "]";
	}
}
