package pubsub.interfaces;

import java.rmi.RemoteException;
import java.rmi.Remote;

import pubsub.Event;
import pubsub.Topic;

public interface Client extends Remote {

    // publish 
    public void publish(Event event) throws RemoteException;
	public void create(Topic newTopic) throws RemoteException;

    // subscribe
	public void subscribe(Topic topic) throws RemoteException;
	public void unsubscribe(Topic topic) throws RemoteException;
	public void notify(Event e) throws RemoteException;

}
