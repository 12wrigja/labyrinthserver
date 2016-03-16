package edu.cwru.eecs395_s16.bots;

import java.util.UUID;


/**
 * TestBot is a bot that does nothing. This is useful in tests - all the
 * actions are triggered by the test and not by the bot code.
 */
public class TestBot extends GameBot {

    public TestBot() {
        this(UUID.randomUUID());
    }

    public TestBot(UUID id){
        super (GameBotType.TESTBOT, id);
    }

    @Override
    public void receiveEvent(String event, Object data) {

    }
}
