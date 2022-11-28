package pubsub.interfaces;

import pubsub.Event;
import pubsub.Topic;

public interface Publisher extends java.rmi.Remote {
	
	public void publish(Event event);
	public void create(Topic newTopic);

}
