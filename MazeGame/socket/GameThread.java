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
import javax.swing.SwingUtilities;

public class GameThread extends Thread {
	private Maze maze;
	private Socket conn;
	private Socket conn2Server;
	private DataOutputStream out;
	private String name;
	private String initialTitle;
	private String title = "";
	private JFrame frame;

	public GameThread() {
	}

	public GameThread(String name){
		this.name = name;
		this.initialTitle = "Game-" + this.name;
	}
	
	public void setMazeSize(int n){
		this.maze = new Maze(n);
	}

	public void updateMaze(String msg) {
		try {
			maze.fromString(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setOutputStream(DataOutputStream out) {
		this.out = out;
	}

	public void run() {
		createAndShowGUI();
		boolean looping = true;
		Scanner command = new Scanner(System.in);

		while (looping) {
			System.out.println(
					"Enter value from these numbers: 0(refresh), 1(west), 2(south), 3(east), 4(north), 9(exit).");
			try {

				int newCommand = command.nextInt();

				if (this.out == null) {
					System.out.println("output stream not ready yet!");
					continue;
				}

				switch (newCommand) {
				case 9:
					// TODO: tell server player exit game
					looping = false;
					break;
				case 0:
					// refresh position, ask server for update
					break;
				case 1:
					out.writeChars("1\n");
					out.flush();
					break;
				case 2:
					out.writeChars("2\n");
					out.flush();
					break;
				case 3:
					out.writeChars("3\n");
					out.flush();
					break;
				case 4:
					out.writeChars("4\n");
					out.flush();
					break;
				default:
					System.out.println("Invalid input: " + newCommand);
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Game END!!!");
		System.exit(0);
	}

	private void createAndShowGUI() {
		// Create and set up the window.
		if (!this.title.isEmpty()) {
			frame = new JFrame(this.title);
		}
		else{
			frame = new JFrame(this.initialTitle);
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(
				new JLabel(
						"Enter value from these numbers: 0(refresh), 1(west), 2(south), 3(east), 4(north), 9(exit)."),
				BorderLayout.NORTH);
		frame.add(maze, BorderLayout.CENTER);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public void MarkasPrimaryServer() {
		if (this.frame == null) {
			this.title = this.initialTitle + "(primary)";
		}
		String title = frame.getTitle();
		if (!title.contains("primary")) {
			frame.setTitle(this.initialTitle + "(primary)");
		}
	}

	public void MarkasBackupServer() {
		if (this.frame == null) {
			this.title = this.initialTitle + "(backup)";
		}
		String title = frame.getTitle();
		if (!title.contains("backup")) {
			frame.setTitle(this.initialTitle + "(backup)");
		}
	}
}
