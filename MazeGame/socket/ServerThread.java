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
	public static boolean isPrimary = false;
	public boolean isBackup = false;
	private DataOutputStream out2Backup = null;
	private ServerEventListener listener;
	protected static Maze maze;
//	private Ping ping;
	
	public static ArrayList<Player> playerList = new ArrayList<Player>();
	public static Player backupServer = null;
	public ServerThread(){}
	
	public int getPortNumber() {
		return this.portNumber;
	}
	
	public void setServerEventListener (ServerEventListener listener) {
	    this.listener = listener;
	}
	
	public void setMaze(Maze maze){
		this.maze = maze;
	}

	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			portNumber = serverSocket.getLocalPort();
			if(listener != null){
				listener.onServerSocketCreatedEvent();
			}
			System.out.println("Server listening on port " + portNumber);
//			ping = new Ping();
//			ping.start();
			
			Socket clientSocket;
			while (true) {
				try {
					clientSocket = serverSocket.accept();
					if(listener != null && !isPrimary){
						listener.onPrimaryServerUpEvent();
						isPrimary = true;
					}
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
				System.out.println("Try to find backup server." + portNumber);
				for(int j=i+1; j<ServerThread.playerList.size(); j++){
					backupServer = ServerThread.playerList.get(j);
					backupServer.isBackup = true;
					System.out.println("Find backupserver: " + backupServer.toStr());
					return true;
				}
			}
		}
		System.out.println("Can not find backupserver");
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
					if(ServerThread.backupServer.outToClient != null){
						ServerThread.backupServer.outToClient.writeBytes("BK;"+msg + "\n");
						ServerThread.backupServer.outToClient.flush();
					}
				} catch (IOException e1) {
					return;
				}
        	}
		}
	}
	
	public static boolean addNewPlayer(Player tmp){
		for(int i=0; i < ServerThread.playerList.size(); i++){
			Player curPlayer = ServerThread.playerList.get(i);
			if(curPlayer.sequenceNumber > tmp.sequenceNumber){
				ServerThread.playerList.add(i, tmp);
				System.out.println("Adding player: insert player: " + tmp.toStr() );
				return true;
			}
			
			if (curPlayer.sequenceNumber == tmp.sequenceNumber){
				curPlayer = tmp;
				System.out.println("Adding player: update old player: " + tmp.toStr() );
				return true;
			}
		}
		ServerThread.playerList.add(tmp);
		
		String fullListString = "";
        for(Player p : ServerThread.playerList){
			fullListString += p.toStr() + ";";
		}
        System.out.println("log: " + fullListString);
		return true;
	}
	
	public static void removePlayer(Player tmp){
		for(Player p: ServerThread.playerList){
			if(p.toStr().equals(tmp.toStr())){
				ServerThread.playerList.remove(p);
				System.out.println("Remove player "+p.toStr()+" from ServerThread");
				break;
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
	        Player remotePlayer = new Player(playerInfo);
	        remotePlayer.connOnServer = this.socket;
            remotePlayer.outToClient = outToClient;
            curPlayer = remotePlayer;
            
//			if(!ServerThread.playerList.contains(curPlayer)){
//				ServerThread.playerList.add(curPlayer);
//			}
//			
//	        ServerThread.playerList.add(remotePlayer);
            if(ServerThread.addNewPlayer(curPlayer)){
            	System.out.println("Player Added: "+ remotePlayer.toStr());
            }
            else{
            	System.out.println("Player exists! : "+ remotePlayer.toStr());
            }
            
            String fullListString = "IF;";
            for(Player p : ServerThread.playerList){
				fullListString += p.toStr() + ";";
			}
            
            ServerThread.sendMsgToBackUp(fullListString);
            
	        outToClient.writeBytes(fullListString + "\n");
	        outToClient.flush();
            
            while(true){
	        	String command = inFromClient.readLine().trim();
	        	System.out.println("Server receive  " + command + "from " + curPlayer.getName());
	        	int newCommand = Integer.parseInt(command);
				switch (newCommand) {
				case 9:
					break;
				case 0:
					// refresh position, ask server for update
					break;
				case 1:
					ServerThread.maze.MoveWest(curPlayer);
					break;
				case 2:
					ServerThread.maze.MoveSouth(curPlayer);
					break;
				case 3:
					ServerThread.maze.MoveEast(curPlayer);
					break;
				case 4:
					ServerThread.maze.MoveNorth(curPlayer);
					break;
				default:
					System.out.println("Invalid input: " + newCommand);
					break;
				}
				// send back the maze
				outToClient.writeChars(ServerThread.maze.toString());
				outToClient.flush();
    		}
		} catch (IOException e) {
			ServerThread.playerList.remove(curPlayer);
			if(curPlayer.toStr().equals(ServerThread.backupServer.toStr()))
				ServerThread.findBackupServer(ServerThread.portNumber);
			String fullListString = "IF;";
            for(Player p : ServerThread.playerList){
				fullListString += p.toStr() + ";";
			}
            ServerThread.sendMsgToBackUp(fullListString);
			System.out.println("Player"+ curPlayer.toStr() +"disconnect");
		}
	}
}

//class Ping extends Thread{
//	public Ping(){}
//	
//	public void run(){
//		while(true){
//			try {
//				Thread.sleep(5000);
//				for(Player p: ServerThread.playerList){
//					try{
//						p.outToClient.writeBytes("PI;" + p.getName() + "\n");
//					}
//					catch(IOException e){
//						ServerThread.playerList.remove(p);
//						String msg = "IF;";
//						for(Player temp : ServerThread.playerList){
//							msg += temp.toStr() + ";";
//						}
//						ServerThread.sendMsgToBackUp(msg);
//						System.out.println("Player "+ p.getName() + " ping failed!!!");
//						break;
//					}
//				}
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//}


