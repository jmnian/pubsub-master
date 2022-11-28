package pubsub;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;

import java.util.Scanner;

public class PubSubClient {

    private int id = 0;
	private EventManager manager;
    
    public PubSubClient(int id, int port) {
		this.id = id;

    	try {
    		String hostName = InetAddress.getLocalHost().getHostAddress();
			this.manager = (EventManager) Naming.lookup("//" + hostName + ":" + port + "/EventManager");
			System.out.println("Connected to server at " + port);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
    }
    
	public static void main(String[] args) throws RemoteException {
		int id = -1;
     	int port = 1099;

		for (int i = 0; i < args.length; i ++) {	
			if (args[i].equals("-p")) 
				port = Integer.parseInt(args[++i]);
			if (args[i].equals("-id")) 
				id = Integer.parseInt(args[++i]);
			else {
				System.out.println("Correct usage: java PubSubClient [-p <portnumber>] [-id <idnumber>]");
				System.exit(1);
			}
		}

		if(id == -1) {
			System.out.println("Each client should have an unique ID");
			System.exit(1);
		}

		PubSubClient client = new PubSubClient(id, port);
		client.manager.addClient(id, client);
		client.commandLineInterface();
	}

	
	
	private void subscribe(String topic) throws RemoteException {
		boolean res = this.manager.subscribe(topic, id); 

		if(!res) {
			System.out.println("Topic doesn't exist");
		}
	}

	private void unsubscribe(String topic) throws RemoteException {
		boolean res = this.manager.unSubscribe(topic, id); 

		if(!res) {
			System.out.println("Topic doesn't exist");
		}
	}

	private void publish(String topic, String msg) throws RemoteException {
		boolean res = this.manager.publish(topic, msg); 

		if(!res) {
			System.out.println("Topic doesn't exist");
		}
	}
	
	private void createTopic(String topic) throws RemoteException {
		this.manager.createTopic(topic);
	}
	
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

			switch (choice) {
				case 1: 
					System.out.println("Enter topic name:");
					createTopic(in.nextLine().trim()); 
					break;
				case 2: 
					System.out.println("Enter topic name:"); 
					String topic = in.nextLine().trim();
					System.out.println("Enter the message:"); 
					publish(topic, in.nextLine().trim()); 
					break;
				case 3: 
					System.out.println("Enter topic name:"); 
					subscribe(in.nextLine().trim());
					break;
				case 4:
					System.out.println("Enter topic name:"); 
					unsubscribe(in.nextLine().trim());
					break;
				case 5: 
					continueExec = false; 
					break;
				default: 
					System.out.println("Input not recognized");
			}
		}
	}

	public void notify(String topic, String msg) {
		System.out.println();
		System.out.println("Message: " + msg);
		System.out.println("Published under Topic: " + topic);
		System.out.println();
	}
}
