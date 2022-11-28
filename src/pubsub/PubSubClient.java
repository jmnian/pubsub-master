package pubsub;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;

import pubsub.interfaces.Server;

public class PubSubClient {

	private String hostName = ""; 
    private int port = 1099;
    private PubSubAgent agent = null;
    
    /**
     * Constructor that makes a new agent or loads a previously saved one
     * 
     * @param args String[] from the command line
     */
    public PubSubClient(String[] args) {
    	if (args.length > 0)  
    		parseArgs(args);

    	try {
    		hostName = InetAddress.getLocalHost().getHostAddress();
			Server server = (Server) Naming.lookup("//" + hostName + ":" + port + "/EventManager");
			System.out.println("Connected to server at " + hostName + ":" + port );
			agent = new PubSubAgent(server);
		} catch (Exception e) {
			System.out.println("Cannot connect to the Event server at this time.  Please try again later.");
			System.exit(1);
		}
    }
    
	private void parseArgs(String args[]) {
		
		for (int i = 0; i < args.length; i ++) {	
			if (args[i].equals("-p")) 
				port = Integer.parseInt(args[++i]);
			else {
				System.out.println("Correct usage: java PubSubClient [-p <portnumber>]");
				System.exit(1);
			}
		}
	}
	
	public static void main(String[] args) throws RemoteException {
		PubSubClient client = new PubSubClient(args);
		client.agent.commandLineInterface();
	}
}
