package pubsub;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Scanner;

import pubsub.interfaces.Client;
import pubsub.interfaces.Server;

public class EventManager extends UnicastRemoteObject implements Server {
	
	private static final long serialVersionUID = 1L;
	//Amount of time to wait between attempts to contact a non-responsive agent
	public static final int TIMEOUT = 1000;
	//counters used to assign Unique IDs
	private Integer topicID = 0;
	private Integer subscriberID = 0;
	private Integer eventID = 0;
	//Storage for all Topic Containers (topic plus subscribers)
	private LinkedHashSet<TopicContainer> allTopicContainers;
	//Events are stored here while they continue to try to contact a missing subscriber
	private LinkedList<Event> pendingEvents;
	// Maps from the ID of a client to the actual RMI object of the client 
	// This allows the client to leave and come back later without 
	//changing the unique identifier
	private HashMap<Integer, Client> clientBinding;

	public EventManager() throws RemoteException {
		allTopicContainers = new LinkedHashSet<>();
		pendingEvents = new LinkedList<>();
		clientBinding = new HashMap<>();
	}

	public int sayHello(Client c) throws RemoteException {
		synchronized (clientBinding) {
			clientBinding.put(++subscriberID, c);
			return subscriberID;
		}
	}

	/**
	 * see interface javadoc
	 */
	public int sayHello(Integer ID, Client c) throws RemoteException {
		synchronized (clientBinding) {
			clientBinding.put(ID, c);
			return ID;
		}
	}
	
	public void unbind(Integer ID) {
		synchronized (clientBinding) {
			clientBinding.put(ID, null);
		}
	}
	
	public void unbindPermanent(Integer ID) {
		synchronized (clientBinding) {
			clientBinding.remove(ID);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	//  Asynchronous notification service
	////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * This is the method that runs in the background to contact all the Subscribers continually when they 
	 * are offline until they return
	 */
	public void startService() {
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				while(true) {
					try { Thread.sleep(TIMEOUT); } catch (InterruptedException e1) { }
					synchronized(pendingEvents) {
						while (pendingEvents.isEmpty()) {
							try {  
								pendingEvents.wait(); 
							} catch (Exception e) { }
						}
						asynchNotify();
					}
				}
			}
			/**
			 * helper method that iterates through each event that is pending compelte 
			 * notification of its subscribers
			 */
			public void asynchNotify() {
				Iterator<Event> event_iter = pendingEvents.iterator();
				while( event_iter.hasNext() ) {
					if (notifySubscribers(event_iter.next()) == 0) 
						event_iter.remove();	
				}
			}
		});
		//Daemon allows this thread not to block program from exiting
		t.setDaemon(true);
		t.start();		
	}

	
	////////////////////////////////////////////////////////////////////////////////////
	//  Publisher services 
	////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Helper method to notify all subscribers of a given event.  Subscribers have been added based on both 
	 * Topic filtering and content filtering at this point.  As subscribers are contacted, they are removed from
	 * the internal list of the event.  When the event subscriber list is empty, then the event is removed from
	 * the lsit of all pending events
	 * 
	 * @param event Event to notify subscribers of
	 */
	public int notifySubscribers(Event event) {
		Iterator<Integer> sub_iter = event.iterator();
		while (sub_iter.hasNext()) {
			try {
				Integer subID = sub_iter.next();
				if (clientBinding.get(subID) != null) {
					clientBinding.get(subID).notify(event);
					sub_iter.remove();
				}
			} catch(RemoteException e) { } //Do nothing on remote exception, try again later 
		}
		//when this returns 0, we know every subscriber has received the message
		return event.notifySize();
	}
	
	/**
	 * see interface javadoc
	 */
	public int publish(Event event) throws RemoteException {
		if (event.getID() != 0) {
			System.err.println("Event has already been published.");
			return 0;
		}
		synchronized (allTopicContainers) {
			for( TopicContainer tc : allTopicContainers) {
				if (tc.getTopic().getID() == event.getTopic().getID() ) {
					event.setID(++eventID).addSubscriberList(tc.getSubscribers());
					if (notifySubscribers(event) > 0) {
						synchronized (pendingEvents) {
							pendingEvents.add(event);
							pendingEvents.notifyAll();
						}
					}
					return eventID;
				}
			}
		}
		System.err.println("Event topic not found.");
		return 0;
	}
	
	/**
	 * see interface javadoc
	 */
	public int addTopic(Topic topic) throws RemoteException {
		synchronized (allTopicContainers) {
			if (allTopicContainers.add( new TopicContainer(topic) )) {
				topic.setID(++topicID);
				return topicID;
			}
			return 0;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	//  Subscriber services 
	////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * see interface javadoc
	 */
	public boolean addSubscriber(Integer subID, Topic topic) throws RemoteException {
		for( TopicContainer tc : allTopicContainers) {
			if (tc.getTopic().getID() == topic.getID() ) {
				return tc.addSubscriber(subID);
			}
		}
		return false;
	}

	/**
	 * see interface javadoc
	 */
	public boolean removeSubscriber(Integer subID, Topic topic) throws RemoteException {
		for( TopicContainer tc : allTopicContainers) {
			if (tc.getTopic().getID() == topic.getID() ) {
				return tc.removeSubscriber(subID);
			}
		}
		return false;
	}
	
	/**
	 * see interface javadoc
	 */
	public ArrayList<Topic> getTopics() {
		synchronized (allTopicContainers) {
			ArrayList<Topic> topics = new ArrayList<>();
			for (TopicContainer tc : allTopicContainers)
				topics.add( tc.getTopic() );
			return topics;
		}
	}
	
	public void commandLineInterface() throws RemoteException {
		Scanner input = new Scanner(System.in);

		while (true) {
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
					for (TopicContainer tc : allTopicContainers)
						System.out.print( tc.getTopic() );
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
	
	/**
	 * show the complete list of subscribers, used by server for command line printing
	 * Prints both all the subscribers to each topic and all the subscribers to each 
	 * keyword
	 */
	public void showSubscribers() throws RemoteException {
		for( TopicContainer tc : allTopicContainers) 
			System.out.print("Topic: " +tc.getTopic().getName()+ "\n" +
							 "Subscribers: " + tc.printSubscribers());
	}
}
