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
import java.util.concurrent.BlockingQueue;


public class ServerThread extends Thread {
	// this is the master server thread
	public static int portNumber = 0;
	private boolean isPrimary = false;
	public boolean isBackup = false;
	private DataOutputStream out2Backup = null;
	private ServerEventListener listener;
	protected static Maze maze;
	
	public static  ArrayList<String> myQ = new ArrayList<String>();
	
	public static ArrayList<Player> playerList = new ArrayList<Player>();
	public static Player backupServer = null;
	public ServerThread(){}
	
	public int getPortNumber() {
		return this.portNumber;
	}
	
	public void setServerEventListener (ServerEventListener listener) {
	    this.listener = listener;
	}
	
	public boolean getIsPrimary(){
		return this.isPrimary;
	}
	
	public void initializeMaze(int n, int k){
		try {
			this.maze = new Maze(n,k);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			portNumber = serverSocket.getLocalPort();
			if(listener != null){
				listener.onServerSocketCreatedEvent();
			}
			new CommandThread().start();
			System.out.println("Server listening on port " + portNumber);
			
			Socket clientSocket;
			while (true) {
				try {
					clientSocket = serverSocket.accept();
					if(listener != null && !isPrimary){
						int start = 0;
						int len = ServerThread.playerList.size();
						for(int i=0; i< len; i++){
							if (ServerThread.playerList.get(i).getPort() == portNumber) {
								start = i;
								break;
							}
						}
						for(int i=0; i< start; i++){
							ServerThread.playerList.remove(0);
						}
						listener.onPrimaryServerUpEvent();
						isPrimary = true;
						isBackup = false;
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
				
				for(int j=i+1; j<ServerThread.playerList.size(); j++){
					backupServer = ServerThread.playerList.get(j);
					System.out.println("Try to find backup server: " + backupServer.toStr());
					if (backupServer.outToClient == null){
						System.out.println("Find backupserver: failed???" + backupServer.outToClient);
						return false;
					}
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
				System.out.println("Adding player: insert new player: " + tmp.toStr() );
				return true;
			}
			
			if (curPlayer.sequenceNumber == tmp.sequenceNumber){
				System.out.println("Adding player: update old player-----------: " + curPlayer.toStr() + curPlayer.outToClient );
				curPlayer.connOnServer = tmp.connOnServer;
				curPlayer.outToClient = tmp.outToClient;
				System.out.println("Adding player: update old player: " + curPlayer.toStr() + curPlayer.outToClient );
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
			if(p.getName().equals(tmp.getName())){
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

            if(ServerThread.addNewPlayer(curPlayer)){
            	ServerThread.maze.JoinGame(curPlayer);
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
            Game.out.println(fullListString);
	        outToClient.writeBytes(fullListString + "\n");
	        outToClient.flush();
            
	        String mazeString = ServerThread.maze.toString();
	        outToClient.writeBytes(mazeString +"\n");
	        outToClient.flush();
	        ServerThread.sendMsgToBackUp(mazeString);
			
	        
            while(true){
	        	String command = inFromClient.readLine().trim();
	        	
	        	int newCommand = Integer.parseInt(command);
	        	ServerThread.myQ.add(curPlayer.getName()+";"+newCommand);
	        	System.out.println("Server receive  " + command + "from " + curPlayer.getName());
    		}
		} catch (Exception e) {
			e.printStackTrace();
			ServerThread.playerList.remove(curPlayer);
			if(ServerThread.backupServer == null || (ServerThread.backupServer != null && curPlayer.getName().equals(ServerThread.backupServer.getName())))
				ServerThread.findBackupServer(ServerThread.portNumber);
			ServerThread.maze.ExitGame(curPlayer);
			String fullListString = "IF;";
            for(Player p : ServerThread.playerList){
				fullListString += p.toStr() + ";";
			}
            ServerThread.sendMsgToBackUp(fullListString);
            Game.out.println(fullListString);
            ServerThread.sendMsgToBackUp(ServerThread.maze.toString());
			System.out.println("Player"+ curPlayer.toStr() +"disconnect");
		}
	}
}

class CommandThread extends Thread{
	public void run(){
		while(true){
			if(ServerThread.myQ.isEmpty()){
				try {
					Thread.sleep(2);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				continue;
			}
			String command = ServerThread.myQ.remove(0);
			if(command != null){
				System.out.println("Server excute  " + command);
				String[] l = command.split(";");
				String playerName = l[0];
				Player curPlayer = null;
				boolean isFound = false;
				for(int i = 0; i < ServerThread.playerList.size(); i++){
					if(ServerThread.playerList.get(i).getName().equals(playerName)){
						isFound = true;
						System.out.println("Server find player "+ command);
						curPlayer = ServerThread.playerList.get(i);
						int newCommand = Integer.parseInt(l[1]);
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
						try {
							String mazeString = ServerThread.maze.toString();
							curPlayer.outToClient.writeBytes(mazeString+"\n");
							curPlayer.outToClient.flush();
							ServerThread.sendMsgToBackUp(mazeString);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					}
				}
				if(!isFound)
					System.out.println("Server can not  find player "+ command);
			}
		}
	}
}


