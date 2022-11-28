package pubsub;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;

public class EventServer {

	private EventManager manager = null;
	
	public EventServer(int port) {
    	try {
    		String hostName = InetAddress.getLocalHost().getHostAddress();
    		manager = new EventManager(port);
    		Naming.rebind("//" + hostName + ":" + port + "/EventManager", manager);

            System.out.println("EventManager bound in registry at " + hostName + ":" + port);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
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
		server.commandLineInterface();
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
					// for (TopicContainer tc : allTopicContainers)
					// 	System.out.print( tc.getTopic() );
					break;
				case 2: 
					// showSubscribers(); 
					break;
				case 3: 
					input.close(); 
					System.exit(0); 
				default: 
					System.out.println("Input not recognized");
			}
		}
	}
	
}
