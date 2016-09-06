package MazeGame.socket;

import java.io.BufferedReader;
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
	private int portNumber = 0;
	public boolean isPrimary = false;
	public boolean isBackup = false;
	private Socket backupSkt = null;
	private OutputStreamWriter out2Backup;
	private ServerEventListener listener;
	
	public ArrayList<Player> playerList = new ArrayList<Player>();
	
	public ServerThread(){}
	
	public int getPortNumber() {
		return this.portNumber;
	}
	
	public void setServerEventListener (ServerEventListener listener) {
	    this.listener = listener;
	}

	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(portNumber);) {
			portNumber = serverSocket.getLocalPort();
			if(listener != null){
				listener.onServerSocketCreatedEvent();
			}
			System.out.println("Server listening on port " + portNumber);
			while (true) {
				try (Socket clientSocket = serverSocket.accept();) {
					String clientAddress = clientSocket.getRemoteSocketAddress().toString();
					int clientPort = clientSocket.getPort();
					System.out.println("client " + clientAddress + ":" + clientPort + " accepted.");
					
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					OutputStreamWriter out = new OutputStreamWriter(clientSocket.getOutputStream());
					
					if(this.isPrimary){
						
						ObjectInputStream objectInput = new ObjectInputStream(clientSocket.getInputStream());
						Object object =(Player) objectInput.readObject();
		                Player remotePlayer = (Player) object;
		                this.playerList.add(remotePlayer);
		                System.out.println("Player Added: "+ remotePlayer.toStr());
		                
						if(this.backupSkt == null){
							findBackupServer(portNumber);
		                }
		                
		                // TODO: Sign this player a position(no other players and no treasure)
		                
		                // find backup server and store the link
		                
		                String fullListString = "";
		                for(Player p : this.playerList){
							fullListString += p.toStr() + ";";
						}
		                
						if(this.out2Backup != null){
							try{
								System.out.println("Server send to backupserver" + this.out2Backup);
								this.out2Backup.write(fullListString);
								this.out2Backup.flush();
								System.out.println("Server send to backupserver=== finished");
							} catch(Exception e){
								this.out2Backup = null;
								System.out.println("Server fail to send to backupserver");
								findBackupServer(portNumber);
								if(this.out2Backup != null){
									this.out2Backup.write(fullListString);
									this.out2Backup.flush();
								}
							}
						}
						
						System.out.println("Server send full list of current players: " + fullListString);
						out.write(fullListString);
						out.flush();
						
					}
					else{
						this.isBackup = true;
						System.out.println("=============BACKUP SERVER================");
					}
					
					if(this.isBackup){
						while(true){
							System.out.println("BackupServer waiting for information...");
							while (!in.ready()) {}
							String information = in.readLine();
							System.out.println("Backupserver reveived information: " + information);
						}
					}
					
	                //send full list of current players registered to primary server to the client
	                
	                //open another thread for this single client???
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void findBackupServer(int portNumber){
		for(int i=0; i< this.playerList.size(); i++){
			if (this.playerList.get(i).getPort() == portNumber) {
				System.out.println("Start to find backup server. " + i);
				for(int j=i+1; j<this.playerList.size(); j++){
					Player p = this.playerList.get(j);
					try{
						this.backupSkt = new Socket(p.getIp(), p.getPort());
						this.out2Backup = new OutputStreamWriter(this.backupSkt.getOutputStream());
						System.out.println("Found the backup player: " + p.toStr());
						break;
					} catch(Exception e){
						System.out.println("Fail to get backup server, try second one!");
					}
				}
				break;
			}
		}
	}
}

