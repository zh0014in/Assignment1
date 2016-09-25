package MazeGame.socket;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;

class Tracker {
	public static int seqID = 0;
	public static String[] playerParameters = new String[200];
	
	public static String playerInfo = "";
	public static int count = 0;
	public static void main(String args[]) {
		try {
			int port = 8000; //Integer.parseInt(args[0]);
			int N = 10; //Integer.parseInt(args[1]);
			int K = 10; //Integer.parseInt(args[2]);
			
//			int port = Integer.parseInt(args[0]);
//			port = 8000;
//			int N = Integer.parseInt(args[1]);
//			int K = Integer.parseInt(args[2]);
			
			Socket socket;
			ServerSocket serverSocket = new ServerSocket(port);
			while (true) {
				try {
					socket = serverSocket.accept();
					
					String playerIP = socket.getInetAddress().toString().replace("/", "");
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
			        String msg = in.readLine();
			        String[] playersToken = msg.split("-");
			        String playerName = playersToken[0];
			        String playerPort = playersToken[1];
			        playerInfo +=  count + "-" + playerName + "-" + playerIP + "-" + playerPort + ";";
			        System.out.println("==>Get new players infomation: " + count + "-" + playerName + "-" + playerIP + "-" + playerPort);
					String message = ""+ N + "-" + K + ";" + playerInfo;
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					out.println(message);
					count += 1;
					(new PlayerConnection(in)).start();
				}
				catch (IOException e) {
					System.out.println("I/O error: " + e);
					serverSocket.close();
				}
			}
		}
		catch(Exception e) {
			System.out.println("Tracker Crashed!");
		}
	}
	
}

class PlayerConnection extends Thread{
	BufferedReader in;
	public PlayerConnection(BufferedReader in){
		this.in= in;
	}
	
	public void run(){
		while(true){
	        String msg;
			try {
				msg = this.in.readLine();
				String[] playersToken = msg.split(";");
				if(playersToken[0].equals("IF")){
		        	Tracker.playerInfo = msg.substring(3);
		        	System.out.println("-->Get updated players infomation: " + Tracker.playerInfo);
		        }
			} catch (IOException e) {
				break;
			}
		}
	}
	
	
}













