package edu.cwru.eecs395_s16.test;


import io.socket.client.Socket;
import org.junit.After;

import java.util.Optional;

/**
 * Created by james on 2/12/16.
 */
public abstract class SingleUserNetworkTest extends NetworkTestCore {

    protected Socket socket;

    public void setupSingleClient() {
        Optional<Socket> sock = super.connectSocketIOClient();
        if (sock.isPresent()) {
            socket = sock.get();
        }
    }

    @After
    public void disconnectClient() {
        if (socket != null) {
            socket.disconnect();
        }
    }

}
