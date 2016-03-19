package edu.cwru.eecs395_s16.core.objects.heroes;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.objects.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.cert.PKIXRevocationChecker;
import java.util.*;

public class HeroBuilder {
    private UUID objectID = UUID.randomUUID();
    private int databaseIdentifier = -1;
    private Optional<String> ownerID = Optional.empty();
    private Optional<String> controllerID = Optional.empty();
    private HeroType type = HeroType.WARRIOR;
    private int level = 1;
    private long exp = 0;
    private Location location = new Location(0, 0);
    private int attack = 10;
    private int defense = 0;
    private int health = 50;
    private int maxHealth = 50;
    private int movement = 3;
    private int vision = 4;
    private int actionPoints = 2;
    private int maxActionPoints = 2;
    private Weapon weapon = new Weapon(-1, "fists", "Fists", "Use your fists!", 1, 1, new HashMap<Location, Float>() {
        {
            put(new Location(0, 0), 1.0f);
        }
    });
    private List<Ability> abilities = new ArrayList<>();
    private List<GameObjectStatus> statuses = new ArrayList<>();

    public HeroBuilder setGameObjectID(UUID objectID) {
        this.objectID = objectID;
        return this;
    }

    public HeroBuilder setDatabaseIdentifier(int databaseIdentifier) {
        this.databaseIdentifier = databaseIdentifier;
        return this;
    }

    public HeroBuilder setOwnerID(Optional<String> ownerID) {
        this.ownerID = ownerID;
        return this;
    }

    public HeroBuilder setControllerID(Optional<String> controllerID) {
        this.controllerID = controllerID;
        return this;
    }

    public HeroBuilder setHeroType(HeroType type) {
        this.type = type;
        return this;
    }

    public HeroBuilder setLevel(int level) {
        this.level = level;
        return this;
    }

    public HeroBuilder setExp(long exp) {
        this.exp = exp;
        return this;
    }

    public HeroBuilder setLocation(Location location) {
        this.location = location;
        return this;
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

    public HeroBuilder setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
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

    public HeroBuilder setActionPoints(int actionPoints) {
        this.actionPoints = actionPoints;
        return this;
    }

    public HeroBuilder setMaxActionPoints(int maxActionPoints) {
        this.maxActionPoints = maxActionPoints;
        return this;
    }

    public HeroBuilder setWeapon(Weapon weapon) {
        this.weapon = weapon;
        return this;
    }

    public HeroBuilder setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
        return this;
    }

    public HeroBuilder setStatuses(List<GameObjectStatus> statuses) {
        this.statuses = statuses;
        return this;
    }

    public HeroBuilder fillFromJSON(JSONObject obj) throws JSONException {
        //Object ID 
        setGameObjectID(UUID.fromString(obj.getString(GameObject.GAMEOBJECT_ID_KEY)));
        //Database ID 
        //TODO implement database ID storage
        //Owner ID
        setOwnerID(Optional.ofNullable(obj.getString(GameObject.OWNER_ID_KEY)));
        //Controller ID 
        setControllerID(Optional.ofNullable(obj.getString(GameObject.CONTROLLER_ID_KEY)));
        //Hero Type 
        setHeroType(HeroType.valueOf(obj.getString(Hero.HERO_TYPE_KEY).toUpperCase()));
        //Level 
        setLevel(obj.getInt(Hero.LEVEL_KEY));
        //Experience 
        setExp(obj.getLong(Hero.EXP_KEY));
        //Location 
        setLocation(new Location(obj.getInt(Location.X_KEY), obj.getInt(Location.Y_KEY)));
        //Attack 
        setAttack(obj.getInt(Creature.ATTACK_KEY));
        //Defense 
        setDefense(obj.getInt(Creature.DEFENSE_KEY));
        //Health 
        setHealth(obj.getInt(Creature.HEALTH_KEY));
        setMaxHealth(obj.getInt(Creature.MAX_HEALTH_KEY));
        //Movement 
        setMovement(obj.getInt(Creature.MOVEMENT_KEY));
        //Vision 
        setVision(obj.getInt(Creature.VISION_KEY));
        //Action Points 
        setActionPoints(obj.getInt(Creature.ACTION_POINTS_KEY));
        setMaxActionPoints(obj.getInt(Creature.MAX_ACTION_POINTS_KEY));
        //Weapon 
        JSONObject weaponObj = obj.getJSONObject(Hero.WEAPON_KEY);
        int weaponID = weaponObj.getInt(DatabaseObject.DATABASE_ID_KEY);
        Optional<Weapon> weapon = GameEngine.instance().services.weaponRepository.getWeaponForId(weaponID);
        if (weapon.isPresent()) {
            setWeapon(weapon.get());
        }
        //Abilities 
        //TODO set abilities 
        //Statuses 
        //TODO set statuses 
        return this;
    }


    public Hero createHero() {
        return new Hero(objectID, databaseIdentifier, ownerID, controllerID, type, level, exp, location, attack, defense, health, maxHealth, movement, vision, actionPoints, maxActionPoints, weapon, abilities, statuses);
    }
}