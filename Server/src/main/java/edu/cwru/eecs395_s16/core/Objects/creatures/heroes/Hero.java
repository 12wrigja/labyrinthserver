package edu.cwru.eecs395_s16.core.objects.creatures.heroes;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.*;
import edu.cwru.eecs395_s16.core.objects.creatures.Ability;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.CreatureStatus;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
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

    Hero(UUID objectID, int databaseIdentifier, String ownerID, Optional<String> controllerID, HeroType type, int level, long exp, Location location, int attack, int defense, int health, int maxHealth, int movement, int vision, int actionPoints, int maxActionPoints, Weapon weapon, List<Ability> abilities, List<CreatureStatus> statuses) {
        super(objectID, Optional.of(ownerID), controllerID, databaseIdentifier, TYPE.HERO, attack, defense, health, maxHealth, movement, vision, actionPoints, maxActionPoints, abilities, statuses, location, weapon);
        this.level = level;
        this.type = type;
        this.exp = exp;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public InternalResponseObject<Boolean> grantXP(long xp) {
        long previousExp = exp;
        long newExp = exp+xp;
        List<LevelReward> levels = GameEngine.instance().services.heroRepository.getLevelRewards(type,previousExp,newExp);
        levels.sort((lr1,lr2)->Long.compare(lr1.expThreshold,lr2.expThreshold));
        this.exp = newExp;
        for(LevelReward reward : levels){
            reward.apply(this);
        }
        //Save hero stuff here hopefully.
        InternalResponseObject<Player> playerResp = GameEngine.instance().services.playerRepository.findPlayer(getOwnerID().get());
        if(!playerResp.isNormal()){
            return InternalResponseObject.cloneError(playerResp);
        }
        InternalResponseObject<Boolean> saveResp = GameEngine.instance().services.heroRepository.saveHeroForPlayer(playerResp.get(),this);
        if(!saveResp.isNormal()){
            return saveResp;
        }
        return new InternalResponseObject<>(true,"updated");
    }

    public long getExp() {
        return exp;
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
