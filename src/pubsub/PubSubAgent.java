package pubsub;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pubsub.interfaces.Server;
import pubsub.interfaces.Client;

public class PubSubAgent extends UnicastRemoteObject implements Client {

	private static final long serialVersionUID = 1L;

	//Used by the agent to try to contact the server repeatedly before giving up
	public static final int MAX_TRIES = 100;
	//Used to make a thread sleep rather than repeated trying to contact the server
	public static final int TIMEOUT = 1000;
	
	private Server server;
	//Used by the subscriber
	private ArrayList<Topic> subscrTopics;
	private ArrayList<Event> recvdEvents;
	//Used by the publisher
	private ArrayList<Topic> myPubTopics;
	private ArrayList<Event> myPubEvents;
	//Unique identifier assigned by the server
	private Integer ID;
	/**
	 * Constructor 
	 * 
	 * @param _server taken from the rmi registry 
	 * @throws RemoteException
	 */
	public PubSubAgent(Server server) throws RemoteException {
		this.server = server;
		if (server != null)
			this.ID = server.sayHello(this);
		subscrTopics = new ArrayList<>();
		recvdEvents = new ArrayList<>();
		myPubTopics = new ArrayList<>();
		myPubEvents = new ArrayList<>();
	}
	
	public boolean equals(Object obj) {
		return this.ID.equals(((PubSubAgent)obj).ID);
	}
	
