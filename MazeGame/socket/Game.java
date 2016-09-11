package MazeGame.socket;

import java.io.*;
import java.net.*;

public class Game implements ServerEventListener {
	Socket playerClientSkt2Tracker;
	public static PrintWriter out;
	private ServerThread serverThread;
	private Player localPlayer;
	
	public static void main(String args[]) {
		try {
			Game game = new Game();
			game.begin();
		} catch (Exception e) {
			System.out.println("Game Crashed!");
		}
	}

	public Game() {
		serverThread = new ServerThread();
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
			String message = "" + portNumber;
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
			String[] mazeParameters = trackerMessages[0].split(",");
			int n = Integer.parseInt(mazeParameters[0]);
			int k = Integer.parseInt(mazeParameters[1]);
			System.out.println("Maze Size: " + n + " Treasure Number : " + k);
			
			int currentExistingPlayers = trackerMessages.length -1;
			System.out.println("Total Players Number: " + currentExistingPlayers);
			
			if (currentExistingPlayers == 1){
				serverThread.isPrimary = true;
				System.out.println("============PRIMARY SERVER===============");
			}
//			if (currentExistingPlayers == 2)
//				serverThread.isBackup = true;
			
			Player[] playerList = parseTrackerInfo(n, k, trackerMessages);
			
			this.localPlayer = playerList[currentExistingPlayers-1];
			
			
			// After the primary and backup server is up, create the local Game
			try {
				GameThread gameThread = new GameThread(n,k, this.localPlayer, playerList);
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
		}
	}
	
	public Player[] parseTrackerInfo(int n, int k, String[] trackerMessages){
		Player[] playerList = new Player[n*n + 500];
		// start from i=1 because the first message is not a player info, i=0 is n and k
		 for (int i=1; i < trackerMessages.length; i++ ){
			 String[] singlePlayerParameters = trackerMessages[i].split(",");
			 String playerName = singlePlayerParameters[0];
			 int playerSequenceNumber = Integer.parseInt(singlePlayerParameters[1]);
			 String playerIP = singlePlayerParameters[2];
			 int playerPort = Integer.parseInt(singlePlayerParameters[3]);
			 playerList[i-1] = new Player(playerSequenceNumber, playerName, playerIP, playerPort);
			 System.out.println("Player Info: " + playerSequenceNumber + " " + playerName + " " + playerIP + " " + playerPort);
		 }
		 return playerList;
	}
}

