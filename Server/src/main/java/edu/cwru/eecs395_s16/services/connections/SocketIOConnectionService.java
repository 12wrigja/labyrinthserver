package edu.cwru.eecs395_s16.services.connections;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.objectives.GameObjective;
import edu.cwru.eecs395_s16.networking.Response;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.ui.FunctionDescription;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by james on 2/25/16.
 */
public class SocketIOConnectionService implements ClientConnectionService {

    SocketIOServer gameSocket;
    private int serverPort = 4567;
    private String serverInterface = "0.0.0.0";
    private boolean isStarted = false;
    private List<FunctionDescription> availableFunctions;
    private List<Module> usedSerializationModules;

    public SocketIOConnectionService() {
        usedSerializationModules = new ArrayList<>();
        Module jsonMod = new JsonOrgModule();
        usedSerializationModules.add(jsonMod);
    }

    public ObjectMapper getManualMapper() {
        ObjectMapper mapper = new ObjectMapper();
        usedSerializationModules.forEach(mapper::registerModule);
        return mapper;
    }

    public void setServerPort(int serverPort) {
        if (!this.isStarted) {
            this.serverPort = serverPort;
        }
    }

    public void setServerInterface(String serverInterface) {
        if (!this.isStarted) {
            this.serverInterface = serverInterface;
        }
    }

    @Override
    public void start() throws IOException {
        //Check here for port availability
        ServerSocket s = new ServerSocket(serverPort, 1, InetAddress.getByName(serverInterface));
        //Port is available.
        s.close();

        Configuration config = new Configuration();
        config.setHostname(this.serverInterface);
        config.setPort(this.serverPort);
        config.getSocketConfig().setReuseAddress(true);

        JacksonJsonSupport jacksonJsonSupport = new JacksonJsonSupport((Module[]) usedSerializationModules.toArray(new Module[usedSerializationModules.size()]));
        config.setJsonSupport(jacksonJsonSupport);
        gameSocket = new SocketIOServer(config);

        //Link all created methods to socket server.
        linkAllFunctionsToNetwork();

        gameSocket.addConnectListener(client -> {
            System.out.println("Client connected: " + client.getSessionId());
        });
        gameSocket.addDisconnectListener(client -> {
            System.out.println("Client disconnected: " + client.getSessionId());
            //TODO modify this when the UI catches up
            InternalResponseObject<Player> p = GameEngine.instance().services.sessionRepository.findPlayer(client.getSessionId());
            if (p.isNormal()) {
                Player player = p.get();
                if (player.getCurrentMatchID().isPresent()) {
                    InternalResponseObject<Match> match = Match.fromCacheWithMatchIdentifier(player.getCurrentMatchID().get());
                    if (match.isNormal()) {
                        Match m = match.get();
                        m.end("Player " + player.getUsername() + " disconnected.", GameObjective.GAME_WINNER.NO_WINNER);
                    }
                }
            }
            GameEngine.instance().services.sessionRepository.expirePlayerSession(client.getSessionId());
        });

        gameSocket.start();
        System.out.println("SocketIOConnectionService is now running, bound to interface " + this.serverInterface + " on port " + this.serverPort);
        this.isStarted = true;
    }

    @Override
    public void stop() {
        gameSocket.stop();
        System.out.println("SocketIOCommunicationService is stopped.");
        this.isStarted = false;
    }

    @Override
    public void linkToGameEngine(GameEngine g) {
        this.availableFunctions = g.getAllFunctions();
    }

    @Override
    public void broadcastEventForRoom(String roomName, String eventName, Object data) {
        gameSocket.getRoomOperations(roomName).sendEvent(eventName, data);
    }

    @Override
    public InternalResponseObject<GameClient> findClientFromUUID(UUID clientID) {
        SocketIOClient client = gameSocket.getClient(clientID);
        if (client != null) {
            return new InternalResponseObject<>(new SocketIOClientWrapper(client));
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.UNKNOWN_SESSION_IDENTIFIER);
        }
    }

    private void linkAllFunctionsToNetwork() {
        for (FunctionDescription fd : this.availableFunctions) {
            gameSocket.addEventListener(fd.name, JSONObject.class, (client, data, ackSender) -> {
                SocketIOClientWrapper wrapper = new SocketIOClientWrapper(client);
                Response r = fd.invocationPoint.onEvent(wrapper, data);
                ackSender.sendAckData(r.getJSONRepresentation());
            });
        }
    }

}
