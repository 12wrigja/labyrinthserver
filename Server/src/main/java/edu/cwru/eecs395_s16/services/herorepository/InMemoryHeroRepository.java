package edu.cwru.eecs395_s16.services.herorepository;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.heroes.LevelReward;
import edu.cwru.eecs395_s16.interfaces.repositories.HeroRepository;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by james on 2/18/16.
 */
public class InMemoryHeroRepository implements HeroRepository {

    final Map<String, Set<Hero>> playerHeroMap = new ConcurrentHashMap<>();
    List<List<String>> heroTemplateData;

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
            playerHeroMap.put(p.getUsername(), heroSet);
        } else {
            heroSet = playerHeroMap.get(p.getUsername());
        }
        if (heroSet.contains(h)) {
            heroSet.remove(h);
        }
        heroSet.add(h);
        return new InternalResponseObject<>(true, "saved");
    }

    @Override
    public InternalResponseObject<Boolean> createDefaultHeroesForPlayer(Player p) {
        for(List<String> lst : heroTemplateData){
            HeroBuilder hb = new HeroBuilder()
                    .setHeroType(HeroType.valueOf(lst.get(1).toUpperCase()))
                    .setAttack(Integer.parseInt(lst.get(2)))
                    .setDefense(Integer.parseInt(lst.get(3)))
                    .setHealth(Integer.parseInt(lst.get(4)))
                    .setMaxHealth(Integer.parseInt(lst.get(4)))
                    .setMovement(Integer.parseInt(lst.get(5)))
                    .setVision(Integer.parseInt(lst.get(6)))
                    .setWeapon(GameEngine.instance().services.heroItemRepository.getWeaponForId(Integer.parseInt(lst.get(7))).get())
                    .setOwnerID(Optional.of(p.getUsername()))
                    .setControllerID(Optional.of(p.getUsername()))
                    .setDatabaseIdentifier(-1);
            InternalResponseObject<Boolean> resp = saveHeroForPlayer(p,hb.createHero());
            if(!resp.isNormal()){
                return resp;
            }
        }
        return new InternalResponseObject<>(true,"created");
    }

    @Override
    public List<LevelReward> getLevelRewards(HeroType type, int level) {
        return null;
    }

    @Override
    public void initialize(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        heroTemplateData = CoreDataUtils.splitEntries(baseData.get("heroes"));
    }

    @Override
    public void resetToDefaultData(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        playerHeroMap.clear();
        heroTemplateData.clear();
        initialize(baseData);
    }
}
