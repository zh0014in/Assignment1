package MazeGame.socket;

import java.io.*;
import java.net.*;
import java.util.Random;

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
//			Random rn = new Random();
//			String name = "A" + rn.nextInt(100);
//			Game game = new Game(name);

			 String name = args[2];
			 String ip = args[0];
			 int port = Integer.parseInt(args[1]);
			 Game game = new Game(ip, port, name);

			game.begin();
		} catch (Exception e) {
			System.out.println("Game Crashed!");
		}
	}

	public Game(String name) {
		serverThread = new ServerThread();
		gameThread = new GameThread(name);
		this.name = name;
		this.localPlayer = null;
		serverThread.setServerEventListener(this);
	}

	public Game(String ip, int port, String name) {
		serverThread = new ServerThread();
		gameThread = new GameThread(name);
		this.name = name;
		this.localPlayer = null;
		serverThread.setServerEventListener(this);
	}

	public void begin() {
		// ------------------------------------------------------------------//
		// players own server socket in case it need to be the Player Server
		try {
			serverThread.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
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

			serverThread.initializeMaze(n, k);
			gameThread.setMazeSize(n);
			gameThread.start();

			int currentExistingPlayers = trackerMessages.length - 1;
			System.out.println("Total Players Number: " + currentExistingPlayers);

			Player[] playerList = parseTrackerInfo(n, k, trackerMessages);

			this.localPlayer = playerList[currentExistingPlayers - 1];

			// After the primary and backup server is up, create the local Game
			ClientThread clientThread = new ClientThread(playerList, this.localPlayer);
			clientThread.setServerEventListener(this);
			clientThread.start();

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
//		this.serverThread.initializeMaze(n, k);
		this.gameThread.MarkasPrimaryServer();
	}

	@Override
	public void onPrimaryServerFoundEvent(DataOutputStream out) {
		if (gameThread != null) {
			gameThread.setOutputStream(out);
		}
	}

	@Override
	public void onMazeStringReceived(String msg) {
		try {
			if (gameThread != null) {
				gameThread.updateMaze(msg);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onBackupServerUpEvent() {
		// TODO Auto-generated method stub
		this.gameThread.MarkasBackupServer();
	}
}