package edu.cwru.eecs395_s16.core.objects.heroes;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.objects.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
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

    private int level = 1;
    private long exp = 0;
    private int databaseIdentifier = -1;

    Hero(UUID objectID, int databaseIdentifier, Optional<String> ownerID, Optional<String> controllerID, HeroType type, int level, long exp, Location location, int attack, int defense, int health, int maxHealth, int movement, int vision, int actionPoints, int maxActionPoints, Weapon weapon, List<Ability> abilities, List<GameObjectStatus> statuses) {
        super(objectID, ownerID, controllerID, TYPE.HERO, attack, defense, health, maxHealth, movement, vision, actionPoints, maxActionPoints, abilities, statuses, location, weapon);
        this.level = level;
        this.databaseIdentifier = databaseIdentifier;
        this.type = type;
        this.exp = exp;
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
    public JSONObject getJSONRepresentation() {
        //Setup json representation
        JSONObject representation = super.getJSONRepresentation();
        try {
            representation.put(HERO_TYPE_KEY, getHeroType().toString().toLowerCase());
            representation.put(LEVEL_KEY, getLevel());
            representation.put(EXP_KEY, getExp());
            representation.put(WEAPON_KEY, getWeapon().getJSONRepresentation());
        } catch (JSONException e) {
            //Never will occur - all keys are non-null
        }
        return representation;
    }

}
