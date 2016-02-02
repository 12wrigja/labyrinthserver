package edu.cwru.eecs395_s16.networking;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.JsonableException;
import edu.cwru.eecs395_s16.core.Interfaces.Jsonable;
import edu.cwru.eecs395_s16.core.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by james on 1/21/16.
 */
public class AuthenticationMiddlewareDataListener<T> implements DataListener<T> {

    private Method next;
    private boolean needsAuthentication = false;
    private NetworkingInterface instance;

    public AuthenticationMiddlewareDataListener(NetworkingInterface instance, Method next) {
        this.next = next;
        this.instance = instance;
    }

    public AuthenticationMiddlewareDataListener(NetworkingInterface instance, Method next, boolean needsAuthentication) {
        this(instance, next);
        this.needsAuthentication = needsAuthentication;
    }

    @Override
    public void onData(SocketIOClient client, T data, AckRequest ackSender) throws Exception {
        System.out.println("Processing method " + next.getName() + " for client with SessionID  " + client.getSessionId());
        Response response;
        try {
            if (needsAuthentication) {
                //Retrieve client ID and check and see if they are authenticated
                UUID token = client.getSessionId();
                Player p = GameEngine.instance().sessionRepo.findPlayer(token);
                if (p != null) {
                    p.setClient(client);
                    //We are all good. Invoke the next method.
                    response = (Response) next.invoke(instance, data, p);
                } else {
                    response = new Response(StatusCode.UNAUTHENTICATED);
                }
            } else {
                if (next.getParameterTypes().length == 2 && next.getParameterTypes()[1].equals(SocketIOClient.class)) {
                    response = (Response) next.invoke(instance, data, client);
                } else {
                    response = (Response) next.invoke(instance, data);
                }
            }
        } catch (Exception e) {
            Exception actualCause;
            if(e instanceof InvocationTargetException){
                actualCause = (Exception)e.getCause();
            } else {
                actualCause = e;
            }
            actualCause.printStackTrace();
            if (actualCause instanceof JsonableException) {
                response = new Response((JsonableException) actualCause);
            } else {
                //Generic error response here.
                //TODO PRODUCTION-IFY
                response = new Response();
            }
        }
        System.out.println("Sent response for method " + next.getName() + " to client " + client.getSessionId());
        ackSender.sendAckData(response.getJsonableRepresentation());
    }
}
