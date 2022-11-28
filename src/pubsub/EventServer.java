package pubsub;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;

import pubsub.interfaces.EventManager;
import pubsub.interfaces.EventClient;

public class EventServer extends UnicastRemoteObject implements EventManager {
	
	private static final long serialVersionUID = 1L;

	private int id;
	
	private HashMap<String, HashSet<Integer>> subscriptionMap;
	private HashMap<Integer, EventClient> clientMap;

	public EventServer(int port) throws RemoteException {
		this.id = port;
		subscriptionMap = new HashMap<>();
		clientMap = new HashMap<>();
	}

	public static void main(String[] args) throws RemoteException {
		int port = 1099;
		for(int i = 0; i < args.length; i ++) {	
			if(args[i].equals("-p")) {
				port = Integer.parseInt(args[++i]);
			} else {
				System.out.println("Correct usage: java EventServer [-p <portnumber>]");
				System.exit(1);
			}
		}

		EventServer server = new EventServer(port);

		try {
    		String hostName = InetAddress.getLocalHost().getHostAddress();
    		Naming.rebind("//" + hostName + ":" + port + "/EventServer", server);
            System.out.println("EventManager bound in registry at " + hostName + ":" + port);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}

		server.commandLineInterface();
	}

	public void commandLineInterface() throws RemoteException {
		Scanner input = new Scanner(System.in);

		while (true) {
			System.out.println();
			System.out.println("What would you like to do? Enter choice [1-3]:");
			System.out.println(" 1: Show topics");
			System.out.println(" 2: Show subscribers");
			System.out.println(" 3: Quit server");
			System.out.print(": ");
			int choice = -1;

			try {
				choice = input.nextInt(); 
				input.nextLine();
			} catch (Exception e) { 
				input.nextLine(); 
			}

			switch (choice) {
				case 1: 
					showTopics();
					break;
				case 2: 
					showSubscribers();
					break;
				case 3: 
					input.close(); 
					System.exit(0); 
				default: 
					System.out.println("Input not recognized");
			}
		}
	}

	public void addClient(int id, EventClient c) {
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

	public boolean unsubscribe(String topic, Integer clientID) throws RemoteException {
		if(!subscriptionMap.containsKey(topic)) return false;

		HashSet<Integer> subscribers = subscriptionMap.get(topic);
		synchronized (subscribers) {
			subscribers.remove(clientID);
		}

		return true;
	}

	public void showTopics() {
		for(String topic : subscriptionMap.keySet()) {
			System.out.println(topic);
		}
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
