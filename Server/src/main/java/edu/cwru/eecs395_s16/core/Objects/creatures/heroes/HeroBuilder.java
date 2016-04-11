package edu.cwru.eecs395_s16.core.objects.creatures.heroes;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.*;
import edu.cwru.eecs395_s16.core.objects.creatures.*;
import edu.cwru.eecs395_s16.services.heroes.HeroRepository;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class HeroBuilder extends CreatureBuilder {

    private HeroType type = HeroType.WARRIOR;
    private int level = 1;
    private long exp = 0;
    private boolean applyExpRewards = false;

    public HeroBuilder(UUID objectID, String ownerID, Optional<String> controllerID, int databaseID, HeroRepository.HeroDefinition heroDef) {
        super(objectID, databaseID, ownerID, controllerID);
        setObjectType(GameObject.TYPE.HERO);
        setInitialValuesFromHeroDefinition(heroDef);
    }

    public HeroBuilder(UUID objectID, String ownerID, Optional<String> controllerID, int databaseID, HeroType type) {
        this(objectID, ownerID, controllerID, databaseID, GameEngine.instance().services.heroRepository.getHeroDefinitionForType(type).get());
    }

    public HeroBuilder(String ownerID, HeroType type) {
        this(UUID.randomUUID(), ownerID, Optional.of(ownerID), -1, type);
    }

    private void setInitialValuesFromHeroDefinition(HeroRepository.HeroDefinition def) {
        setAttack(def.startAttack);
        setDefense(def.startDefense);
        setHealth(def.startHealth);
        setMaxHealth(def.startHealth);
        setVision(def.startVision);
        setMovement(def.startMovement);
        setHeroType(def.type);
        setWeapon(GameEngine.instance().services.heroItemRepository.getWeaponForId(def.defaultWeaponId).get());
    }

    public HeroBuilder setHeroType(HeroType type) {
        this.type = type;
        return this;
    }

    @Override
    public HeroBuilder setAttack(int attack) {
        super.setAttack(attack);
        return this;
    }

    @Override
    public HeroBuilder setDefense(int defense) {
        super.setDefense(defense);
        return this;
    }

    @Override
    public HeroBuilder setHealth(int currentHealth) {
        super.setHealth(currentHealth);
        return this;
    }

    @Override
    public HeroBuilder setMaxHealth(int maxHealth) {
        super.setMaxHealth(maxHealth);
        return this;
    }

    @Override
    public HeroBuilder setMovement(int movement) {
        super.setMovement(movement);
        return this;
    }

    @Override
    public HeroBuilder setVision(int vision) {
        super.setVision(vision);
        return this;
    }

    @Override
    public HeroBuilder setActionPoints(int currentActionPoints) {
        super.setActionPoints(currentActionPoints);
        return this;
    }

    @Override
    public HeroBuilder setMaxActionPoints(int maxActionPoints) {
        super.setMaxActionPoints(maxActionPoints);
        return this;
    }

    @Override
    public HeroBuilder setAbilities(List<Ability> abilities) {
        super.setAbilities(abilities);
        return this;
    }

    @Override
    public HeroBuilder setStatuses(List<CreatureStatus> statuses) {
        super.setStatuses(statuses);
        return this;
    }

    @Override
    public HeroBuilder setLocation(Location location) {
        super.setLocation(location);
        return this;
    }

    @Override
    public HeroBuilder setWeapon(Weapon weapon) {
        super.setWeapon(weapon);
        return this;
    }

    public HeroBuilder setLevel(int level) {
        this.level = level;
        return this;
    }

    public HeroBuilder setExp(long exp, boolean applyRewards) {
        this.exp = exp;
        this.applyExpRewards = applyRewards;
        return this;
    }

    public HeroBuilder fillFromJSON(JSONObject obj) throws JSONException {
        super.fillFromJSON(obj);
        //Hero Type 
        setHeroType(HeroType.valueOf(obj.getString(Hero.HERO_TYPE_KEY).toUpperCase()));
        //Level 
        setLevel(obj.getInt(Hero.LEVEL_KEY));
        //Experience 
        setExp(obj.getLong(Hero.EXP_KEY), false);
        return this;
    }

    public Hero createHero() {
        Hero h = new Hero(objectID, databaseIdentifier, ownerID, controllerID, type, level, exp, location, attack, defense, health, maxHealth, movement, vision, actionPoints, maxActionPoints, weapon, abilities, statuses);
        if (this.applyExpRewards) {
            List<LevelReward> rewards = GameEngine.instance().services.heroRepository.getLevelRewards(type, 0, exp);
            rewards.sort((r1, r2) -> Integer.compare(r1.levelApplied, r2.levelApplied));
            for (LevelReward r : rewards) {
                r.apply(h);
            }
        }
        return h;
    }
}