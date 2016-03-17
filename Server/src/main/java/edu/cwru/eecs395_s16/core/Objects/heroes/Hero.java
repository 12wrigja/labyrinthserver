package edu.cwru.eecs395_s16.core.objects.heroes;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.objects.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/18/16.
 */
public class Hero extends Creature implements DatabaseObject {

    public static final String HERO_TYPE_KEY = "hero_type";
    public static final String LEVEL_KEY = "level";
    public static final String EXP_KEY = "experience";
    private final HeroType type;
    private Optional<Weapon> weapon;
    private int level = 1;
    private long exp = 0;
    private int databaseIdentifier = -1;

    Hero(UUID objectID, int databaseIdentifier, Optional<String> ownerID, Optional<String> controllerID, HeroType type, int level, long exp, Location location, int attack, int defense, int health, int movement, int vision, int actionPoints, Optional<Weapon> weapon, List<Ability> abilities, List<GameObjectStatus> statuses) {
        super(objectID, ownerID, controllerID, TYPE.HERO, attack, defense, health, movement, vision, actionPoints, abilities, statuses);
        this.weapon = weapon;
        this.level = level;
        this.databaseIdentifier = databaseIdentifier;
        this.type = type;
        this.exp = exp;
    }

    public Optional<Weapon> getWeapon() {
        return this.weapon;
    }

    public void setWeapon(Optional<Weapon> weapon) {
        this.weapon = weapon;
    }

    public int getLevel() {
        return this.level;
    }

    public void grantXP(long xp) {
        this.exp += xp;
        //TODO check for level threshold
    }

    public long getExp() {
        return exp;
    }

    @Override
    public int getDatabaseID() {
        return this.databaseIdentifier;
    }

    public HeroType getHeroType() {
        return type;
    }

    @Override
    public int hashCode() {
        return getGameObjectID().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hero hero = (Hero) o;

        return getGameObjectID().equals(hero.getGameObjectID());

    }

    @Override
    public JSONObject getJSONRepresentation() {
        //Setup json representation
        JSONObject representation = super.getJSONRepresentation();
        try {
            representation.put(HERO_TYPE_KEY, getHeroType().toString().toLowerCase());
            representation.put(LEVEL_KEY, getLevel());
            representation.put(EXP_KEY, getExp());
            //TODO add in weapons
        } catch (JSONException e) {
            //Never will occur - all keys are non-null
        }
        return representation;
    }

    @Override
    public int getBasicAttackDamage() {
        if(weapon.isPresent()){
            return weapon.get().getDamageModifier() * getAttack();
        } else {
            return super.getBasicAttackDamage();
        }
    }

    @Override
    public InternalResponseObject<Boolean> validListOfBasicAttackTargets(List<Creature> targets) {
        if(weapon.isPresent()){
            Weapon wep = weapon.get();
            for(Creature target : targets){
                if(!wep.isAttackLocationValid(this,target)){
                    return new InternalResponseObject<>(InternalErrorCode.NOT_IN_RANGE);
                }
            }
            return new InternalResponseObject<>(true,"valid");
        } else {
            return super.validListOfBasicAttackTargets(targets);
        }
    }

    @Override
    public void attackTarget(Creature target, int computedDamage) {
        if(weapon.isPresent()){
            float percentageOfDamage = weapon.get().getDamagePercentageForLocation(target.getLocation());
            int actualDamage = (int)Math.floor(percentageOfDamage*computedDamage);
            target.takeDamage(actualDamage);
        } else {
            super.attackTarget(target,computedDamage);
        }
    }
}
