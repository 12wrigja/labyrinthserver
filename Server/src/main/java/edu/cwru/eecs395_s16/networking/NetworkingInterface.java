package edu.cwru.eecs395_s16.networking;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.auth.LoginRequestObject;
import edu.cwru.eecs395_s16.auth.RegisterUserRequest;
import edu.cwru.eecs395_s16.auth.exceptions.DuplicateUsernameException;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.MismatchedPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.core.Player;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 1/20/16.
 */
public class NetworkingInterface {

    public <T> DataListener<T> createTypecastMiddleware(@SuppressWarnings("UnusedParameters") Class<T> data, Method next, boolean needsAuth) {
        return new AuthenticationMiddlewareDataListener<>(this, GameEngine.instance().sessionRepository, next, needsAuth);
    }

    @NetworkEvent(mustAuthenticate = false, description = "Used to log a player in. This must be called once to allow the user to the call all methods that are marked as needing authentication.")
    public Response login(LoginRequestObject data, SocketIOClient client) throws UnknownUsernameException, InvalidPasswordException {
        Player p = GameEngine.instance().playerRepository.loginPlayer(data.getUsername(), data.getPassword());
        GameEngine.instance().sessionRepository.storePlayer(client.getSessionId(), p);
        return new Response();
    }

    @NetworkEvent(mustAuthenticate = false, description = "Registers a user if the username does not already exist and the given passwords match.")
    public Response register(RegisterUserRequest data) throws DuplicateUsernameException, MismatchedPasswordException {
        GameEngine.instance().playerRepository.registerPlayer(data.getUsername(), data.getPassword());
        return new Response();
    }

    @NetworkEvent(description = "TESTING ONLY: allows the user to join the socket.io test room.")
    public Response join(Object obj, Player player) {
        SocketIOClient c = player.getClient();
        if (!c.getAllRooms().contains("TestRoom")) {
            c.joinRoom("TestRoom");
        }
        return new Response();
    }

    @NetworkEvent(description = "TESTING ONLY: allows the user to leave the socket.io test room.")
    public Response leave(Object obj, Player player) {
        SocketIOClient c = player.getClient();
        if (c.getAllRooms().contains("TestRoom")) {
            c.leaveRoom("TestRoom");
        }
        return new Response();
    }
}