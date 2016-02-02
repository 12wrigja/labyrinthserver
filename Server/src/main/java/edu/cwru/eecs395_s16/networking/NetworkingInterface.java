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
import edu.cwru.eecs395_s16.core.Interfaces.Objects.Character;
import edu.cwru.eecs395_s16.core.Player;

import java.lang.reflect.Method;

/**
 * Created by james on 1/20/16.
 */
public class NetworkingInterface {

    private SocketIOServer server;

    public NetworkingInterface() {
    }

    public NetworkingInterface(SocketIOServer server) {
        this.server = server;
        //Attach links to server events
        Method[] methods = this.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(NetworkEvent.class)) {
                System.out.println("Registering a network socket method '" + convertMethodNameToEventName(m.getName()) + "'");
                Class dataType = m.getParameterTypes()[0];
                NetworkEvent at = m.getAnnotation(NetworkEvent.class);

                //TODO transform the method name according to socket rules
                server.addEventListener(m.getName(), dataType, createTypecastMiddleware(dataType, m, at.mustAuthenticate()));
            }
        }
    }

    private <T> DataListener<T> createTypecastMiddleware(@SuppressWarnings("UnusedParameters") Class<T> data, Method next, boolean needsAuth) {
        return new AuthenticationMiddlewareDataListener<>(this, next, needsAuth);
    }

    private String convertMethodNameToEventName(String methodName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < methodName.length(); i++) {
            char letter = methodName.charAt(i);
            if (letter <= 'Z' && letter >= 'A') {
                sb.append('_');
                sb.append((char) ((int) letter + 32));
            } else {
                sb.append(letter);
            }
        }
        return sb.toString();

    }

    @NetworkEvent(mustAuthenticate = false)
    public Response login(LoginRequestObject data, SocketIOClient client) throws UnknownUsernameException, InvalidPasswordException {
        Player p = GameEngine.instance().userRepo.loginPlayer(data.getUsername(), data.getPassword());
        GameEngine.instance().sessionRepo.storePlayer(client.getSessionId(), p);
        return new Response();
    }

    @NetworkEvent(mustAuthenticate = false)
    public Response register(RegisterUserRequest data) throws DuplicateUsernameException, MismatchedPasswordException {
        Player p = GameEngine.instance().userRepo.registerPlayer(data.getUsername(), data.getPassword());
        return new Response();
    }

    @NetworkEvent
    public Response submitTurnActions(LoginRequestObject data, Player player) {
        return new Response();
    }

    @NetworkEvent
    public ExtendedResponse purchaseAbility(LoginRequestObject data, Player player) {
        return new ExtendedResponse();
    }


    @NetworkEvent
    public Response join(Object obj, Player player) {
        SocketIOClient c = player.getClient();
        if (!c.getAllRooms().contains("TestRoom")) {
            c.joinRoom("TestRoom");
        }
        return new Response();
    }

    @NetworkEvent
    public Response leave(Object obj, Player player){
        SocketIOClient c = player.getClient();
        if(c.getAllRooms().contains("TestRoom")){
            c.leaveRoom("TestRoom");
        }
        return new Response();
    }
}

class ExtendedResponse extends Response {

}