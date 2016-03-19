package edu.cwru.eecs395_s16.services.herorepository;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroType;
import edu.cwru.eecs395_s16.interfaces.repositories.HeroRepository;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by james on 2/18/16.
 */
public class InMemoryHeroRepository implements HeroRepository {

    final Map<String, Set<Hero>> playerHeroMap = new ConcurrentHashMap<>();

    @Override
    public InternalResponseObject<List<Hero>> getPlayerHeroes(Player p) {
        if (playerHeroMap.containsKey(p.getUsername())) {
            List<Hero> heroList = new ArrayList<>();
            heroList.addAll(playerHeroMap.get(p.getUsername()));
            return new InternalResponseObject<>(heroList, "heroes");
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.UNKNOWN_USERNAME);
        }
    }

    @Override
    public InternalResponseObject<Boolean> saveHeroForPlayer(Player p, Hero h) {
        Set<Hero> heroSet;
        if (!playerHeroMap.containsKey(p.getUsername())) {
            heroSet = new HashSet<>();
            playerHeroMap.put(p.getUsername(),heroSet);
        } else {
            heroSet = playerHeroMap.get(p.getUsername());
        }
        if (heroSet.contains(h)) {
            heroSet.remove(h);
        }
        heroSet.add(h);
        return new InternalResponseObject<>(true,"saved");
    }

    @Override
    public InternalResponseObject<Boolean> createDefaultHeroesForPlayer(Player p) {
        Hero h = new HeroBuilder().setWeapon(GameEngine.instance().services.weaponRepository.getWeaponForId(0).get()).setOwnerID(Optional.of(p.getUsername())).createHero();
        InternalResponseObject<Boolean> resp = saveHeroForPlayer(p,h);
        if(!resp.isNormal()){
            return resp;
        }
        Hero h2 = new HeroBuilder().setHeroType(HeroType.WARRIORROGUE).setWeapon(GameEngine.instance().services.weaponRepository.getWeaponForId(1).get()).setOwnerID(Optional.of(p.getUsername())).createHero();
        resp = saveHeroForPlayer(p,h2);
        return resp;
    }
}
