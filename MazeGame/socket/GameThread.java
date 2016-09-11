package MazeGame.socket;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class GameThread extends Thread {
	private Maze maze;
	private Player localPlayer;
	private Socket conn;
	private Player[] playerList;
	private Socket conn2Server;
	public GameThread() {}

	public GameThread(int mazeSize, int treasureCount, Player localPlayer,  Player[] playerList) throws Exception {
		this.maze = new Maze(mazeSize, treasureCount);
		this.localPlayer = localPlayer;
		this.playerList = playerList;
	}

	public void run() {
//		Connect2Server c2s = new Connect2Server(this.playerList, this.localPlayer);
//		c2s.start();
		
		createAndShowGUI();
		// Question: we need to get server approve you can move the we can move right?
		maze.JoinGame(this.localPlayer);
		boolean looping = true;
		Scanner command = new Scanner(System.in);
		while (looping) {
			System.out.println(
					"Enter value from these numbers: 0(refresh), 1(west), 2(south), 3(east), 4(north), 9(exit).");
			int newCommand = command.nextInt();
			switch (newCommand) {
			case 9:
				// TODO: tell server player exit game
				looping = false;
				break;
			case 0:
				// refresh position, ask server for update
				break;
			case 1:
				maze.MoveWest(this.localPlayer);
				break;
			case 2:
				maze.MoveSouth(this.localPlayer);
				break;
			case 3:
				maze.MoveEast(this.localPlayer);
				break;
			case 4:
				maze.MoveNorth(this.localPlayer);
				break;
			default:
				System.out.println("Invalid input: " + newCommand);
				break;
			}
		}
		System.out.println("Game END!!!");
		System.exit(0);
	}
	
	private void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(new JLabel("Enter value from these numbers: 0(refresh), 1(west), 2(south), 3(east), 4(north), 9(exit)."), BorderLayout.NORTH);
		frame.add(maze, BorderLayout.CENTER);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}
}

//class Connect2Server extends Thread{
//	BufferedReader inFromServer;
//	private Player localPlayer;
//	private Player[] playerList;
//	private Socket conn2Server;
//	
//	public Connect2Server(Player[] playerList, Player localPlayer){
//		this.localPlayer = localPlayer;
//		this.playerList = playerList;
//	}
//	
//	public void run(){
//		while(true){
//			try {
//				// find the actual primary server
//				Player primaryServer = null;
//				int primaryServeIndex = 0;
//				for(int i=0; i<playerList.length; i++){
//					primaryServer = playerList[i];
//					try {
//						System.out.println("LocalPlayer try to connect to " + primaryServer.getName() + " " + primaryServer.getIp() + " " + primaryServer.getPort());
//						this.conn2Server = new Socket(primaryServer.getIp(), primaryServer.getPort());
//						System.out.println("LocalPlayer find primary server: " + primaryServer.getName() + " " + primaryServer.getIp() + " " + primaryServer.getPort());
//						primaryServeIndex = i;
//						break;
//					} catch (Exception e) {
//						ServerThread.removePlayer(primaryServer);
//						System.out.println("Connect to " + primaryServer.getName() + " failed! Try next one!");
//					}
//				}
//
//				
//				// Send local player to server
//				DataOutputStream out = new DataOutputStream(this.conn2Server.getOutputStream());
//		        out.writeBytes(this.localPlayer.toStr() + "\n");
//		        out.flush();
//		        
//		        inFromServer = new BufferedReader(new InputStreamReader(this.conn2Server.getInputStream()));
//		        try{
//			        while(true){
//			        	// receive the full list of players
//			        	String msg = inFromServer.readLine();
//			        	String[] msgToken = msg.split(";");
//			        	
//			        	if (msgToken[0].equals("BK")){
//			        		System.out.println("BackupServer received backup info: " + msg);
////			        		if (msgToken[1].equals("IF")){
////			        			for(int i=2; i<msgToken.length; i++){
////			        				Player tmp = new Player(msgToken[i]);
////			        				if(ServerThread.addNewPlayer(tmp)){
////			        					System.out.println("==>Player "+ msgToken[i] +" added in list");
////			        				}
////			        				else
////			        					System.out.println("Player "+ msgToken[i] +" already in list");
////			        			}
////			        		}
//			        		// if = MZ then back up maze info
//			        	}
//			        	else if (msgToken[0].equals("IF")){
//			        		System.out.println("LocalPlayer receive full list of current players: " + msg);
//			        	}
//			        	else if (msgToken[0] == "MZ"){
//			        		System.out.println("LocalPlayer receive maze info: " + msg);
//			        	}
//			        	else
//			    			System.out.println("Unkown message received: " + msg);
//			        }
//		        } catch (IOException e) {
//					// TODO Auto-generated catch block
//		        	ServerThread.removePlayer(primaryServer);
//					System.out.println("Primary Server down!");
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//}
