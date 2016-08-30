package MazeGame.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class GameThread extends Thread {
	private int portNumber;

	public GameThread() {

	}

	public GameThread(int portNumber) {
		this.portNumber = portNumber;
	}

	public void run() {

		// players socket client to talk to Tracker
		Socket playerClientSkt2Tracker;
		try {
			playerClientSkt2Tracker = new Socket("localhost", 8000);

			PrintWriter out = new PrintWriter(playerClientSkt2Tracker.getOutputStream(), true);
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
			System.out.println("Client received message: " + trackerMessage);
			// close connection with Tracker after received all information
			playerClientSkt2Tracker.close();

			String[] trackerMessages = trackerMessage.split(";");
			// maze parameters: n and k
			String[] mazeParameters = trackerMessages[0].split(",");
			int n = Integer.parseInt(mazeParameters[0]);
			int k = Integer.parseInt(mazeParameters[1]);
			System.out.println("Maze Size: " + n + " Treasure Number : " + k);

			// players basic information list received from Tracker (player
			// name, sequence number, ip, port)
			Player[] playerList = new Player[n * n + 500];
			for (int i = 1; i < trackerMessages.length; i++) {
				String[] singlePlayerParameters = trackerMessages[i].split(",");
				String playerName = singlePlayerParameters[0];
				int playerSequenceNumber = Integer.parseInt(singlePlayerParameters[1]);
				String playerIP = singlePlayerParameters[2];
				int playerPort = Integer.parseInt(singlePlayerParameters[3]);
				playerList[i - 1] = new Player(playerSequenceNumber, playerName, playerIP, playerPort, n, k);
				System.out.println(
						"Player Info: " + playerSequenceNumber + " " + playerName + " " + playerIP + " " + playerPort);
			}

			// local player always is the last one in the list Tracker Send to
			// us
			Player localPlayer = playerList[playerList.length - 1];

			// TODO : Setup GUI (use JAVAFX???)

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
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					break;
				default:
					System.out.println("Invalid input: " + newCommand);
					break;
				}
			}
			System.out.println("Game END!!!");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
