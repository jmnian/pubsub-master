package pubsub;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import pubsub.interfaces.EventManager;
import pubsub.interfaces.EventClient;

public class EventServer extends UnicastRemoteObject implements EventManager {
	
	private static final long serialVersionUID = 1L;
	private int OKsReceived;
	private int id;
	private String registeredName;
	protected EventManager leader;
	private EventManager failureDetector;
	private HashMap<String, HashSet<Integer>> subscriptionMap;
	private HashMap<Integer, EventClient> clientMap;

	//Constructors
	public EventServer() throws RemoteException {}

	public EventServer(int port, String name) throws Exception {
		id = port;
		setFailureDetector();
		this.registeredName = name;
		this.leader = failureDetector.getLeader(this);
		subscriptionMap = new HashMap<>();
		clientMap = new HashMap<>();
	}

	//Methods
	public void commandLineInterface() throws RemoteException {
		Scanner input = new Scanner(System.in);

		while (true) {
			System.out.println();
			System.out.println("What would you like to do? Enter choice [1-3]:");
			System.out.println(" 1: Show topics");
			System.out.println(" 2: Show subscribers");
			System.out.println(" 3: Show leader");
			System.out.println(" 4: Quit server");
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
					showLeader();
					break;
				case 4: 
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

	public void showLeader() throws RemoteException {
		System.out.println("Leader is: " + leader.getRegisteredName());
	}

	//Leader Election
	public void bully(EventManager sender) throws Exception {
		sender.ok();
		leader = null;
		List<String> higher = getServersWithHigherOrLowerId("higher");
		if (higher.size() == 0) {
			announceLeader();
			System.out.println("New leader " + registeredName + " has finished announcing victory");
		} else {
			elect(higher);
		}
	}

	private void announceLeader() throws RemoteException {
		this.leader = this;
		List<String> lowerServers = getServersWithHigherOrLowerId("lower");
		for (String name : lowerServers) {
			EventManager em = null;
			try {
				em = (EventManager) Naming.lookup(name);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			em.setLeader(this);
		}
		this.failureDetector.setLeader(this);
	}

	private void elect(List<String> higher) throws Exception {
		System.out.println(registeredName + " has entered elect");
		OKsReceived = 0;
		int n = higher.size();
		for (String name : higher) {
			EventManager em = null;
			try {
				em = (EventManager) Naming.lookup(name);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			em.bully(this);
		}
		int wait = 0;
		while (wait < 1 && OKsReceived < 1) {
			Thread.sleep(1000);
			wait++;
		}
		if (OKsReceived == 0) {
			announceLeader();
		} 
		if (OKsReceived < n) {
			wait = 0;
			while (wait < 1 && this.leader == null) {
				Thread.sleep(1000);
				wait++;
			}
			List<String> newHigher = getServersWithHigherOrLowerId("higher");
			elect(newHigher);
		}
	}


	//Helper functions
	
	// choice can be "higher" or "lower"
	private List<String> getServersWithHigherOrLowerId(String choice) throws RemoteException {
		List<String> res = new ArrayList<>();
		Set<String> allServerNames = failureDetector.getServerPool();
		for (String s : allServerNames) {
			String[] split = s.split(":");
			int curId = 0;
			int i = 0;
			while (Character.isDigit(split[1].charAt(i))) {
				curId = curId * 10 + split[1].charAt(i) - '0';
				i++;
			}
			switch(choice) {
				case "higher":
					if (curId > this.id) {
						res.add(s);
					}
					break;
				case "lower":
					if (curId < this.id) {
						res.add(s);
					}
					break;
			}
		}
		return res;
	}

	public void ok() throws RemoteException {
		System.out.println(registeredName + " got an OK");
		this.OKsReceived++;
	}

	public void setFailureDetector() throws RemoteException, MalformedURLException, NotBoundException {
		String s = "//127.0.0.1:6666/FailureDetector";
		this.failureDetector = (EventManager) Naming.lookup(s);
		if (this.failureDetector == null) {
			System.out.println("You should start Failure Detector first, then servers");
			System.exit(0);
		}
	}

	public int getId() throws RemoteException {
		return this.id;
	}

	public String getRegisteredName() throws RemoteException{
		return this.registeredName;
	}

	public void setRegisteredName(String name) throws RemoteException{
		this.registeredName = name;
	}

	public void setLeader(EventManager leader) throws RemoteException{
		this.leader = leader;
	}
	
	@Override
	public Set<String> getServerPool() throws RemoteException{
		return this.failureDetector.getServerPool();
	}

	@Override
	public EventManager getLeader(EventManager sender) throws RemoteException {
		return this.leader;
	}

	//main
	public static void main(String[] args) throws Exception {
		int port = -1;
		for(int i = 0; i < args.length; i ++) {	
			if(args[i].equals("-p")) {
				port = Integer.parseInt(args[++i]);
			} else {
				System.out.println("Correct usage: java EventServer [-p <portnumber>]");
				System.exit(1);
			}
		}

		EventServer server = null;

		try {
    		//String hostName = InetAddress.getLocalHost().getHostAddress();
			String hostName = "localhost";
			String name = "//" + hostName + ":" + port + "/EventServer";
			server = new EventServer(port, name);
    		Naming.rebind(name, server);
            System.out.println("EventManager bound in registry at " + hostName + ":" + port);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}

		server.commandLineInterface();
	}
}
