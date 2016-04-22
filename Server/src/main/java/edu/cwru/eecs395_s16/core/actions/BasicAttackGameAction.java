package edu.cwru.eecs395_s16.core.actions;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.networking.requests.gameactions.BasicAttackActionData;
import edu.cwru.eecs395_s16.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public InternalResponseObject<Boolean> checkCanDoAction(Match match, GameMap map, GameObjectCollection boardObjects, Player player) {
        //Check and see if the object is a valid object for doing active actions
        InternalResponseObject<Boolean> validCreatureResp = GameAction.canDoActiveAction(data.getAttacker(), boardObjects, player, "attacker_id");
        if (!validCreatureResp.isNormal()) {
            return validCreatureResp;
        }
        attacker = (Creature) boardObjects.getByID(data.getAttacker()).get();
        Weapon weapon = attacker.getWeapon();

        if (data.getTargets().size() > weapon.getUsePattern().getInputCount()) {
            return new InternalResponseObject<>(InternalErrorCode.TOO_MANY_TARGETS);
        } else if (data.getTargets().size() < weapon.getUsePattern().getInputCount()) {
            return new InternalResponseObject<>(InternalErrorCode.TOO_FEW_TARGETS);
        }

        //Look at the list of attack locations, validate them, and
        //pull out the creatures (if any) that are affected
        for (Location target : data.getTargets()) {
            if (!map.getTile(target).isPresent()) {
                return new InternalResponseObject<>(InternalErrorCode.INVALID_LOCATION);
            }
            if (weapon.getRange() == 1) {
                if (attacker.getLocation().equals(target)) {
                    return new InternalResponseObject<>(InternalErrorCode.FRIENDLY_FIRE);
                }
                if (!attacker.getLocation().isNeighbourOf(target, true)) {
                    return new InternalResponseObject<>(InternalErrorCode.NOT_IN_RANGE);
                }
            } else {
                //Check to see that the target point is within the weapon's range and that we have line of sight to that point
                if (!GameAction.floodFill(map, attacker.getLocation(), weapon.getRange(), true).contains(target)) {
                    return new InternalResponseObject<>(InternalErrorCode.NOT_IN_RANGE);
                } else if (!GameAction.isLineOfSight(attacker.getLocation(), target, map, boardObjects)) {
                    return new InternalResponseObject<>(InternalErrorCode.NOT_VISIBLE);
                }
            }

            List<Creature> creaturesHit = new ArrayList<>();
            //Loop through pattern points
            for (Location targetPoint : weapon.getUsePattern().getEffectDistributionMap(target).keySet()) {
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
                } else if (GameAction.isControlledByOpponent(c, player)) {
                    creaturesHit.add(c);
                }
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
                float percentageDamage = attacker.getWeapon().getUsePattern().effectPercentForLocation(creatureHit.getLocation().locationRelativeTo(target));
                //Compute base damage here according to formula
                int baseDamage = computeBaseDamage(attacker, creatureHit);
                baseDamage *= percentageDamage;
                //Tell the attacker to attack the list of targets knowing the base damage it does
                attacker.attackTarget(creatureHit, baseDamage);
                damageMap.put(creatureHit, baseDamage);
            }
        }
        attacker.useActionPoint();
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

    private int computeBaseDamage(Creature attacker, Creature target) {
        //This is the damage formula we agreed upon.
        return (int) Math.floor(((100 - target.getDefense()) / 100f) * attacker.getAttack());
    }
}
