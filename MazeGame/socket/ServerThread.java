package MazeGame.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
	private int portNumber = 0;
	private ServerEventListener listener;
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
			while (true) {
				System.out.println("listing on port " + portNumber);
				try (Socket clientSocket = serverSocket.accept();) {
					String clientAddress = clientSocket.getRemoteSocketAddress().toString();
					int clientPort = clientSocket.getPort();
					System.out.println("client " + clientAddress + ":" + clientPort + " accepted.");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
