package MazeGame.socket;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class GameThread extends Thread {
	private Maze maze;

	public GameThread() {

	}

	public GameThread(int mazeSize, int treasureCount) throws Exception {
		this.maze = new Maze(mazeSize, treasureCount);
	}

	public void run() {

		// players socket client to talk to Tracker

		// players basic information list received from Tracker (player
		// name, sequence number, ip, port)
		// Player[] playerList = new Player[n * n + 500];
		// for (int i = 1; i < trackerMessages.length; i++) {
		// String[] singlePlayerParameters = trackerMessages[i].split(",");
		// String playerName = singlePlayerParameters[0];
		// int playerSequenceNumber =
		// Integer.parseInt(singlePlayerParameters[1]);
		// String playerIP = singlePlayerParameters[2];
		// int playerPort = Integer.parseInt(singlePlayerParameters[3]);
		// playerList[i - 1] = new Player(playerSequenceNumber, playerName,
		// playerIP, playerPort);
		// System.out.println(
		// "Player Info: " + playerSequenceNumber + " " + playerName + " " +
		// playerIP + " " + playerPort);
		// }

		// local player always is the last one in the list Tracker Send to
		// us
		// Player localPlayer = playerList[playerList.length - 1];

		// TODO : Setup GUI (use JAVAFX???)
		createAndShowGUI();
		Player player = new Player(0, "ZZ", "127.0.0.1",1234);
		maze.JoinGame(player);
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
				maze.MoveWest(player);
				break;
			case 2:
				maze.MoveSouth(player);
				break;
			case 3:
				maze.MoveEast(player);
				break;
			case 4:
				maze.MoveNorth(player);
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
