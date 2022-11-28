package pubsub;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class EventServer {

	private int port = 1099;
	private String hostName = "";
	private EventManager manager = null;
	
	public EventServer(String[] args) {
		if(args.length > 0) parseArgs(args);
    		
    	try {
    		hostName = InetAddress.getLocalHost().getHostAddress();
    		manager = new EventManager();
    		Naming.rebind("//" + hostName + ":" + port + "/EventManager", manager);

            System.out.println("EventManager bound in registry at " + hostName + ":" + port);
		} catch (Exception e) {
			System.out.println( "EventManager error");
			System.out.println( "Did you run 'rmiregistry [port] &' first?" );
			System.exit(1);
		}
	}
	
	private void parseArgs(String args[]) {
		for(int i = 0; i < args.length; i ++) {	
			if(args[i].equals("-p")) {
				port = Integer.parseInt(args[++i]);
			} else {
				System.out.println("Correct usage: java EventServer [-p <portnumber>]");
				System.exit(1);
			}
		}
	}

	public static void main(String[] args) throws RemoteException {
		EventServer server = new EventServer(args);
		server.manager.startService();
		server.manager.commandLineInterface();
	}
}
