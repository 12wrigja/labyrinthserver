package edu.cwru.eecs395_s16.bots;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.interfaces.services.GameClient;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/25/16.
 */
public class PassBot extends GameBot {

    static final String BOT_NAME = "PASSBOT";

    public PassBot() {
        super(BOT_NAME,UUID.randomUUID());
    }

    @Override
    public void receiveEvent(String event, Object data) {

    }

    @Override
    public void onConnect() {

    }

    @Override
    protected void onDisconnect() {

    }

    @Override
    public void setCurrentMatch(Optional<UUID> currentMatch) {
        //Do nothing. PassBot can be in multiple matches simultaneously as it is a singleton bot
    }

}
