package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import org.json.JSONObject;

/**
 * Created by james on 2/25/16.
 */
public class QueueRequest implements RequestData {

    private boolean queueWithPassBot = false;

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.queueWithPassBot = obj.optBoolean("queue_with_passbot",false);
    }

    public boolean shouldQueueWithPassBot() {
        return queueWithPassBot;
    }
}
