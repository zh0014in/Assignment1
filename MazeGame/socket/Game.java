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
		// TODO Auto-generated method stub
		int portNumber = serverThread.getPortNumber();
		GameThread gameThread = new GameThread(portNumber);
		gameThread.start();
	}
}

class Player {
	public int sequenceNumber = 0;
	public String name = "";
	public String ip = "";
	public int port = 0;
	public int score = 0;
	// let primary server decide the initial position
	public int x = 0;
	public int y = 0;
	// Should I put it here? or in the Game? I don't think I should put it here
	public Player[] knownPlayers;
	// need this if it is primary or backup server
	public String namePrefix = "";

	public Player(int sequenceNumber, String name, String ip, int port, int n, int k) {
		this.sequenceNumber = sequenceNumber;
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.score = 0;
	}
}
