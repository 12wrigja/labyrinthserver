package edu.cwru.eecs395_s16.core.objects.heroes;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.objects.Ability;
import edu.cwru.eecs395_s16.interfaces.objects.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HeroBuilder {
    private Weapon weapon;
    private int attack = 0;
    private int defense = 0;
    private int health = 0;
    private int mobility = 0;
    private int vision = 0;
    private List<Ability> abilities = new ArrayList<>();
    private Location location = new Location(0,0);
    private Optional<String> ownerID = Optional.empty();
    private UUID objectID = UUID.randomUUID();
    private int level = 1;
    private int databaseIdentifier = -1;
    private HeroType type;

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

    public HeroBuilder setMovement(int movement) {
        this.mobility = movement;
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
        this.type = type;
        return this;
    }

    public Hero createHero() {
        return new Hero(weapon, attack, defense, health, mobility, vision, abilities, location, ownerID, objectID, level, databaseIdentifier, type);
    }
}