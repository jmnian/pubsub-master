package pubsub.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EventClient extends Remote {

	public void notify(String topic, String msg) throws RemoteException;
	
}
