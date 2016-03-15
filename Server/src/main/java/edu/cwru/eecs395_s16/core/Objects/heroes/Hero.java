package edu.cwru.eecs395_s16.core.objects.heroes;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.objects.*;
import edu.cwru.eecs395_s16.interfaces.objects.Character;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/18/16.
 */
public class Hero implements Character, DatabaseObject {

    private Weapon weapon;
    private int attack = 10;
    private int defense = 10;
    private int health = 50;
    private int movement = 3;
    private int vision = 5;
    private int actionPoints = 2;
    private List<Ability> abilities = new ArrayList<>();
    private Location location = new Location(0,0);
    private final Optional<String> ownerID;
    private final UUID objectID;
    private int level = 1;
    private int databaseIdentifier = -1;
    public static final String HERO_TYPE_KEY = "hero_type";
    private final HeroType type;
    private final Optional<String> controllerID;

    Hero(UUID objectID, int databaseIdentifier, Optional<String> ownerID, Optional<String> controllerID, HeroType type, int level, Location location, int attack, int defense, int health, int movement, int vision, int actionPoints, Weapon weapon, List<Ability> abilities, List<String> statuses) {
        this.controllerID = controllerID;
        this.weapon = weapon;
        this.attack = attack;
        this.defense = defense;
        this.health = health;
        this.movement = movement;
        this.vision = vision;
        this.abilities = abilities;
        this.location = location;
        this.ownerID = ownerID;
        this.objectID = objectID;
        this.level = level;
        this.databaseIdentifier = databaseIdentifier;
        this.type = type;
        this.actionPoints = actionPoints;
    }

    @Override
    public Weapon getWeapon() {
        return this.weapon;
    }

    @Override
    public void setWeapon() {

    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void grantXP(int xp) {

    }

    @Override
    public int getAttack() {
        return this.attack;
    }

    @Override
    public int getDefense() {
        return this.defense;
    }

    @Override
    public int getHealth() {
        return this.health;
    }

    @Override
    public int getMovement() {
        return this.movement;
    }

    @Override
    public int getVision() {
        return this.vision;
    }

    @Override
    public List<Ability> getAbilities() {
        return this.abilities;
    }

    @Override
    public List<String> getStatuses() {
        return new ArrayList<>();
    }

    @Override
    public int getActionPoints() {
        return this.actionPoints;
    }

    @Override
    public void drainActionPoints() {
        this.actionPoints = 0;
    }

    @Override
    public void useActionPoint() {
        this.actionPoints--;
    }

    @Override
    public void resetActionPoints() {
        this.actionPoints = 2;
    }

    @Override
    public void triggerPassive(GameMap map, List<GameObject> boardObjects) {

    }

    @Override
    public TYPE getGameObjectType() {
        return TYPE.HERO;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public void setLocation(Location loc) {
        this.location = loc;
    }

    @Override
    public Optional<String> getOwnerID() {
        return this.ownerID;
    }

    @Override
    public UUID getGameObjectID() {
        return this.objectID;
    }

    @Override
    public Optional<String> getControllerID() {
        return this.controllerID;
    }

    @Override
    public int getDatabaseID() {
        return this.databaseIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hero hero = (Hero) o;

        return objectID.equals(hero.objectID);

    }


    public HeroType getHeroType() {
        return type;
    }

    @Override
    public int hashCode() {
        return objectID.hashCode();
    }

    @Override
    public JSONObject getJSONRepresentation(){
        //Setup json representation
        JSONObject representation = new JSONObject();
        try {
            representation.put(GAMEOBJECT_ID_KEY, getGameObjectID());
            representation.put(Location.X_KEY, location.getX());
            representation.put(Location.Y_KEY, location.getY());
            representation.put(GAMEOBJECT_TYPE_KEY, "hero");
            representation.put(ATTACK_KEY, getAttack());
            representation.put(DEFENSE_KEY, getDefense());
            representation.put(HEALTH_KEY, getHealth());
            representation.put(MOVEMENT_KEY, getMovement());
            representation.put(VISION_KEY, getVision());
            representation.put(ABILITIES_KEY, getAbilities());
            representation.put(STATUSES_KEY, new ArrayList<String>());
            representation.put(OWNER_ID_KEY, getOwnerID().isPresent() ? getOwnerID().get() : null);
            representation.put(CONTROLLER_ID_KEY, getOwnerID().isPresent() ? getOwnerID().get() : null);
            representation.put(HERO_TYPE_KEY, getHeroType().toString().toLowerCase());
            representation.put(LEVEL_KEY, getLevel());
            representation.put(ACTION_POINTS_KEY,getActionPoints());
            //TODO add in weapons
        } catch (JSONException e) {
            //Never will occur - all keys are non-null
        }
        return representation;
    }

}
