package pubsub;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

public class EventManager extends UnicastRemoteObject {
	
	private static final long serialVersionUID = 1L;

	private int id;
	
	private HashMap<String, HashSet<Integer>> subscriptionMap;
	private HashMap<Integer, PubSubClient> clientMap;

	public EventManager(int port) throws RemoteException {
		this.id = port;
		subscriptionMap = new HashMap<>();
		clientMap = new HashMap<>();
	}

	public void addClient(int id, PubSubClient c) {
		synchronized (clientMap) {
			clientMap.put(id, c);
		}
	}
	
	public void createTopic(String topic) throws RemoteException {
		if(!subscriptionMap.containsKey(topic)) {
			synchronized (subscriptionMap) {
				subscriptionMap.put(topic, new HashSet<>());
			}
		}
	}
	
	public boolean publish(String topic, String msg) throws RemoteException {
		if(!subscriptionMap.containsKey(topic)) return false;

		HashSet<Integer> subscribers = subscriptionMap.get(topic);

		synchronized (subscribers) {
			for(Integer clientID : subscribers) {
				clientMap.get(clientID).notify(topic, msg);
			}
		}

		return true;
	}
	
	public boolean subscribe(String topic, Integer clientID) throws RemoteException {
		if(!subscriptionMap.containsKey(topic)) return false;

		HashSet<Integer> subscribers = subscriptionMap.get(topic);
		synchronized (subscribers) {
			subscribers.add(clientID);
		}

		return true;
	}

	public boolean unSubscribe(String topic, Integer clientID) throws RemoteException {
		if(!subscriptionMap.containsKey(topic)) return false;

		HashSet<Integer> subscribers = subscriptionMap.get(topic);
		synchronized (subscribers) {
			subscribers.remove(clientID);
		}

		return true;
	}

	public Set<String> getTopics() {
		return subscriptionMap.keySet();
	}
	
	public void showSubscribers() throws RemoteException {
		for(Entry<String, HashSet<Integer>> entry : subscriptionMap.entrySet()) {
			System.out.println("Topic: " + entry.getKey());
			for(Integer id : entry.getValue()) {
				System.out.println("Subscribers: ");
				System.out.println(id);
			}
		}
	}
}
