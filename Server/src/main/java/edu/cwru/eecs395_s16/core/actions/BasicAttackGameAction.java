package edu.cwru.eecs395_s16.core.actions;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.objects.*;
import edu.cwru.eecs395_s16.networking.requests.gameactions.BasicAttackActionData;
import edu.cwru.eecs395_s16.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by james on 3/16/16.
 */
public class BasicAttackGameAction implements GameAction {

    BasicAttackActionData data;

    Creature attacker;
    Map<Location, List<Creature>> targets;
    Map<Creature, Integer> damageMap;

    public BasicAttackGameAction(BasicAttackActionData data) {
        this.data = data;
        targets = new HashMap<>();
        damageMap = new HashMap<>();
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
        if (!(attackerObj instanceof Creature)) {
            return new InternalResponseObject<>(InternalErrorCode.INVALID_OBJECT, "The attacker game object is not a creature that can attack.");
        }
        //Check and see if the player who made the request can control their attacker
        if (!GameAction.isControlledByPlayer(attackerObj, player)) {
            return new InternalResponseObject<>(InternalErrorCode.NOT_CONTROLLER, "You are not the controller of the attacker object");
        }
        attacker = (Creature) attackerObj;
        Weapon weapon = attacker.getWeapon();

        if (data.getTargets().size() > weapon.getAttackPattern().getInputCount()) {
            return new InternalResponseObject<>(InternalErrorCode.TOO_MANY_TARGETS);
        }

        //Look at the list of attack locations, validate them, and
        //pull out the creatures (if any) that are affected
        for (Location target : data.getTargets()) {
            if (weapon.getRange() == 1) {
                if (!attacker.getLocation().isNeighbourOf(target, true)) {
                    return new InternalResponseObject<>(InternalErrorCode.NOT_IN_RANGE);
                }
            } else {
                //TODO potentially make this change based on the weapon equipped
                //Check to see that the target point is within the weapon's range and that we have line of sight to that point
                if (!GameAction.floodFill(map, attacker.getLocation(), weapon.getRange(), true).contains(target)){
                    return new InternalResponseObject<>(InternalErrorCode.NOT_IN_RANGE);
                } else if (!GameAction.isLineOfSight(attacker.getLocation(), target, map, boardObjects)) {
                    return new InternalResponseObject<>(InternalErrorCode.NOT_VISIBLE);
                }
            }

            List<Creature> creaturesHit = new ArrayList<>();
            //Loop through pattern points
            for (Location targetPoint : weapon.getAttackPattern().getDamageDistributionMap(target).keySet()) {
                List<GameObject> targetObjs = boardObjects.getForLocation(targetPoint).stream().filter(obj -> obj instanceof Creature).collect(Collectors.toList());
                Creature c;
                if (targetObjs.size() == 0) {
                    continue;
                } else {
                    c = (Creature) targetObjs.get(0);
                }

                if (GameAction.isControlledByPlayer(c, player)) {
                    //Check and see if the player is trying to target a friendly unit with their attacker
                    return new InternalResponseObject<>(InternalErrorCode.FRIENDLY_FIRE, "A target game object is controlled by you.");
                }
                creaturesHit.add(c);
            }
            targets.put(target, creaturesHit);
        }
        return new InternalResponseObject<>(true, "valid");
    }

    @Override
    public void doGameAction(GameMap map, GameObjectCollection boardObjects) {
        //For each of the target points
        for (Location target : targets.keySet()) {
            for (Creature creatureHit : targets.get(target)) {
                float percentageDamage = attacker.getWeapon().getAttackPattern().damageForLocation(creatureHit.getLocation().locationRelativeTo(target));
                //Compute base damage here according to formula
                int baseDamage = computeBaseDamage(attacker, creatureHit);
                baseDamage *= percentageDamage;
                //Tell the attacker to attack the list of targets knowing the base damage it does
                attacker.attackTarget(creatureHit, baseDamage);
                damageMap.put(creatureHit, baseDamage);
            }
        }
    }

    private int computeBaseDamage(Creature attacker, Creature target) {
        //This is the damage formula we agreed upon.
        return ((100 - target.getDefense()) / 100) * attacker.getAttack();
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject representation = new JSONObject();
        try {
            representation.put("type", "basic_attack");
            representation.put("attacker_id", attacker.getGameObjectID());
            representation.put("targets", JSONUtils.listify(damageMap, entry -> {
                JSONObject repr = new JSONObject();
                try {
                    repr.put("creature_id", entry.getKey().getGameObjectID());
                    repr.put("damage", entry.getValue());
                } catch (JSONException e) {
                    //Should never happen - keys are non-null
                }
                return repr;
            }));
        } catch (JSONException e) {
            //Should not happen - all keys are nonnull
        }
        return representation;
    }
}
