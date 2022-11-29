package pubsub.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface EventManager extends Remote {

	public void addClient(int id, EventClient c) throws RemoteException;
	
	public void createTopic(String topic) throws RemoteException;
	
	public boolean publish(String topic, String msg) throws RemoteException;
	
	public boolean subscribe(String topic, Integer clientID) throws RemoteException;

	public boolean unsubscribe(String topic, Integer clientID) throws RemoteException;

	public Set<String> getServerPool() throws RemoteException;

	public EventManager getLeader(EventManager sender) throws RemoteException;

	public void setLeader(EventManager leader) throws RemoteException;

	public String getRegisteredName() throws RemoteException;

	public void setRegisteredName(String name) throws RemoteException; 

	public void bully(EventManager sender) throws Exception;

	public void ok() throws RemoteException;
}
