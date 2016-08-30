package MazeGame.socket;

import java.io.Serializable;

public class Player implements Serializable {
	public int sequenceNumber = 0;
	// need this if it is primary or backup server
	public String namePrefix = "";
	private String name;
	private String ip;
	private int port;
	private int score;

	public Player() {

	}

	public Player(int sequenceNumber,String name, String ip, int port) {
		this.sequenceNumber = sequenceNumber;
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.score = 0;
	}

	public String getName() {
		return name;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public void findTreasure() {
		this.score++;
	}
}