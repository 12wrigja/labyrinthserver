package edu.cwru.eecs395_s16.services;

import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.interfaces.repositories.HeroRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by james on 2/18/16.
 */
public class InMemoryHeroRepository implements HeroRepository {

    final Map<String, Hero> playerHeroMap = new ConcurrentHashMap<>();

    @Override
    public List<Hero> getPlayerHeroes(Player p) {
        Hero h = playerHeroMap.get(p.getUsername());
        if (h == null) {
            h = new HeroBuilder().setOwnerID(Optional.of(p.getUsername())).setControllerID(Optional.of(p.getUsername())).createHero();
            playerHeroMap.put(p.getUsername(), h);
        }
        List<Hero> heroList = new ArrayList<>();
        heroList.add(h);
        return heroList;
    }

    @Override
    public void saveHeroForPlayer(Player p, Hero h) {
        playerHeroMap.put(p.getUsername(),h);
    }
}
