package pubsub;

import pubsub.interfaces.EventManInterface;
import pubsub.interfaces.Subscriber;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class EventManagerMain extends UnicastRemoteObject implements EventManInterface {

	private static final long serialVersionUID = 1L;
	protected HashMap<Integer, Integer> managerMap;
	protected EventManInterface leader;

	public EventManagerMain(boolean preload) throws RemoteException {
		managerMap = new HashMap<>();
	}

	public HashMap<Integer, Integer> getManagerMap() {
		return managerMap;
	}

	public void setManagerMap(HashMap<Integer, Integer> managerMap) {
		this.managerMap = managerMap;
	}

	public int sayHello(Subscriber sub) throws RemoteException {
		return leader.sayHello(sub);
	}

	public int sayHello(Integer ID, Subscriber sub) throws RemoteException {
		return leader.sayHello(ID, sub);
	}

	public void unbind(Integer ID) throws RemoteException{
		leader.unbind(ID);
	}

	public void unbindPermanent(Integer ID) throws RemoteException{
		leader.unbindPermanent(ID);
	}
	
	public Subscriber getSubscriber(Integer ID)  throws RemoteException{
		return leader.getSubscriber(ID);
	}

	public void startService(String hostName) throws Exception {
		while (true) {
			int max = 0;
			for (Integer p : managerMap.keySet()) {
				EventManInterface m = (EventManInterface) Naming.lookup("//" + hostName + ":" + p + "/EventManager");
				try {
					m.getTopics();
					if(p > max) {
						max = p;
						leader = m;
					}
					managerMap.put(p, 1);
				} catch (RemoteException e) {
					managerMap.put(p, 0);
					System.out.println("inactive manager ---> : " + p);
				}
			}
			System.out.println("cluster status ---> : " + managerMap.entrySet());
			System.out.println("leader is ---> : " + max);
			Thread.sleep(1000 * 10);
		}
	}

	
	////////////////////////////////////////////////////////////////////////////////////
	//  Publisher services 
	////////////////////////////////////////////////////////////////////////////////////

	public int publish(Event event) throws RemoteException {
		return leader.publish(event);
	}

	public int addTopic(Topic topic) throws RemoteException {
		return leader.addTopic(topic);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	//  Subscriber services 
	////////////////////////////////////////////////////////////////////////////////////

	public boolean addSubscriber(Integer subID, Topic topic) throws RemoteException {
		return leader.addSubscriber(subID, topic);
	}

	public boolean removeSubscriber(Integer subID, Topic topic) throws RemoteException {
		return leader.addSubscriber(subID, topic);
	}

	public ArrayList<Topic> getTopics() throws RemoteException {
		return leader.getTopics();
	}

	public void commandLineInterface() throws RemoteException {
		Scanner in = new Scanner(System.in);
		do {
			System.out.println("What would you like to do? Enter choice [1-3]:");
			System.out.println(" 1: Show topics");
			System.out.println(" 2: Show subscribers");
			System.out.println(" 3: Quit server");
			System.out.print("> ");
			int choice = -1;
			try {
				choice = in.nextInt(); in.nextLine();
			} catch (Exception e) { in.nextLine(); }
			switch (choice) {
				case 1: getTopics();break;
				case 2: showSubscribers(); break;
				case 3: in.close(); System.exit(0); 
				default: System.out.println("Input not recognized");
			}
		} while (true);
	}

	public void showSubscribers() throws RemoteException {
		leader.showSubscribers();
	}
}
