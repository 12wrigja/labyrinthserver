package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/19/16.
 */
public class Creature extends GameObject {

    public static final String ATTACK_KEY = "attack";
    public static final String DEFENSE_KEY = "defense";
    public static final String HEALTH_KEY = "health";
    public static final String MOVEMENT_KEY = "movement";
    public static final String VISION_KEY = "vision";
    public static final String ABILITIES_KEY = "abilities";
    public static final String STATUSES_KEY = "statuses";
    public static final String ACTION_POINTS_KEY = "action_points";
    private int attack = 10;
    private int defense = 10;
    private int health = 50;
    private int movement = 3;
    private int vision = 5;
    private int maxActionPoints = 2;
    private int currentActionPoints;
    private List<Ability> abilities = new ArrayList<>();
    private List<GameObjectStatus> statuses = new ArrayList<>();

    public Creature(UUID objectID, Optional<String> ownerID, Optional<String> controllerID, TYPE objectType, int attack, int defense, int health, int movement, int vision, int actionPoints, List<Ability> abilities, List<GameObjectStatus> statuses) {
        super(objectID, ownerID, controllerID, objectType);
        this.attack = attack;
        this.defense = defense;
        this.health = health;
        this.movement = movement;
        this.vision = vision;
        this.maxActionPoints = actionPoints;
        this.abilities = abilities;
        this.statuses = statuses;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getHealth() {
        return health;
    }

    public int getMovement() {
        return movement;
    }

    public int getVision() {
        return vision;
    }

    public List<Ability> getAbilities() {
        return abilities;
    }

    public void triggerPassive(GameMap map, GameObjectCollection boardObjects) {
    }

    public List<GameObjectStatus> getStatuses() {
        return statuses;
    }

    public int getActionPoints() {
        return currentActionPoints;
    }

    public void drainActionPoints() {
        currentActionPoints = 0;
    }

    public void useActionPoint() {
        currentActionPoints = Math.max(currentActionPoints - 1, 0);
    }

    public void resetActionPoints() {
        currentActionPoints = maxActionPoints;
    }

    public int getBasicAttackDamage() {
        return attack;
    }

    public InternalResponseObject<Boolean> validListOfBasicAttackTargets(List<Creature> targets){
        if(targets.size() != 1){
            return new InternalResponseObject<>(InternalErrorCode.TOO_MANY_TARGETS);
        } else if (targets.get(0).getLocation().isNeighbourOf(getLocation(),true)){
            return new InternalResponseObject<>(InternalErrorCode.NOT_IN_RANGE);
        } else {
            return new InternalResponseObject<>(true,"valid");
        }
    }

    public void attackTarget(Creature target, int computedDamage){
        target.takeDamage(computedDamage);
    }

    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

    @Override
    public JSONObject getJSONRepresentation() {
        //Setup json representation
        JSONObject representation = super.getJSONRepresentation();
        try {
            representation.put(ATTACK_KEY, getAttack());
            representation.put(DEFENSE_KEY, getDefense());
            representation.put(HEALTH_KEY, getHealth());
            representation.put(MOVEMENT_KEY, getMovement());
            representation.put(VISION_KEY, getVision());
            representation.put(ABILITIES_KEY, getAbilities());
            representation.put(STATUSES_KEY, getStatuses());
            representation.put(ACTION_POINTS_KEY, getActionPoints());
        } catch (JSONException e) {
            //Never will occur - all keys are non-null
        }
        return representation;
    }

    enum ATTACK_TYPE {
        SINGLE_ADJACENT_POINT,
        SINGLE_POINT,
        MULTIPLE_ADJACENT_POINT,
        MULTIPLE_POINT
    }
}
