package edu.cwru.eecs395_s16.core.objects;

import edu.cwru.eecs395_s16.interfaces.objects.Ability;
import edu.cwru.eecs395_s16.interfaces.objects.Location;
import edu.cwru.eecs395_s16.interfaces.objects.Weapon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HeroBuilder {
    private Weapon weapon;
    private int attack = 0;
    private int defense;
    private int health;
    private int mobility;
    private int vision;
    private List<Ability> abilities;
    private Location location;
    private Optional<String> ownerID;
    private UUID objectID;
    private int level;
    private int databaseIdentifier;

    public HeroBuilder setWeapon(Weapon weapon) {
        this.weapon = weapon;
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

    public HeroBuilder setMobility(int mobility) {
        this.mobility = mobility;
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

    public Hero createHero() {
        return new Hero(weapon, attack, defense, health, mobility, vision, abilities, location, ownerID, objectID, level, databaseIdentifier);
    }
}