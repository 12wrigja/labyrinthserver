package edu.cwru.eecs395_s16.services.bots.botimpls;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.GameState;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.networking.Response;
import edu.cwru.eecs395_s16.networking.requests.gameactions.PassGameActionData;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

/**
 * Created by james on 2/25/16.
 */
public class PassBot extends GameBot {

    static final String BOT_NAME = "PASSBOT";

    private boolean enabled = true;

    public PassBot() {
        super(BOT_NAME, UUID.randomUUID());
    }

    @Override
    public void receiveEvent(String event, Object data) {
        if (enabled) {
            if (event.equals(Match.MATCH_FOUND_KEY)) {
                JSONObject jData = (JSONObject) data;
                try {
                    String startState = jData.getString(Match.GAME_STATE_KEY);
                    boolean isHeroPlayer = jData.getJSONObject(Match.PLAYER_OBJ_KEY).getString(Match.HERO_PLAYER_KEY).equals(getUsername());
                    boolean isArchitectPlayer = jData.getJSONObject(Match.PLAYER_OBJ_KEY).getString(Match.ARCHITECT_PLAYER_KEY).equals(getUsername());
                    if ((startState.equals(GameState.HERO_TURN.toString().toLowerCase()) && isHeroPlayer) ||
                            (startState.equals(GameState.ARCHITECT_TURN.toString().toLowerCase()) && isArchitectPlayer)) {
                        passAllCharacters();
                    }
                } catch (JSONException e) {
                    if (GameEngine.instance().IS_DEBUG_MODE) {
                        System.err.println("JSON Formatting error for passbot.");
                        e.printStackTrace();
                    }
                }
            } else if (event.equals(Match.GAME_UPDATE_KEY)) {
                String gameStateStr = ((JSONObject) data).optString(Match.GAME_UPDATE_TYPE_KEY, null);
                if (gameStateStr != null && gameStateStr.equals(Match.MATCH_END_KEY)) {
                    sendEvent("leave_match", new JSONObject());
                } else {
                    passAllCharacters();
                }
            }
        }
    }

    public void passAllCharacters() {
        Response matchResp = sendEvent("match_state", new JSONObject());
        if (matchResp.getStatus() == WebStatusCode.OK) {
            @SuppressWarnings("unchecked")
            InternalResponseObject<Match> actualResp = (InternalResponseObject<Match>) matchResp;
            Match match = actualResp.get();
            if (match.isPlayerTurn(this)) {
                List<GameObject> myCreatures = match.getBoardObjects().getForPlayerOwner(this);
                myCreatures.stream().filter(obj -> obj instanceof Creature).forEach(obj -> {
                    PassGameActionData passData = new PassGameActionData(obj.getGameObjectID());
                    sendEvent("game_action", passData.convertToJSON());
                });
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
