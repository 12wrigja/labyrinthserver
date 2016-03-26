package edu.cwru.eecs395_s16.core.objects.creatures;

import edu.cwru.eecs395_s16.core.objects.*;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
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
    public static final String MAX_HEALTH_KEY = "max_health";
    public static final String MOVEMENT_KEY = "movement";
    public static final String VISION_KEY = "vision";
    public static final String ABILITIES_KEY = "abilities";
    public static final String STATUSES_KEY = "statuses";
    public static final String ACTION_POINTS_KEY = "action_points";
    public static final String MAX_ACTION_POINTS_KEY = "max_action_points";
    private int attack = 10;
    private int defense = 10;
    private int health = 50;
    private int maxHealth = 50;
    private int movement = 3;
    private int vision = 5;
    private int maxActionPoints = 2;
    private int currentActionPoints;
    private List<Ability> abilities = new ArrayList<>();
    private List<CreatureStatus> statuses = new ArrayList<>();

    public Creature(UUID objectID, Optional<String> ownerID, Optional<String> controllerID, TYPE objectType, int attack, int defense, int currentHealth, int maxHealth, int movement, int vision, int currentActionPoints, int maxActionPoints, List<Ability> abilities, List<CreatureStatus> statuses, Location location, Weapon weapon) {
        super(objectID, ownerID, controllerID, objectType, location);
        this.attack = attack;
        this.defense = defense;
        this.health = currentHealth;
        this.maxHealth = maxHealth;
        this.movement = movement;
        this.vision = vision;
        this.maxActionPoints = maxActionPoints;
        this.currentActionPoints = currentActionPoints;
        this.abilities = abilities;
        this.statuses = statuses;
        this.weapon = weapon;
    }

    public static final String WEAPON_KEY = "weapon";
    private Weapon weapon;

    public Weapon getWeapon() {
        return this.weapon;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
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

    protected void setAttack(int attack) {
        this.attack = attack;
    }

    protected void setDefense(int defense) {
        this.defense = defense;
    }

    protected void setHealth(int health) {
        this.health = health;
    }

    protected void setMaxHealth(int maxHealth){
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void setMovement(int movement) {
        this.movement = movement;
    }

    protected void setVision(int vision) {
        this.vision = vision;
    }

    protected void setCurrentActionPoints(int currentActionPoints) {
        this.currentActionPoints = currentActionPoints;
    }

    protected void setStatuses(List<CreatureStatus> statuses) {
        this.statuses = statuses;
    }

    protected void setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
    }

    public List<Ability> getAbilities() {
        return abilities;
    }

    public void triggerPassive(GameMap map, GameObjectCollection boardObjects) {
    }

    public List<CreatureStatus> getStatuses() {
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

    public void attackTarget(Creature target, int computedDamage) {
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
            representation.put(MAX_ACTION_POINTS_KEY, maxActionPoints);
            representation.put(MAX_HEALTH_KEY, maxHealth);
        } catch (JSONException e) {
            //Never will occur - all keys are non-null
        }
        return representation;
    }
}