package MazeGame.socket;

import java.io.DataOutputStream;

public interface ServerEventListener {
	void onServerSocketCreatedEvent ();
	void onPrimaryServerUpEvent();
	void onBackupServerUpEvent();
	void onPrimaryServerFoundEvent(DataOutputStream out);
	void onMazeStringReceived(String msg);
}
