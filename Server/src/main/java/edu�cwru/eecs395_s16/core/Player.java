package edu.cwru.eecs395_s16.core;

import com.corundumstudio.socketio.SocketIOClient;

/**
 * Created by james on 1/19/16.
 */
public class Player {

    private String username;

    private String password;

    private SocketIOClient client;

    public Player(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Player(SocketIOClient client){
        this.client = client;
        //TODO retrieve username and password here. Or see if this ends up being used.
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public String getUsername() {
        return username;
    }

    public SocketIOClient getClient(){
        return this.client;
    }

    public void setClient(SocketIOClient client){
        this.client = client;
    }
}
