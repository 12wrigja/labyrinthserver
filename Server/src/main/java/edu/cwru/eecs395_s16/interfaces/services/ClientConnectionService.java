package edu.cwru.eecs395_s16.interfaces.services;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalResponseObject;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by james on 2/25/16.
 */
public interface ClientConnectionService {

    void start() throws IOException;

    void stop();

    void linkToGameEngine(GameEngine g);

    void broadcastEventForRoom(String roomName, String eventName, Object data);

    InternalResponseObject<GameClient> findClientFromUUID(UUID clientID);

}
