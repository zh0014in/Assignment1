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
		createAndShowGUI();
		// Question: we need to get server approve you can move the we can move right?
		maze.JoinGame(this.localPlayer);
		boolean looping = true;
		BufferedReader inFromServer;
		try {
			// find the actual primary server
			Player primaryServer = null;
			for(int i=0; i<playerList.length; i++){
				primaryServer = playerList[i];
				try {
					System.out.println("LocalPlayer try to connect to " + primaryServer.getName() + " " + primaryServer.getIp() + " " + primaryServer.getPort());
					this.conn2Server = new Socket(primaryServer.getIp(), primaryServer.getPort());
					System.out.println("LocalPlayer find primary server: " + primaryServer.getName() + " " + primaryServer.getIp() + " " + primaryServer.getPort());
					break;
				} catch (Exception e) {
					System.out.println("Connect to " + primaryServer.getName() + " failed! Try next one!");
				}
			}
			//send local player to server
//			ObjectOutputStream objectOutput = new ObjectOutputStream(this.conn2Server.getOutputStream());
			DataOutputStream out = new DataOutputStream(this.conn2Server.getOutputStream());
	        out.writeBytes(this.localPlayer.toStr() + "\n");
	        out.flush();
	        
	        inFromServer = new BufferedReader(new InputStreamReader(this.conn2Server.getInputStream()));
	        while(true){
	        	// receive the full list of players
	        	System.out.println("LocalPlayer wait for information from server.");
	        	while (!inFromServer.ready()) {}
	        	String fullListPlayersString = inFromServer.readLine();
	        	System.out.println("LocalPlayer receive full list of current players: " + fullListPlayersString);
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
