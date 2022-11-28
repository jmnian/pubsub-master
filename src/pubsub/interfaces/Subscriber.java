package pubsub.interfaces;

import java.rmi.RemoteException;

import pubsub.Event;
import pubsub.Topic;

public interface Subscriber extends java.rmi.Remote {
	
	/**
	 * Subscribe to a topic
	 * 
	 * @param topic to subscribe to
	 * @throws RemoteException
	 */
	public void subscribe(Topic topic) throws RemoteException;
	
	/**
	 * Unsubscribe from a topic 
	 * 
	 * @param topic topic to unsubscribe from
	 * @throws RemoteException
	 */
	public void unsubscribe(Topic topic) throws RemoteException;

	/**
	 * 
	 * @param e the event on which to notify the subscriber
	 * @throws RemoteException
	 */
	public void notify(Event e) throws RemoteException;

}
