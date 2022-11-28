package pubsub.interfaces;

import java.rmi.RemoteException;
import java.rmi.Remote;

import java.util.ArrayList;

import pubsub.Event;
import pubsub.Topic;

public interface Server extends Remote {

	public int addTopic(Topic topic) throws RemoteException;
	public int publish(Event event) throws RemoteException;

	public boolean addSubscriber(Integer subID, Topic t) throws RemoteException;
	public boolean removeSubscriber(Integer subID, Topic t) throws RemoteException;
	
	public ArrayList<Topic> getTopics() throws RemoteException;

	// /**
	//  * This method establishes the relationship between server and client
	//  * 
	//  * @param sub Subscriber registering with the server
	//  * @return unique int ID of this client for the Server to track
	//  * @throws RemoteException if server is offline
	//  */
	public int sayHello(Client c) throws RemoteException;
	
	// /**
	//  * This method re-establishes the relationship between server and client
	//  * 
	//  * @param sub Subscriber re-registering with the server after being away
	//  * @param ID Integer that the client received when first registering
	//  * @return same ID this client had previously
	//  * @throws RemoteException if server is offline
	//  */
	public int sayHello(Integer ID, Client c) throws RemoteException;
	
	/**
	 * Subscriber sets ID to null while he is offline, saysHello once he comes back online to re-establish
	 * relationship
	 * 
	 * @param ID unique subscriberID that is set to null
	 * @throws RemoteException
	 */
	public void unbind(Integer ID) throws RemoteException;
	
	/**
	 * This user is quitting and will not be returning
	 * 
	 * @param ID unique subscriberID that is removed altogether
	 * @throws RemoteException
	 */
	public void unbindPermanent(Integer ID) throws RemoteException;

	public void showSubscribers() throws RemoteException;
}

