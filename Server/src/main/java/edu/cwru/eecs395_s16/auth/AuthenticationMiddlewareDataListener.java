package edu.cwru.eecs395_s16.auth;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import edu.cwru.eecs395_s16.core.JsonableException;
import edu.cwru.eecs395_s16.interfaces.repositories.SessionRepository;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.networking.responses.StatusCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/21/16.
 */
public class AuthenticationMiddlewareDataListener<T> implements DataListener<T> {

    private final Method next;
    private boolean needsAuthentication = false;
    private final NetworkingInterface instance;
    private final SessionRepository sessions;

    public AuthenticationMiddlewareDataListener(NetworkingInterface instance, SessionRepository sessions, Method next) {
        this.next = next;
        this.instance = instance;
        this.sessions = sessions;
    }

    public AuthenticationMiddlewareDataListener(NetworkingInterface instance,  SessionRepository sessions,Method next, boolean needsAuthentication) {
        this(instance, sessions, next);
        this.needsAuthentication = needsAuthentication;
    }

    @Override
    public void onData(SocketIOClient client, T data, AckRequest ackSender) throws Exception {
        System.out.println("Processing method " + next.getName() + " for client with SessionID  " + client.getSessionId());
        Response response;
        try {
            ((RequestData)data).validate();
            if (needsAuthentication) {
                //Retrieve client ID and check and see if they are authenticated
                UUID token = client.getSessionId();
                Optional<Player> p = sessions.findPlayer(token);
                if (p.isPresent()) {
                    p.get().setClient(client);
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
            if (actualCause instanceof JsonableException) {
                response = new Response((JsonableException) actualCause);
            } else {
                //Generic error response here.
                //TODO PRODUCTION-IFY
                response = new Response(StatusCode.SERVER_ERROR);
            }
        }
        System.out.println("Sent response for method " + next.getName() + " to client " + client.getSessionId());
        ackSender.sendAckData(response.getJsonableRepresentation());
    }
}
