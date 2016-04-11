package edu.cwru.eecs395_s16.core.objects.creatures;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.DatabaseObject;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.GameObjectFactory;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CreatureBuilder extends GameObjectFactory {

    protected int databaseIdentifier = -1;
    protected int attack = 10;
    protected int defense = 0;
    protected int health = 50;
    protected int maxHealth = 50;
    protected int movement = 3;
    protected int vision = 4;
    protected int actionPoints = 2;
    protected int maxActionPoints = 2;
    protected Weapon weapon = Weapon.DEFAULT_NO_WEAPON;
    protected List<Ability> abilities = new ArrayList<>();
    protected List<CreatureStatus> statuses = new ArrayList<>();

    public CreatureBuilder(UUID objectID, int databaseIdentifier, String ownerID, Optional<String> controllerID) {
        this.objectID = objectID;
        this.databaseIdentifier = databaseIdentifier;
        this.ownerID = ownerID;
        this.controllerID = controllerID;
        this.objectType = GameObject.TYPE.MONSTER;
        this.location = new Location(0,0);
    }

    public CreatureBuilder setAttack(int attack) {
        this.attack = attack;
        return this;
    }

    public CreatureBuilder setDefense(int defense) {
        this.defense = defense;
        return this;
    }

    public CreatureBuilder setHealth(int currentHealth) {
        this.health = currentHealth;
        return this;
    }

    public CreatureBuilder setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        return this;
    }

    public CreatureBuilder setMovement(int movement) {
        this.movement = movement;
        return this;
    }

    public CreatureBuilder setVision(int vision) {
        this.vision = vision;
        return this;
    }

    public CreatureBuilder setActionPoints(int currentActionPoints) {
        this.actionPoints = currentActionPoints;
        return this;
    }

    public CreatureBuilder setMaxActionPoints(int maxActionPoints) {
        this.maxActionPoints = maxActionPoints;
        return this;
    }

    public CreatureBuilder setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
        return this;
    }

    public CreatureBuilder setStatuses(List<CreatureStatus> statuses) {
        this.statuses = statuses;
        return this;
    }

    public CreatureBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    public CreatureBuilder setWeapon(Weapon weapon) {
        this.weapon = weapon;
        return this;
    }

    public CreatureBuilder setObjectType(GameObject.TYPE objectType) {
        this.objectType = objectType;
        return this;
    }

    public CreatureBuilder fillFromJSON(JSONObject obj) throws JSONException {
        //Object ID 
        objectID = UUID.fromString(obj.getString(GameObject.GAMEOBJECT_ID_KEY));
        //Database ID 
        databaseIdentifier = obj.getInt(DatabaseObject.DATABASE_ID_KEY);
        //Controller ID 
        controllerID = Optional.ofNullable(obj.getString(GameObject.CONTROLLER_ID_KEY));
        //Location 
        location = new Location(obj.getInt(Location.X_KEY), obj.getInt(Location.Y_KEY));
        //Attack 
        attack = obj.getInt(Creature.ATTACK_KEY);
        //Defense 
        defense = obj.getInt(Creature.DEFENSE_KEY);
        //Health 
        health = obj.getInt(Creature.HEALTH_KEY);
        maxHealth = obj.getInt(Creature.MAX_HEALTH_KEY);
        //Movement 
        movement = obj.getInt(Creature.MOVEMENT_KEY);
        //Vision 
        vision = obj.getInt(Creature.VISION_KEY);
        //Action Points 
        actionPoints = obj.getInt(Creature.ACTION_POINTS_KEY);
        maxActionPoints = obj.getInt(Creature.MAX_ACTION_POINTS_KEY);
        //Weapon 
        JSONObject weaponObj = obj.getJSONObject(Hero.WEAPON_KEY);
        int weaponID = weaponObj.getInt(DatabaseObject.DATABASE_ID_KEY);
        Optional<Weapon> wOpt = GameEngine.instance().services.heroItemRepository.getWeaponForId(weaponID);
        if (wOpt.isPresent()) {
            weapon = wOpt.get();
        }
        return this;
    }

    public Creature createCreature() {
        return new Creature(objectID, Optional.of(ownerID), controllerID, databaseIdentifier, objectType, attack, defense, health, maxHealth, movement, vision, actionPoints, maxActionPoints, abilities, statuses, location, weapon);
    }
}