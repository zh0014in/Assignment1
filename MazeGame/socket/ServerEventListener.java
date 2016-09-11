package MazeGame.socket;

import java.io.DataOutputStream;

public interface ServerEventListener {
	void onServerSocketCreatedEvent ();
	void onPrimaryServerUpEvent();
	void onPrimaryServerFoundEvent(DataOutputStream out);
}
