package edu.cwru.eecs395_s16.core.objects.heroes;

import edu.cwru.eecs395_s16.core.objects.BasicLocation;
import edu.cwru.eecs395_s16.interfaces.objects.*;
import edu.cwru.eecs395_s16.interfaces.objects.Character;

import java.util.*;

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
    private List<Ability> abilities = new ArrayList<>();
    private Location location = new BasicLocation(0,0);
    private final Optional<String> ownerID;
    private final UUID objectID;
    private int level = 1;
    private int databaseIdentifier = -1;
    private final HeroType type;

    Hero(Weapon weapon, int attack, int defense, int health, int movement, int vision, List<Ability> abilities, Location location, Optional<String> ownerID, UUID objectID, int level, int databaseIdentifier, HeroType type) {
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
    public void triggerPassive(GameMap map, List<GameObject> boardObjects) {

    }

    @Override
    public Location getLocation() {
        return this.location;
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
    public Map<String, Object> getJsonableRepresentation() {
        //Setup json representation
        Map<String,Object> representation = new HashMap<>();
        representation.put("id",getGameObjectID());
        representation.put("x",location.getX());
        representation.put("y",location.getY());
        representation.put("type","hero");
        representation.put("attack",getAttack());
        representation.put("defense",getDefense());
        representation.put("health",getHealth());
        representation.put("movement",getMovement());
        representation.put("vision",getVision());
        representation.put("abilities",getAbilities());
        representation.put("statuses",new ArrayList<String>());
        representation.put("owner",getOwnerID().isPresent()?getOwnerID().get():null);
        representation.put("controller",getOwnerID().isPresent()?getOwnerID().get():null);
        representation.put("hero_type",getHeroType().toString().toLowerCase());
        return representation;
    }

}
