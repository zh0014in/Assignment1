package MazeGame.socket;

import java.io.DataOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class Player implements Serializable {
	public int sequenceNumber = 0;
	// need this if it is primary or backup server
	public String namePrefix = "";
	private String name;
	private String ip;
	private int port;
	private int score;
	public int informationSeqNumber;
	public Socket connOnServer = null;
	public boolean isBackup = false;
	public int command = -1;
	public DataOutputStream outToClient = null;
	
	public Player() {}

	public Player(int sequenceNumber,String name, String ip, int port) {
		this.sequenceNumber = sequenceNumber;
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.score = 0;
		this.informationSeqNumber = 0;
	}
	
	public Player(String input){
		fromString(input);
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
	
	public int getScore(){
		return this.score;
	}
	
	public void findTreasure() {
		this.score++;
	}

	
	public String toStr(){
		return "" + this.sequenceNumber + "-" + this.name + "-" + this.ip + "-"+ this.port + "-"+ this.score;// + "-" + this.informationSeqNumber;
	}
	
	public void fromString(String playerInfo){
		String[] info = playerInfo.split("-");
		this.sequenceNumber = Integer.parseInt(info[0]);
		this.name = info[1];
		this.ip = info[2];
		this.port = Integer.parseInt(info[3]);
		this.score = Integer.parseInt(info[4]);
//		this.informationSeqNumber = Integer.parseInt(info[5]);
	}
}
