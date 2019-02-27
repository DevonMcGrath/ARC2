package ca.sqrlab.arc.tools.monitoring;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Phase} class represents a phase of a task/process for the logger.
 * 
 * @author Devon McGrath
 * @see Logger
 * @since 1.0
 */
public class Phase {
	
	/** The name of the phase. */
	private String name;
	
	/** The messages associated with the phase. */
	private List<Message> messages;

	/**
	 * Constructs a new phase with no name.
	 * @since 1.0
	 */
	public Phase() {
		this.messages = new ArrayList<>();
	}
	
	/**
	 * Constructs a phase with the specified name.
	 * @param name	the name of the phase.
	 * @since 1.0
	 */
	public Phase(String name) {
		this();
		setName(name);
	}
	
	/**
	 * Adds a message to the list of phase messages. If the message is null,
	 * this method does nothing.
	 * 
	 * @param m	the message.
	 * @since 1.0
	 */
	public void addMessage(Message m) {
		if (m != null) {
			this.messages.add(m);
		}
	}

	/**
	 * Gets the name of the phase.
	 * 
	 * @return the phase name.
	 * @since 1.0
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the phase, which may be presented to the user in some
	 * form.
	 * 
	 * @param name	the name of the phase.
	 * @since 1.0
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the messages associated with the phase. This value will never be
	 * null. If there are no messages, then an empty list is returned.
	 * 
	 * @return the messages.
	 * @since 1.0
	 */
	public List<Message> getMessages() {
		return messages;
	}

	/**
	 * Sets the messages associated with the phase.
	 * 
	 * @param messages	the phase's messages.
	 * @since 1.0
	 */
	public void setMessages(List<Message> messages) {
		if (messages == null) {
			messages = new ArrayList<>();
		}
		this.messages = messages;
	}
}
