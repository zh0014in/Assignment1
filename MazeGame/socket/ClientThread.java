package MazeGame.socket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

class ClientThread extends Thread {
	BufferedReader inFromServer;
	private Player localPlayer;
	private Player[] playerList;
	private Socket conn2Server;
	private ServerEventListener listener;

	public ClientThread(Player[] playerList, Player localPlayer) {
		this.localPlayer = localPlayer;
		this.playerList = playerList;
	}

	public void setServerEventListener(ServerEventListener listener) {
		this.listener = listener;
	}

	public void run() {
		DataOutputStream out;
		while (true) {
			try {
				// find the actual primary server
				Player primaryServer = null;
				int primaryServeIndex = 0;
				for (int i = 0; i < playerList.length; i++) {
					primaryServer = playerList[i];
					try {
						System.out.println("LocalPlayer try to connect to " + primaryServer.getName() + " "
								+ primaryServer.getIp() + " " + primaryServer.getPort());
						this.conn2Server = new Socket(primaryServer.getIp(), primaryServer.getPort());
						System.out.println("LocalPlayer find primary server: " + primaryServer.getName() + " "
								+ primaryServer.getIp() + " " + primaryServer.getPort());
						primaryServeIndex = i;
						break;
					} catch (Exception e) {
						ServerThread.removePlayer(primaryServer);
						System.out.println("Connect to " + primaryServer.getName() + " failed! Try next one!");
					}
				}

				// Send local player to server
				out = new DataOutputStream(this.conn2Server.getOutputStream());
				out.writeBytes(this.localPlayer.toStr() + "\n");
				out.flush();

				if (this.listener != null) {
					// client found a primary server
					this.listener.onPrimaryServerFoundEvent(out);
				}

				inFromServer = new BufferedReader(new InputStreamReader(this.conn2Server.getInputStream()));
				boolean isB = false;
				
				try {
					while (true) {
						// receive the full list of players
						String msg = inFromServer.readLine();
						System.out.println("Raw infomation-----------: " + msg);
						String[] msgToken = msg.split(";");
						String tag = msgToken[0].trim();
						if (tag.equals("BK")) {
							isB = true;
							System.out.println("BackupServer received backup info: " + msg);
							if (this.listener != null) {
								this.listener.onBackupServerUpEvent();
							}
							if (msgToken[1].equals("IF")) {
								ServerThread.playerList = new ArrayList<Player>();
								for (int i = 2; i < msgToken.length; i++) {
									Player tmp = new Player(msgToken[i]);
									ServerThread.addNewPlayer(tmp);

									// if(ServerThread.addNewPlayer(tmp)){
									// System.out.println("==>Player "+
									// msgToken[i] +" added in list");
									// }
									// else
									// System.out.println("Player "+ msgToken[i]
									// +" already in list");
								}
							}
							// if = MZ then back up maze info
							else if (msgToken[1].equals("MZ")) {
								ServerThread.maze.fromString(msg.substring(3));
							}
						} else if (tag.equals("IF")) {
							System.out.println("LocalPlayer receive full list of current players: " + msg);
						} else if (tag.equals("MZ")) {
							System.out.println("LocalPlayer receive maze info: " + msg);
							if (this.listener != null) {
								this.listener.onMazeStringReceived(msg);
							}
						} else
							System.out.println("Unkown message received: " + msg);
					}
				} 
//				catch (IOException e) {
//					// TODO Auto-generated catch block
//					ServerThread.removePlayer(primaryServer);
//					ServerThread.maze.ExitGame(primaryServer);
//					System.out.println("Primary Server down!");
//				}
				catch (Exception e) {
					ServerThread.removePlayer(primaryServer);
					if(isB) 
						ServerThread.maze.ExitGame(primaryServer);
					System.out.println("Primary Server down!");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}