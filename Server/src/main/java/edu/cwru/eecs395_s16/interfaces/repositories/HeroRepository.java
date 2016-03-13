package edu.cwru.eecs395_s16.interfaces.repositories;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;

import java.util.List;

/**
 * Created by james on 2/18/16.
 */
public interface HeroRepository {

    InternalResponseObject<List<Hero>> getPlayerHeroes(Player p);

    void saveHeroForPlayer(Player p, Hero h);

}