	public int hashCode() {
		return this.ID.hashCode();
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	//  Subscriber Methods
	////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Used to notify the remote client
	 * @param event Event that the Subscriber will be receiving from the server
	 */
	public void notify(Event event) throws RemoteException {
		System.out.println("*Notification of received event*");
		System.out.print(event);
		System.out.print("> ");
		recvdEvents.add(event);
	}
	/**
	 * This method contacts the server to subscribe this agent to the given topic
	 * @param topic Topic to subscribe to
	 */
	public void subscribe(final Topic topic) {
		
		new Thread(new Runnable() {
			public void run() {
				int tries = 0;
				while(++tries < MAX_TRIES) {
					try {
						if (server.addSubscriber(PubSubAgent.this.ID, topic))
							subscrTopics.add(topic);
						return;
					} catch(RemoteException e) {
						if (tries == 1)
							System.err.println("Server currently unavailable. Will continue to process request in background.");
						try { Thread.sleep(TIMEOUT); } catch(Exception f) {}
					}
				}
				System.err.println("Could not contact server to subscribe to Topic "+topic.getName()+". Please try again later.");
			}
		}).start(); 
	}

	/**
	 * This method contacts the server to unsubscribe this agent from the given topic
	 * @param topic Topic to unsubscribe from
	 */
	public void unsubscribe(final Topic topic) {
		
		new Thread(new Runnable() {
			public void run() {
				int tries = 0;
				while(++tries < MAX_TRIES) {
					try {
						if (server.removeSubscriber(PubSubAgent.this.ID, topic))
							subscrTopics.remove(topic);
						return;
					} catch(RemoteException e) {
						if (tries == 1)
							System.err.println("Server currently unavailable. Will continue to process request in background.");
						try { Thread.sleep(TIMEOUT); } catch(Exception f) {}
					}
				}
				System.err.println("Could not contact server to unsubscribe from "+topic.getName()+". Please try again later.");
			}
		}).start(); 
	}

	////////////////////////////////////////////////////////////////////////////////////
	//  Publisher Methods
	////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ASynchronously publishes this event to all subscribers on the server
	 */
	public void publish(final Event event) {
		if (event == null)
			return;
		
		new Thread(new Runnable() {
			public void run() {
				int tries = 0;
				while(++tries < MAX_TRIES) {
					try {
						int uniqueID = server.publish(event);
						if (uniqueID != 0)
							myPubEvents.add( event.setID(uniqueID) );
						return;
					} catch(RemoteException e) {
						if (tries == 1) 
							System.err.println("Server currently unavailable. Will continue to process request in background.");
						try { Thread.sleep(TIMEOUT); } catch(Exception f) {}
					}
				}
				System.err.println("Could not contact server for Event "+event.getContent()+" creation. Please try again later.");
			}
		}).start();
	}
	
	/**
	 * Helper method to make an event from a Scanner (takes user input)
	 * 
	 * @param in Scanner taking input from the console
 	 * @return Event created
	 */
	public Event makeEvent(Scanner in) {
		Topic t = null;
		if ( (t = findTopic(in)) != null) {
			System.out.println("Enter event title:");
			String content = in.nextLine().trim();
			return new Event(t, content);
		}
		return null;
	}

	/**
	 * The Publisher advertises a new topic on the server
	 * 
	 * @param newTopic Topic to be advertised
	 */
	public void create(final Topic newTopic) {

		new Thread(new Runnable() {
			public void run() {
				int tries = 0;
				while(++tries < MAX_TRIES) {
					try {
						int uniqueID = server.addTopic(newTopic);
						if (uniqueID != 0)
							myPubTopics.add(newTopic.setID(uniqueID));
						else
							System.err.println("Topic already exists on server");
						return;
					} catch(RemoteException e) {
						if (tries == 1) 
							System.err.println("Server currently unavailable. Will continue to process request in background.");
						try { Thread.sleep(TIMEOUT); } catch(Exception f) {}
					}
				}
				System.err.println("Could not contact server for Topic "+newTopic.getName()+" creation. Please try again later.");
			}
		}).start();
	}
	
	/**
	 * Helper method to create a Topic from user input
	 * @param in Scanner to take user input from console
	 * @return Topic created
	 */
	public Topic makeTopic(Scanner in) {
		System.out.println("Enter topic name:");
		String name = in.nextLine().trim();
		
		return new Topic(name);
	}
	
	//////////////////////////////////////////////////////////////
	// Main-thread execution of user interface
	//////////////////////////////////////////////////////////////
	
	public void commandLineInterface() throws RemoteException {
		Scanner in = new Scanner(System.in);
		boolean continueExec = true;
		while (continueExec) {
			System.out.println("Client actions [1-4]:");
			System.out.println(" 1: Create a topic");
			System.out.println(" 2: Publish an event");
			System.out.println(" 3: Subscribe to a topic");
			System.out.println(" 4: Unsubscribe from a topic");
			System.out.println(" 5: Go back");
			System.out.print(": ");
			int choice = -1;

			try {
				choice = in.nextInt(); 
				in.nextLine();
			} catch (Exception e) { 
				in.nextLine(); 
			}

			Topic t = null;
			switch (choice) {
				case 1: 
					create(makeTopic(in)); 
					break;
				case 2: 
					publish(makeEvent(in)); 
					break;
				case 3: 
					if ( (t = findTopic(in)) != null)
						subscribe(t);
					break;
				case 4:
					if ( (t = findTopic(in)) != null)
						unsubscribe(t);
					break;
				case 5: 
					continueExec = false; 
					break;
				default: 
					System.out.println("Input not recognized");
			}
		}
	}
	
	private Topic findTopic(Scanner in) {
		System.out.println("Enter the name or ID number of the Topic:"); 
		String input = in.nextLine().trim();
		List<Topic> fromServer;
		try {
			fromServer = server.getTopics();
		} catch (RemoteException f) { 
			System.out.println("Cannot contact server. Try again later"); 
			return null;
		}
		try {
			int findID = Integer.parseInt(input);
			for (Topic t : fromServer ) 
				if (t.getID() == findID)
					return t;
			System.out.println("Topic not found.");
			return null;
		} catch (NumberFormatException e) {
			for (Topic t : fromServer ) 
				if (t.getName().equalsIgnoreCase( input ))
					return t;
			System.out.println("Topic not found.");
			return null;
		} 
	}
}