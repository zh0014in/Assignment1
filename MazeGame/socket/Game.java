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
				Connect2Server c2s = new Connect2Server(playerList, this.localPlayer);
				c2s.start();
				
				
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


class Connect2Server extends Thread{
	BufferedReader inFromServer;
	private Player localPlayer;
	private Player[] playerList;
	private Socket conn2Server;
	
	public Connect2Server(Player[] playerList, Player localPlayer){
		this.localPlayer = localPlayer;
		this.playerList = playerList;
	}
	
	public void run(){
		while(true){
			try {
				// find the actual primary server
				Player primaryServer = null;
				int primaryServeIndex = 0;
				for(int i=0; i<playerList.length; i++){
					primaryServer = playerList[i];
					try {
						System.out.println("LocalPlayer try to connect to " + primaryServer.getName() + " " + primaryServer.getIp() + " " + primaryServer.getPort());
						this.conn2Server = new Socket(primaryServer.getIp(), primaryServer.getPort());
						System.out.println("LocalPlayer find primary server: " + primaryServer.getName() + " " + primaryServer.getIp() + " " + primaryServer.getPort());
						primaryServeIndex = i;
						break;
					} catch (Exception e) {
						ServerThread.removePlayer(primaryServer);
						System.out.println("Connect to " + primaryServer.getName() + " failed! Try next one!");
					}
				}

				
				// Send local player to server
				DataOutputStream out = new DataOutputStream(this.conn2Server.getOutputStream());
		        out.writeBytes(this.localPlayer.toStr() + "\n");
		        out.flush();
		        
		        inFromServer = new BufferedReader(new InputStreamReader(this.conn2Server.getInputStream()));
		        try{
			        while(true){
			        	// receive the full list of players
			        	String msg = inFromServer.readLine();
			        	String[] msgToken = msg.split(";");
			        	
			        	if (msgToken[0].equals("BK")){
			        		System.out.println("BackupServer received backup info: " + msg);
//			        		if (msgToken[1].equals("IF")){
//			        			for(int i=2; i<msgToken.length; i++){
//			        				Player tmp = new Player(msgToken[i]);
//			        				if(ServerThread.addNewPlayer(tmp)){
//			        					System.out.println("==>Player "+ msgToken[i] +" added in list");
//			        				}
//			        				else
//			        					System.out.println("Player "+ msgToken[i] +" already in list");
//			        			}
//			        		}
			        		// if = MZ then back up maze info
			        	}
			        	else if (msgToken[0].equals("IF")){
			        		System.out.println("LocalPlayer receive full list of current players: " + msg);
			        	}
			        	else if (msgToken[0] == "MZ"){
			        		System.out.println("LocalPlayer receive maze info: " + msg);
			        	}
			        	else
			    			System.out.println("Unkown message received: " + msg);
			        }
		        } catch (IOException e) {
					// TODO Auto-generated catch block
		        	ServerThread.removePlayer(primaryServer);
					System.out.println("Primary Server down!");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}