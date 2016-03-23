package edu.cwru.eecs395_s16.interfaces.repositories;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.heroes.LevelReward;

import java.util.List;

/**
 * Created by james on 2/18/16.
 */
public interface HeroRepository extends Repository {

    InternalResponseObject<List<Hero>> getPlayerHeroes(Player p);

    InternalResponseObject<Boolean> saveHeroForPlayer(Player p, Hero h);

    InternalResponseObject<Boolean> createDefaultHeroesForPlayer(Player p);

    List<LevelReward> getLevelRewards(HeroType type, int level);

}
