package edu.cwru.eecs395_s16.bots;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.interfaces.objects.Creature;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.networking.requests.gameactions.PassGameActionData;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/25/16.
 */
public class PassBot extends GameBot {

    static final String BOT_NAME = "PASSBOT";

    public PassBot() {
        super(BOT_NAME,UUID.randomUUID());
        heroes.add(new HeroBuilder().setOwnerID(Optional.of(getUsername())).createHero());
    }

    @Override
    public void receiveEvent(String event, Object data) {
        if(event.equals(Match.GAME_UPDATE_KEY)){
            Response matchResp = sendEvent("match_state",new JSONObject());
            if(matchResp.getStatus() == WebStatusCode.OK){
                @SuppressWarnings("unchecked")
                InternalResponseObject<Match> actualResp = (InternalResponseObject<Match>)matchResp;
                Match match = actualResp.get();
                List<GameObject> myCreatures = match.getBoardObjects().getForPlayerOwner(this);
                myCreatures.stream().filter(obj -> obj instanceof Creature).forEach(obj -> {
                    PassGameActionData passData = new PassGameActionData(obj.getGameObjectID());
                    sendEvent("game_action", passData.convertToJSON());
                });
            }
        }
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {

    }

}
