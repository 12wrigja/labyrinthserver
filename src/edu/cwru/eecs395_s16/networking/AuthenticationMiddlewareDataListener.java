package edu.cwru.eecs395_s16.networking;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import edu.cwru.eecs395_s16.auth.exceptions.JsonableException;
import edu.cwru.eecs395_s16.core.Interfaces.Jsonable;

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
        if (needsAuthentication) {
            //Retrieve client ID and check and see if they are authenticated
            UUID token = client.getSessionId();

        } else {
            Jsonable response;
            try {
                response = (Jsonable) next.invoke(instance, data);
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof JsonableException) {
                    response = (JsonableException) e;
                } else {
                    response = new Response();
                }
            }
            ackSender.sendAckData(response.getJsonableRepresentation());
        }
    }
}
