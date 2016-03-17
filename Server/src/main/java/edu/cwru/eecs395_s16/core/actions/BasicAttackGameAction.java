package edu.cwru.eecs395_s16.core.actions;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.interfaces.objects.Creature;
import edu.cwru.eecs395_s16.interfaces.objects.GameAction;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.networking.requests.gameactions.BasicAttackActionData;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 3/16/16.
 */
public class BasicAttackGameAction implements GameAction {

    BasicAttackActionData data;

    Creature attacker;
    List<Creature> targets;

    public BasicAttackGameAction(BasicAttackActionData data) {
        this.data = data;
    }

    @Override
    public InternalResponseObject<Boolean> checkCanDoAction(GameMap map, GameObjectCollection boardObjects, Player player) {
        //Check and see if the object is a valid object in this game
        Optional<GameObject> attackerObjOpt = boardObjects.getByID(data.getAttacker());
        if (!attackerObjOpt.isPresent()) {
            return new InternalResponseObject<>(InternalErrorCode.INVALID_OBJECT, "The attacker id is not a valid game object.");
        }
        GameObject attackerObj = attackerObjOpt.get();
        //Check that the target has health - you can't attack dead stuff (it will have probably been removed from the board)
        if(!(attackerObj instanceof Creature)){
            return new InternalResponseObject<>(InternalErrorCode.INVALID_OBJECT, "The attacker game object is not a creature that can attack.");
        }
        //Check and see if the player who made the request can control their attacker
        if (!GameAction.isControlledByPlayer(attackerObj, player)) {
            return new InternalResponseObject<>(InternalErrorCode.NOT_CONTROLLER, "You are not the controller of the attacker object");
        }
        attacker = (Creature)attackerObj;

        targets = new ArrayList<>();
        for(UUID targetID : data.getTargets()) {
            Optional<GameObject> targetObjOpt = boardObjects.getByID(targetID);
            if (!targetObjOpt.isPresent() || !(targetObjOpt.get() instanceof Creature)) {
                return new InternalResponseObject<>(InternalErrorCode.INVALID_OBJECT, "A target id is not a valid game object.");
            } else if (GameAction.isControlledByPlayer(targetObjOpt.get(), player)) {
                //Check and see if the player is trying to target a friendly unit with their attacker
                return new InternalResponseObject<>(InternalErrorCode.FRIENDLY_FIRE, "A target game object is controlled by you.");
            }else {
                targets.add((Creature)targetObjOpt.get());
            }
        }

        //Check that for a specific attacker the list of targets is valid.
        InternalResponseObject<Boolean> validationResp = attacker.validListOfBasicAttackTargets(targets);
        if(!validationResp.isNormal()){
            return validationResp;
        }
        return new InternalResponseObject<>(true, "valid");
    }

    @Override
    public void doGameAction(GameMap map, GameObjectCollection boardObjects) {
        //For each of the targets
        for(Creature target : targets) {
            //Compute base damage here according to formula
            int baseDamage = computeBaseDamage(attacker,target);
            //Tell the attacker to attack the list of targets knowing the base damage it does
            attacker.attackTarget(target,baseDamage);
        }
    }

    private int computeBaseDamage(Creature attacker, Creature target) {
        //This is the damage formula we agreed upon.
        return ((100-target.getDefense())/100)* attacker.getAttack();
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null;
    }
}
