package edu.cwru.eecs395_s16.services.connections;

import com.corundumstudio.socketio.SocketIOClient;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.interfaces.services.GameClient;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by james on 2/25/16.
 */
public final class SocketIOClientWrapper implements GameClient {

    private SocketIOClient client;

    public SocketIOClientWrapper(SocketIOClient client) {
        this.client = client;
    }

    @Override
    public final Response sendEvent(String event, JSONObject data) {
        throw new UnsupportedOperationException("This is a SocketIO client wrapper. This cannot programmatically send requests.");
        //Do nothing. This method allows you to submit events to the system, something this client wrapper should NOT be doing
    }

    @Override
    public final void receiveEvent(String event, Object data) {
        //This should just work as Socket.IO uses Jackson to auto-serialize the data
        this.client.sendEvent(event,data);
    }

    @Override
    public final UUID getSessionId() {
        return client.getSessionId();
    }

    @Override
    public final void joinRoom(String roomName) {
        this.client.joinRoom(roomName);
    }

    @Override
    public final void leaveRoom(String roomName) {
        this.client.leaveRoom(roomName);
    }

}
