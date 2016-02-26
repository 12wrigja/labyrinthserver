package edu.cwru.eecs395_s16.interfaces.services;

import edu.cwru.eecs395_s16.GameEngine;

import java.io.IOException;

/**
 * Created by james on 2/25/16.
 */
public interface ClientConnectionService {

    void start() throws IOException;

    void stop();

    void linkToGameEngine(GameEngine g);

    void broadcastEventForRoom(String roomName, String eventName, Object data);

}
