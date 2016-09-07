package MazeGame.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {
	// this is the master server thread
	public static int portNumber = 0;
	public boolean isPrimary = false;
	public boolean isBackup = false;
	private DataOutputStream out2Backup = null;
	private ServerEventListener listener;
	private Ping ping;
	
	public static ArrayList<Player> playerList = new ArrayList<Player>();
	public static Player backupServer = null;
	public ServerThread(){}
	
	public int getPortNumber() {
		return this.portNumber;
	}
	
	public void setServerEventListener (ServerEventListener listener) {
	    this.listener = listener;
	}

	public void run() {
		
		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			portNumber = serverSocket.getLocalPort();
			if(listener != null){
				listener.onServerSocketCreatedEvent();
			}
			System.out.println("Server listening on port " + portNumber);
			ping = new Ping();
			ping.start();
			
			Socket clientSocket;
			while (true) {
				try {
					clientSocket = serverSocket.accept();
					String clientAddress = clientSocket.getRemoteSocketAddress().toString();
					int clientPort = clientSocket.getPort();
					System.out.println("Player " + clientAddress + ":" + clientPort + " accepted.");
					
					new PlayerThread(clientSocket).start();
					Thread.sleep(100);
					
					String fullListString = "";
					for(Player p : this.playerList){
						fullListString += p.toStr() + ";";
					}
					
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean findBackupServer(int portNumber){
		for(int i=0; i< ServerThread.playerList.size(); i++){
			if (ServerThread.playerList.get(i).getPort() == portNumber) {
				System.out.println("Start to find backup server. " + i);
				for(int j=i+1; j<ServerThread.playerList.size(); j++){
					backupServer = ServerThread.playerList.get(j);
					backupServer.isBackup = true;
					return true;
				}
			}
		}
		return false;
	}
	
	public static void sendMsgToBackUp(String msg){
		try{
        	ServerThread.backupServer.outToClient.writeBytes("BK;"+msg + "\n");
        	ServerThread.backupServer.outToClient.flush();
		} catch(Exception e){
			if(ServerThread.backupServer != null){
				ServerThread.playerList.remove(ServerThread.backupServer);
			}
			if(ServerThread.findBackupServer(ServerThread.portNumber)){
				try {
					ServerThread.backupServer.outToClient.writeBytes("BK;"+msg + "\n");
					ServerThread.backupServer.outToClient.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					return;
				}
        	}
		}
	}
}

class PlayerThread extends Thread{
	private Socket socket;
	private DataOutputStream outToClient;
	private BufferedReader inFromClient;
	private Player curPlayer;
	public PlayerThread(Socket clientSocket){
		this.socket = clientSocket;
	}
	public void run(){
		try {
			outToClient = new DataOutputStream(this.socket.getOutputStream());
			inFromClient = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			
	        String playerInfo = inFromClient.readLine();
	        Player remotePlayer = Player.fromString(playerInfo);
	        remotePlayer.connOnServer = this.socket;
            remotePlayer.outToClient = outToClient;
            curPlayer = remotePlayer;
	        ServerThread.playerList.add(remotePlayer);
            System.out.println("Player Added: "+ remotePlayer.toStr());
            String fullListString = "";
            
            for(Player p : ServerThread.playerList){
				fullListString += p.toStr() + ";";
			}
            outToClient.writeBytes(fullListString + "\n");
            outToClient.flush();
            
            ServerThread.sendMsgToBackUp(fullListString);
            
            while(true){
	        	String msg = inFromClient.readLine();
	        	System.out.println("Server receive  " + msg + "from " + curPlayer.getName());
    		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class Ping extends Thread{
	public Ping(){}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(5000);
				for(Player p: ServerThread.playerList){
					try{
						p.outToClient.writeBytes("Ping: " + p.getName() + "\n");
					}
					catch(IOException e){
						ServerThread.playerList.remove(p);
						String msg = "";
						for(Player temp : ServerThread.playerList){
							msg += temp.toStr() + ";";
						}
						ServerThread.sendMsgToBackUp(msg);
						System.out.println("Player "+ p.getName() + " ping failed!!!");
						break;
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}


