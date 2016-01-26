package edu.cwru.eecs395_s16.networking;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.auth.LoginRequestObject;
import edu.cwru.eecs395_s16.auth.RegisterUserRequest;
import edu.cwru.eecs395_s16.core.Player;

import java.lang.reflect.Method;

/**
 * Created by james on 1/20/16.
 */
public class NetworkingInterface {

    public NetworkingInterface(SocketIOServer server) {
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
        return methodName.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                "_"
        ).toLowerCase();
    }

    @NetworkEvent(mustAuthenticate = false)
    public Response login(LoginRequestObject data) {
        System.out.println(data.getUsername());
        System.out.println(data.getPassword());
        return new Response();
    }

    @NetworkEvent(mustAuthenticate = false)
    public Response register(RegisterUserRequest data) {
        return new Response();
    }

    @NetworkEvent
    public Response submitTurnActions(LoginRequestObject data) {
        return new Response();
    }

    @NetworkEvent
    public Object purchaseAbility(LoginRequestObject data, Player player) {
        return new Response();
    }

}