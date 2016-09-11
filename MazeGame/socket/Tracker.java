package MazeGame.socket;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;

class Tracker {
	public static int seqID = 0;
	public static String[] playerParameters = new String[200];
	public static List<String> playerParametersList;
	
	public static void main(String args[]) {
		try {
			int port = 8000; //Integer.parseInt(args[0]);
			int N = 10; //Integer.parseInt(args[1]);
			int K = 2; //Integer.parseInt(args[2]);
			int count = 0;
			Socket socket;
			Socket clientSocket = null;
			ServerSocket serverSocket = new ServerSocket(port);
			while (true) {
				try {
					socket = serverSocket.accept();
					
					String playerName = "Player" + count;
					String playerIP = socket.getInetAddress().toString().replace("/", "");
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
			        while (!in.ready()) {}
			        String playerPort = in.readLine();
			        
			        playerParameters[count] = playerName + "," + count + "," + playerIP + "," + playerPort;
			        count += 1;
			        
			        String[] newPlayerParameters = Arrays.copyOfRange(playerParameters, 0, count);

			        String playersList = String.join(";", newPlayerParameters);
			        
					String message = ""+ N + "," + K + ";" + playersList;
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					out.println(message);
					
			        socket.close();
//					if (seqID >= 200) {
//						System.out.println("Players Number beyond the limit 200");
//					}
//					else {
//						clientSocket = serverSocket.accept();						
//						String playerNum = "Player " + seqID;
//						String playerIP = clientSocket.getInetAddress().toString().replace("/", "");
//						System.out.println("Player Num: " + playerNum);
//						System.out.println("PlayerIP: " + playerIP);
//						
//						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//						
//				        while (!in.ready()) {}
//				        String clientReq = in.readLine();
//				        System.out.println("Server received message: " + clientReq);
//				        //getPlayers(clientReq);
//				        playerParameters[seqID] = seqID + "," + clientReq;
//				        seqID += 1;
//				        
//				        String[] newPlayerParameters = Arrays.copyOfRange(playerParameters, 0, seqID);
//				        String playersList = String.join(";", newPlayerParameters);
//				        System.out.println("--" + playersList);
//				        String message = ""+ N + "," + K + ";" + playersList;
//						PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//						out.println(message);
//						clientSocket.close();
//					}
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
	
	// TODO, may need to handle the update from Primary Server
	private static String getPlayers(String clientReq) {
		String output = "";
		return output;
	}
}	// Class Tracker
