package edu.cwru.eecs395_s16.core.objects.heroes;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.objects.Ability;
import edu.cwru.eecs395_s16.interfaces.objects.Creature;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.interfaces.objects.Weapon;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HeroBuilder {
    private Optional<Weapon> weapon = Optional.empty();
    private int attack = 10;
    private int defense = 10;
    private int health = 50;
    private int movement = 3;
    private int vision = 5;
    private int actionPoints = 2;
    private List<Ability> abilities = new ArrayList<>();
    private Location location = new Location(0,0);
    private Optional<String> ownerID = Optional.empty();
    private Optional<String> controllerID = Optional.empty();
    private UUID objectID = UUID.randomUUID();
    private int level = 1;
    private long exp = 0;
    private int databaseIdentifier = -1;
    private HeroType heroType = HeroType.WARRIOR;
    private GameObject.TYPE type = GameObject.TYPE.HERO;

    public HeroBuilder setWeapon(Optional<Weapon> weapon) {
        this.weapon = weapon;
        return this;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public HeroBuilder setAttack(int attack) {
        this.attack = attack;
        return this;
    }

    public HeroBuilder setDefense(int defense) {
        this.defense = defense;
        return this;
    }

    public HeroBuilder setHealth(int health) {
        this.health = health;
        return this;
    }

    public HeroBuilder setMovement(int movement) {
        this.movement = movement;
        return this;
    }

    public HeroBuilder setVision(int vision) {
        this.vision = vision;
        return this;
    }

    public HeroBuilder setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
        return this;
    }

    public HeroBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    public HeroBuilder setOwnerID(Optional<String> ownerID) {
        this.ownerID = ownerID;
        return this;
    }

    public HeroBuilder setObjectID(UUID objectID) {
        this.objectID = objectID;
        return this;
    }

    public HeroBuilder setLevel(int level) {
        this.level = level;
        return this;
    }

    public HeroBuilder setDatabaseIdentifier(int databaseIdentifier) {
        this.databaseIdentifier = databaseIdentifier;
        return this;
    }

    public HeroBuilder setHeroType(HeroType type){
        this.heroType = type;
        return this;
    }

    public HeroBuilder setControllerID(Optional<String> controllerID) {
        this.controllerID = controllerID;
        return this;
    }

    public void setActionPoints(int actionPoints) {
        this.actionPoints = actionPoints;
    }

    public HeroBuilder fillFromJSON(JSONObject obj) throws JSONException {
        //Object ID
        setObjectID(UUID.fromString(obj.getString(GameObject.GAMEOBJECT_ID_KEY)));
        //Database ID
        //TODO implement database ID storage
        //Owner ID
        setOwnerID(Optional.ofNullable(obj.getString(GameObject.OWNER_ID_KEY)));
        //Controller ID
        setControllerID(Optional.ofNullable(obj.getString(GameObject.CONTROLLER_ID_KEY)));
        //Game Object Type
        setGameObjectType(GameObject.TYPE.valueOf(obj.getString(GameObject.GAMEOBJECT_TYPE_KEY).toUpperCase()));
        //Hero Type
        setHeroType(HeroType.valueOf(obj.getString(Hero.HERO_TYPE_KEY).toUpperCase()));
        //Level
        setLevel(obj.getInt(Hero.LEVEL_KEY));
        //Experience
        setExp(obj.getLong(Hero.EXP_KEY));
        //Location
        setLocation(new Location(obj.getInt(Location.X_KEY),obj.getInt(Location.Y_KEY)));
        //Attack
        setAttack(obj.getInt(Creature.ATTACK_KEY));
        //Defense
        setDefense(obj.getInt(Creature.DEFENSE_KEY));
        //Health
        setHealth(obj.getInt(Creature.HEALTH_KEY));
        //Movement
        setMovement(obj.getInt(Creature.MOVEMENT_KEY));
        //Vision
        setVision(obj.getInt(Creature.VISION_KEY));
        //Action Points
        setActionPoints(obj.getInt(Creature.ACTION_POINTS_KEY));
        //TODO implement setting weapon from ID
        //Weapon
//        String weaponID = obj.getString(Hero.);
        //Abilities
        //TODO set abilities
        //Statuses
        //TODO set statuses
        return this;
    }

    public Hero createHero() {
        return new Hero(objectID, databaseIdentifier, ownerID, controllerID, heroType, level, exp, location, attack, defense, health, movement, vision, actionPoints, weapon, abilities, new ArrayList<>());
    }

    public void setGameObjectType(GameObject.TYPE type) {
        this.type = type;
    }
}