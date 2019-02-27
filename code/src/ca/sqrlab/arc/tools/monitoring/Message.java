package ca.sqrlab.arc.tools.monitoring;

/**
 * The {@code Message} class stores a message and the type of message.
 * 
 * @author Devon McGrath
 * @since 1.0
 */
public class Message {
	
	/** The type of message, which is never null. */
	private MessageType type;
	
	/** The text associated with this message, which is never null. */
	private String text;
	
	/** The name of the calling method/constructor. */
	private String caller;
	
	/**
	 * Constructs an empty debug message.
	 * @since 1.0
	 */
	public Message() {
		this(MessageType.DEBUG, "");
	}
	
	/**
	 * Constructs a message with the specified type and text.
	 * 
	 * @param type	the type of message.
	 * @param text	the text belonging to the message.
	 * @since 1.0
	 */
	public Message(MessageType type, String text) {
		setType(type);
		setText(text);
	}

	/**
	 * Gets the type of message this object represents. Note: the value
	 * returned will never be null.
	 * 
	 * @return the message type.
	 * @since 1.0
	 */
	public MessageType getType() {
		return type;
	}
	
	/**
	 * Creates a printable version of this message with the type prefix and
	 * caller on the first line followed by the message on the remaining lines.
	 * 
	 * @return the complete message.
	 * @see #getFullMessageHTML()
	 * @since 1.0
	 */
	public String getFullMessage() {
		String c = "(Caller not specified)";
		if (caller != null && !caller.isEmpty()) {
			c = caller;
		}
		return type.prefix + " " + c + "\n" + text;
	}
	
	/**
	 * Creates a printable version of this message in HTML with the type prefix
	 * and caller on the first line followed by the message on the remaining
	 * lines.
	 * 
	 * @return the complete HTML message.
	 * @see #getFullMessage()
	 * @since 1.0
	 */
	public String getFullMessageHTML() {
		
		// Make sure the caller is initialized
		String c = "(Caller not specified)";
		if (caller != null && !caller.isEmpty()) {
			c = caller;
		}
		
		// Determine formatting based on type of message
		String line1 = type.prefix, rest = text.replaceAll("\r?\n", "<br />");
		if (type == MessageType.DEBUG) {
			line1 = "<b style=\"color: #FF00FF;\">" + line1 + "</b> " + c;
		} else if (type == MessageType.WARNING) {
			line1 = "<b style=\"color: orange;\">" + line1 + "</b> " + c;
		} else if (type == MessageType.ERROR) {
			line1 = "<b style=\"color: #FF0000;\">" + line1 + "</b> " + c;
		} else if (type == MessageType.FATAL_ERROR) {
			line1 = "<b style=\"color: #FF0000;\">" + line1 + " " + c;
			rest += "</b>";
		} else {
			line1 = line1 + " " + c;
		}
		
		return line1 + "<br />" + rest;
	}

	/**
	 * Sets the type of message this object represents. If the type parameter
	 * is null, the message type will default to {@link MessageType#DEBUG}.
	 * 
	 * @param type	the type of message.
	 * @since 1.0
	 */
	public void setType(MessageType type) {
		if (type == null) {
			type = MessageType.DEBUG;
		}
		this.type = type;
	}

	/**
	 * Gets the message text. Note: the value returned will never be null.
	 * 
	 * @return the message text.
	 * @since 1.0
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the message text. If the value is null, the message text will be
	 * set to an empty string.
	 * 
	 * @param text	the text belonging to the message.
	 * @since 1.0
	 */
	public void setText(String text) {
		if (text == null) {
			text = "";
		}
		this.text = text;
	}
	
	/**
	 * Gets the name of the method/constructor which the message was created
	 * from. This value may be null.
	 * 
	 * @return the caller.
	 * @since 1.0
	 */
	public String getCaller() {
		return caller;
	}

	/**
	 * Sets the name of the method or constructor which the message is
	 * referring to.
	 * 
	 * @param caller	the name of the caller.
	 * @since 1.0
	 */
	public void setCaller(String caller) {
		this.caller = caller;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[type=" + type +
				", text='" + text + "', caller='" + caller + "']";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
