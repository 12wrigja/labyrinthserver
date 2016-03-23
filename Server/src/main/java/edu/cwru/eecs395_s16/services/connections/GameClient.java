package edu.cwru.eecs395_s16.services.connections;

import edu.cwru.eecs395_s16.networking.Response;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by james on 2/25/16.
 */
public interface GameClient {

    Response sendEvent(String event, JSONObject data);
    void receiveEvent(String event, Object data);
    UUID getSessionId();
    void joinRoom(String roomName);
    void leaveRoom(String roomName);

}
