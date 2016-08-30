package MazeGame.socket;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Game implements ServerEventListener {
	private ServerThread serverThread;

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
			
			try {
				GameThread gameThread = new GameThread(n,k);
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
}
