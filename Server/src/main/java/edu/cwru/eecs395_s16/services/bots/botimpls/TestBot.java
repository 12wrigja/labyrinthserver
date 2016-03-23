package edu.cwru.eecs395_s16.services.bots.botimpls;

import java.util.UUID;


/**
 * TestBot is a bot that does nothing. This is useful in tests - all the
 * actions are triggered by the test and not by the bot code.
 */
public class TestBot extends GameBot {

    static final String BOT_NAME = "TESTBOT";

    public TestBot() {
        super(BOT_NAME, UUID.randomUUID());
    }

    @Override
    public void receiveEvent(String event, Object data) {

    }
}
