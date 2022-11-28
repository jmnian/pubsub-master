package pubsub;

import java.io.Serializable;

public class Topic implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final String name;
	private int id;

	public Topic(String name) {
		this.name = name;
	}

	public Topic setID(int n) {
		this.id = n;
		return this;
	}
	
	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	/**
	 * Override Object toString
	 */
	public String toString() {
		return "Topic " + this.id + "-" + this.name + "\n";
	}
		
	/**
	 * Overrides Object equals in order to hash this topic correctly
	 */
	public boolean equals(Object obj) {
		return this.name.equals(((Topic)obj).name);
	}
	
	/**
	 * Used by the Topic Container's hash to ensure collisions for equivalent underlying topics
	 */
	public int hashCode() {
		return name.hashCode();
	}
}
