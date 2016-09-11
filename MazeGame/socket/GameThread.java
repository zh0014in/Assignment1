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
	private Socket conn2Server;
	private DataOutputStream out;
	public GameThread() {}

	public GameThread(Maze maze,Player localPlayer) throws Exception {
		this.maze = maze;
		this.localPlayer = localPlayer;
	}
	
	public void setOutputStream(DataOutputStream out){
		this.out = out;
	}

	public void run() {
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
