package MazeGame.socket;

import java.io.*;
import java.net.*;

public class Game implements ServerEventListener {
	Socket playerClientSkt2Tracker;
	public static PrintWriter out;
	private ServerThread serverThread;
	private GameThread gameThread;
	private Player localPlayer;
	private int n;
	private int k;

	private String name = "";
	
	public static void main(String args[]) {
		try {
			String name = "A0";
			Game game = new Game(name);
			game.begin();
		} catch (Exception e) {
			System.out.println("Game Crashed!");
		}
	}

	public Game(String name) {
		serverThread = new ServerThread();
		this.name = name;
		this.localPlayer = null;
		serverThread.setServerEventListener(this);
	}

	public void begin() {
		// ------------------------------------------------------------------//
		// players own server socket in case it need to be the Player Server
		serverThread.start();
		// -------------------------------------------------------------------//
	}

	@Override
	public void onServerSocketCreatedEvent() {
		int portNumber = serverThread.getPortNumber();
		try {
			playerClientSkt2Tracker = new Socket("localhost", 8000);
			out = new PrintWriter(playerClientSkt2Tracker.getOutputStream(), true);
			// send its own port number to Tracker
			String message = this.name + "-" + portNumber;
			out.println(message);
			// receive information from Tracker
			BufferedReader in = new BufferedReader(new InputStreamReader(playerClientSkt2Tracker.getInputStream()));
			while (!in.ready()) {
			}
			// this information contains all registered players which might
			// already crashed or exit the game
			String trackerMessage = in.readLine();
			System.out.println("Client received message from tracker: " + trackerMessage);
			// close connection with Tracker after received all information

			String[] trackerMessages = trackerMessage.split(";");
			// maze parameters: n and k
			String[] mazeParameters = trackerMessages[0].split("-");
			n = Integer.parseInt(mazeParameters[0]);
			k = Integer.parseInt(mazeParameters[1]);
			System.out.println("Maze Size: " + n + " Treasure Number : " + k);

			int currentExistingPlayers = trackerMessages.length - 1;
			System.out.println("Total Players Number: " + currentExistingPlayers);

			Player[] playerList = parseTrackerInfo(n, k, trackerMessages);

			this.localPlayer = playerList[currentExistingPlayers - 1];

			// After the primary and backup server is up, create the local Game
			try {
				ClientThread c2s = new ClientThread(playerList, this.localPlayer);
				c2s.setServerEventListener(this);
				c2s.start();

				gameThread = new GameThread(n, k);
				gameThread.start();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Player[] parseTrackerInfo(int n, int k, String[] trackerMessages) {
		Player[] playerList = new Player[n * n + 500];
		// start from i=1 because the first message is not a player info, i=0 is
		// n and k
		for (int i = 1; i < trackerMessages.length; i++) {
			String[] singlePlayerParameters = trackerMessages[i].split("-");
			int playerSequenceNumber = Integer.parseInt(singlePlayerParameters[0]);
			String playerName = singlePlayerParameters[1];
			String playerIP = singlePlayerParameters[2];
			int playerPort = Integer.parseInt(singlePlayerParameters[3]);
			playerList[i - 1] = new Player(playerSequenceNumber, playerName, playerIP, playerPort);
			System.out.println(
					"Player Info: " + playerSequenceNumber + " " + playerName + " " + playerIP + " " + playerPort);
		}
		return playerList;
	}

	@Override
	public void onPrimaryServerUpEvent() {
		// pass maze to server thread
		System.out.println("Primary server is up");
		this.serverThread.initializeMaze(n, k);
	}

	@Override
	public void onPrimaryServerFoundEvent(DataOutputStream out) {
		gameThread.setOutputStream(out);
	}

	@Override
	public void onMazeStringReceived(String msg) {
		try {
			gameThread.updateMaze(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}