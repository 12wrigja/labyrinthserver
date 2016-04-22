package edu.cwru.eecs395_s16.core.actions;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.objectives.ObjectiveGameObject;
import edu.cwru.eecs395_s16.networking.requests.gameactions.CaptureObjectiveActionData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Created by james on 4/8/16.
 */
public class CaptureObjectiveGameAction implements GameAction {

    private CaptureObjectiveActionData data;
    private Creature capturingCreature;

    public CaptureObjectiveGameAction(CaptureObjectiveActionData data) {
        this.data = data;
    }

    @Override
    public InternalResponseObject<Boolean> checkCanDoAction(Match match, GameMap map, GameObjectCollection boardObjects, Player player) {
        if (!match.getHeroPlayer().getUsername().equals(player.getUsername())) {
            return new InternalResponseObject<>(InternalErrorCode.INVALID_GAME_ACTION, "Only the heroes can capture objectives. Your supposed to defend them, not capture them!");
        }

        InternalResponseObject<Boolean> canDoActiveActionResp = GameAction.canDoActiveAction(data.getCharacterID(), boardObjects, player, "character_id");
        if (!canDoActiveActionResp.isNormal()) {
            return canDoActiveActionResp;
        }
        capturingCreature = (Creature) boardObjects.getByID(data.getCharacterID()).get();

        Optional<GameObject> objectiveOpt = boardObjects.getByID(data.getObjectiveID());
        if (!objectiveOpt.isPresent()) {
            return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_OBJECT, "The objective_id is not a valid object.");
        }
        GameObject objectiveGO = objectiveOpt.get();
        if (!(objectiveGO instanceof ObjectiveGameObject)) {
            return new InternalResponseObject<>(InternalErrorCode.INVALID_OBJECT, "The objective_id is not a game object.");
        }

        ObjectiveGameObject objective = (ObjectiveGameObject) objectiveGO;

        if (objective.getControllerID().isPresent()) {
            return new InternalResponseObject<>(InternalErrorCode.NOT_CONTROLLER, "This game objective is already captured.");
        }

        if (!objective.getLocation().isNeighbourOf(capturingCreature.getLocation(), true)) {
            return new InternalResponseObject<>(InternalErrorCode.NOT_IN_RANGE, "The capturing creature is not in range of the objective.");
        } else {
            return new InternalResponseObject<>(true, "valid");
        }
    }

    @Override
    public void doGameAction(GameMap map, GameObjectCollection boardObjects) {
        ObjectiveGameObject objective = (ObjectiveGameObject) boardObjects.getByID(data.getObjectiveID()).get();
        objective.setControllerID(capturingCreature.getControllerID());
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("type", "capture_objective");
            repr.put("character_id", data.getCharacterID().toString());
            repr.put("objective_id", data.getObjectiveID().toString());
        } catch (JSONException e) {
            //This should never be hit because the keys are not null
        }
        return repr;
    }
}
