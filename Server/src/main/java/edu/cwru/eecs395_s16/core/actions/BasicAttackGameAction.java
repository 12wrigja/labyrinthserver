package edu.cwru.eecs395_s16.core.actions;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.interfaces.objects.GameAction;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.networking.requests.gameactions.BasicAttackActionData;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Created by james on 3/16/16.
 */
public class BasicAttackGameAction implements GameAction {

    BasicAttackActionData data;

    public BasicAttackGameAction(BasicAttackActionData data) {
        this.data = data;
    }

    @Override
    public InternalResponseObject<Boolean> checkCanDoAction(GameMap map, GameObjectCollection boardObjects, Player player) {
        //Check and see if the object is a valid object in this game
        Optional<GameObject> attackerObjOpt = boardObjects.getByID(data.getAttacker());
        Optional<GameObject> targetObjOpt = boardObjects.getByID(data.getTarget());
        if (!attackerObjOpt.isPresent()) {
            return new InternalResponseObject<>(InternalErrorCode.INVALID_OBJECT, "The attacker id is not a valid game object.");
        }
        if (!targetObjOpt.isPresent()) {
            return new InternalResponseObject<>(InternalErrorCode.INVALID_OBJECT, "The target id is not a valid game object.");
        }
        GameObject attackerObj = attackerObjOpt.get();
        GameObject targetObj = targetObjOpt.get();

        //Check and see if the player who made the request can control their attacker
        if (!GameAction.isControlledByPlayer(attackerObj, player)) {
            return new InternalResponseObject<>(InternalErrorCode.NOT_CONTROLLER, "You are not the controller of the attacker object");
        }
        //Check and see if the player is trying to target a friendly unit with their attacker
        if (GameAction.isControlledByPlayer(targetObj, player)) {
            return new InternalResponseObject<>(InternalErrorCode.FRIENDLY_FIRE, "The target game object is controller by you.");
        }

        //Check that the target has health - you can't attack dead stuff (it will have probably been removed from the board)


        //Check attack range - this is based off the weapon equipped. If no weapon is equipped, then you use your fists - they have a range of 1.




        return new InternalResponseObject<>(true, "valid");
    }

    @Override
    public void doGameAction(GameMap map, GameObjectCollection boardObjects) {

    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null;
    }
}
