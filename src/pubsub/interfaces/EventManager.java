package pubsub.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EventManager extends Remote {

	public void addClient(int id, EventClient c) throws RemoteException;
	
	public void createTopic(String topic) throws RemoteException;
	
	public boolean publish(String topic, String msg) throws RemoteException;
	
	public boolean subscribe(String topic, Integer clientID) throws RemoteException;

	public boolean unsubscribe(String topic, Integer clientID) throws RemoteException;

}
