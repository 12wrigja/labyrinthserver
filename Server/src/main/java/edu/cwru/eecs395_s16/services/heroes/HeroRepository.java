package edu.cwru.eecs395_s16.services.heroes;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.LevelReward;
import edu.cwru.eecs395_s16.core.objects.creatures.StatChangeLevelReward;
import edu.cwru.eecs395_s16.services.containers.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by james on 2/18/16.
 */
public interface HeroRepository extends Repository {

    InternalResponseObject<List<Hero>> getPlayerHeroes(Player p);

    InternalResponseObject<Boolean> saveHeroForPlayer(Player p, Hero h);

    InternalResponseObject<Boolean> createDefaultHeroesForPlayer(Player p);

    List<LevelReward> getLevelRewards(HeroType type, long previousExperience, long newExperience);

    InternalResponseObject<HeroType> getHeroTypeForId(int id);

    default Optional<LevelReward> buildReward(HeroType type, int level, long experience, String rewardStr){
        //Here we differentiate between reward types
        try{
            return Optional.of(new StatChangeLevelReward(level, experience,type, rewardStr));
        } catch (IllegalArgumentException e){
            return Optional.empty();
        }
    }

}
