package cs5223.assignment1.tracker;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tracker implements ITracker {
	private static int portNumber;
	private static int gridSize;
	private static int treasureCount;
	private static List<Player> players;

	public Tracker() {

	}

	public Tracker(int portNumber, int gridSize, int tresureCount) {
		this.portNumber = portNumber;
		this.gridSize = gridSize;
		this.treasureCount = treasureCount;
		this.players = new ArrayList<Player>();
	}

	public static void main(String args[]) {
		ITracker stub = null;
		Registry registry = null;
		try {
			int portNumber = Integer.parseInt(args[0]);
			int gridSize = Integer.parseInt(args[1]);
			int treasureCount = Integer.parseInt(args[2]);
			Tracker tracker = new Tracker(portNumber, gridSize, treasureCount);
			stub = (ITracker) UnicastRemoteObject.exportObject(tracker, 0);
			registry = LocateRegistry.getRegistry();
			registry.bind("Tracker", stub);
			System.out.println("Tracker ready.");
		} catch (Exception e) {
			try {
				if (registry != null) {
					registry.unbind("Tracker");
					registry.bind("Tracker", stub);
					System.out.println("Tracker ready");
				}
			} catch (Exception ee) {
				System.err.println("Tracker exception: " + ee.toString());
				ee.printStackTrace();
			}
		}
	}

	@Override
	public Player getRandomPlayer(Player callingPlayer) throws RemoteException {
		synchronized (this.players){
			if(this.players.size() == 0){
				this.players.add(callingPlayer);
				return null;
			}else{
				Random rand = new Random();
				int indexOfPlayer = rand.nextInt(this.players.size());
				this.players.add(callingPlayer);
				return this.players.get(indexOfPlayer);
			}
		}
	}
}