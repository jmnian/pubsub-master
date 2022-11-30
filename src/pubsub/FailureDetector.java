package pubsub;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import pubsub.interfaces.EventManager;

import java.util.List;
import java.util.Scanner;

public class FailureDetector extends EventServer{ 
    private Set<String> serverPool;
    private EventManager leader;
    private String leaderName;

    public FailureDetector() throws RemoteException{
        serverPool = new HashSet<>();
    }

    public void startRunning() throws Exception {
        while (true) {
            //iterate over set and look up from RMI registry
            //check each server is running
            List<String> list = new ArrayList<>(serverPool);
            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
                EventManager server = (EventManager) Naming.lookup(s);
                try {
                    server.getRegisteredName();
                } catch (RemoteException e) {
                    System.out.println("\nServer: " + s + " just went down");
                    list.remove(i);
                    serverPool.remove(s);
                    //if leader is down, chose a server and ask it to re-elect (bully)
                    if (serverPool.size() > 0 && s.equals(this.leaderName)) {
                        System.out.println(s + " was a leader");
                        EventManager pickedRandomServer = (EventManager) Naming.lookup(list.get(0));
                        pickedRandomServer.bully(this);
                        System.out.println("Re-election has finished");
                    } else if (serverPool.size() == 0) {
                        leader = null;
                        leaderName = "No Leader";
                    }
                }
            }
            Thread.sleep(1000);
        }
    }

    /*
     * commandline interface:
     * show all servers running
     * show leader
    */
    @Override
    public void commandLineInterface() throws RemoteException {
        // use a new thread to do command line interface 
        // because startRunning() will keep the main thread busy waiting 
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner input = new Scanner(System.in);
                while (true) {
                    System.out.println();
                    System.out.println("What would you like to do? Enter choice [1-2]:");
                    System.out.println(" 1: Show Cluster Info");
                    System.out.println(" 2: Quit");
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
                            System.out.println("Cluster Status ---> : " + serverPool.toString());
                            if (leader != null) {
                                System.out.println("Leader is --------> :  " + leaderName);
                            } else {
                                System.out.println("No Leader Yet");
                            }
                            break;
                        case 2: 
                            input.close(); 
                            System.exit(0); 
                        default: 
                            System.out.println("Input not recognized");
                    }
                }
            }
        });
        // Daemon allows this thread not to block program from exiting
        t.setDaemon(true);
        t.start();
	}


    //Helper functions
    public EventManager getLeader(EventManager sender) throws RemoteException {
        String senderName = sender.getRegisteredName();
        synchronized(serverPool) {
            this.serverPool.add(senderName);
        }
        if (this.leader == null) {
            this.leader = sender;
            this.leaderName = leader.getRegisteredName();
        }
        return this.leader;
    }

    public void setLeader(EventManager leader) throws RemoteException{
		this.leader = leader;
        this.leaderName = leader.getRegisteredName();
	}

    public Set<String> getServerPool() {
        return this.serverPool;
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

        FailureDetector fd = new FailureDetector();
        //register in RMI
        try {
			String hostName = "localhost";
			String name = "//" + hostName + ":" + port + "/FailureDetector";
    		Naming.rebind(name, fd);
			fd.setRegisteredName(name);
            System.out.println("FailureDetector bound in registry at " + hostName + ":" + port);

        } catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
        fd.commandLineInterface();
        fd.startRunning();
    }
}
