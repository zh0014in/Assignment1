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
	public int informationSeqNumber;
	
	public Player() {}

	public Player(int sequenceNumber,String name, String ip, int port) {
		this.sequenceNumber = sequenceNumber;
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.score = 0;
		this.informationSeqNumber = 0;
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
	
	public void setScore(int score){
		this.score = score;
	}
	
	
	public void findTreasure() {
		this.score++;
	}

	
	public String toStr(){
		return "" + this.sequenceNumber + "," + this.name + "," + this.ip + ","+ this.port + ","+ this.score + "," + this.informationSeqNumber;
	}
	
	public static Player fromString(String playerInfo){
		String[] info = playerInfo.split(",");
		Player player = new Player(Integer.parseInt(info[0]), info[1], info[2], Integer.parseInt(info[3]));
		player.setScore(Integer.parseInt(info[4]));
		player.informationSeqNumber = Integer.parseInt(info[5]);
		return player;
	}
}
