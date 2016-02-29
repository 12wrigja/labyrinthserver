package edu.cwru.eecs395_s16.networking.responses;

import edu.cwru.eecs395_s16.interfaces.Response;

/**
 * Created by james on 2/28/16.
 */
public class QueueStatusResponse extends Response {

    boolean isQueued = false;

    public QueueStatusResponse(boolean isQueued) {
        this.isQueued = isQueued;
    }

    public QueueStatusResponse(WebStatusCode code, String message, boolean isQueued) {
        super(code, message);
        this.isQueued = isQueued;
    }
}
