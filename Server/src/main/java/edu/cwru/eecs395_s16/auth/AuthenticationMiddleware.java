package edu.cwru.eecs395_s16.auth;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.core.JsonableException;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.interfaces.repositories.SessionRepository;
import edu.cwru.eecs395_s16.interfaces.services.GameClient;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;
import edu.cwru.eecs395_s16.networking.responses.StatusCode;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/21/16.
 */
public class AuthenticationMiddleware {

    private final Method next;
    private boolean needsAuthentication = false;
    private final NetworkingInterface instance;
    private final SessionRepository sessions;

    private final Class<? extends RequestData> objClass;

    public AuthenticationMiddleware(NetworkingInterface instance, SessionRepository sessions, Method next, boolean needsAuthentication) {
        this.next = next;
        Class firstParam = next.getParameterTypes()[0];
        if (JSONObject.class.isAssignableFrom(firstParam)) {
            objClass = null;
        } else {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends RequestData> castedParam = (Class<? extends RequestData>) firstParam;
                objClass = castedParam;
            } catch (ClassCastException e) {
                throw new Error("Can't cast the input class.");
            }
        }
        this.instance = instance;
        this.sessions = sessions;
        this.needsAuthentication = needsAuthentication;
    }

    public Response onEvent(GameClient client, JSONObject data) {
        System.out.println("Processing method " + next.getName() + " for client with SessionID  " + client.getSessionId());
        Response response;
        try {
            Object obj;
            if (objClass == null) {
                obj = data;
            } else {
                obj = objClass.newInstance();
                ((RequestData) obj).fillFromJSON(data);
            }
            if (needsAuthentication) {
                //Retrieve client ID and check and see if they are authenticated
                UUID token = client.getSessionId();
                Optional<Player> p;
                if (client instanceof GameBot) {
                    p = Optional.of((GameBot) client);
                } else {
                    p = sessions.findPlayer(token);
                }
                if (p.isPresent()) {
                    p.get().setClient(Optional.of(client));
                    //We are all good. Invoke the next method.
                    response = (Response) next.invoke(instance, obj, p.get());
                } else {
                    response = new Response(StatusCode.UNAUTHENTICATED);
                }
            } else {
                if (next.getParameterTypes().length == 2 && next.getParameterTypes()[1].equals(GameClient.class)) {
                    response = (Response) next.invoke(instance, obj, client);
                } else {
                    response = (Response) next.invoke(instance, obj);
                }
            }
        } catch (Exception e) {
            Exception actualCause;
            if (e instanceof InvocationTargetException) {
                actualCause = (Exception) e.getCause();
            } else {
                actualCause = e;
            }
            if (actualCause instanceof JsonableException) {
                response = new Response((JsonableException) actualCause);
            } else {
                if (GameEngine.instance().IS_DEBUG_MODE) {
                    actualCause.printStackTrace();
                }
                //Generic error response here.
                //TODO PRODUCTION-IFY
                response = new Response(StatusCode.SERVER_ERROR);
            }
        }
        System.out.println("Sending response for method " + next.getName() + " for client " + client.getSessionId() + ".\n" + response.getJSONRepresentation().toString());
        return response;
    }
}