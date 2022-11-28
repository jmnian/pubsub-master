package pubsub;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class Event implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id = 0;
	private Topic topic;
	private String content;
	private LinkedHashSet<Integer> toBeNotified;
	
	public Event(Topic topic, String content) {
		this.topic = topic;
		this.content = content;

		toBeNotified = new LinkedHashSet<>();
	}
	
	/**
	 * Used by the server to set the ID once it has been published
	 * @param n the ID to set this too
	 * @return this instance
	 */
	public Event setID(int n) {
		this.id = n;
		return this;
	}
	
	public int getID() {
		return id;
	}
	
	public Topic getTopic() {
		return topic;
	}
	
	public String getContent() {
		return content;
	}
	
	public boolean equals(Object obj) {
		Event e = (Event) obj;
		return this.topic.equals(e.topic) && this.content.equals(e.content);
	}
	
	public int hashCode() {
		return topic.hashCode() + content.hashCode();
	}
	
	public String toString() {
		String event = "Event " +this.id +"-"+ this.content + "\n" +
					   "Published under Topic "+ topic.getID() +"-"+ topic.getName() + "\n";
		return event;
	}
	
	/**
	 * 
	 * @return iterator to allow for removal of Subscribers as they are notified one by one about this event
	 */
	public synchronized Iterator<Integer> iterator() {
		return toBeNotified.iterator();
	}

	/**
	 * 
	 * @return number of users left to notify
	 */
	public synchronized int notifySize() {
		return toBeNotified.size();
	}
	
	/**
	 * 
	 * @param c some other list of subscribers (either content or topic filtering) to be added to the 
	 * list of subscribers of this event
	 * @return true/false that the list was successfully added
	 */
	public synchronized boolean addSubscriberList(Collection<Integer> c) {
		if (c != null)
			return toBeNotified.addAll(c);
		return false;
	}
}
