package edu.cwru.eecs395_s16.services.bots;

import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.heroes.LevelReward;
import edu.cwru.eecs395_s16.interfaces.repositories.HeroRepository;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by james on 3/16/16.
 */
public class HeroRepositoryBotWrapper implements HeroRepository {

    private final HeroRepository actualRepo;

    public HeroRepositoryBotWrapper(HeroRepository actualRepo) {
        this.actualRepo = actualRepo;
    }

    @Override
    public InternalResponseObject<List<Hero>> getPlayerHeroes(Player p) {
        if(p instanceof GameBot){
            return new InternalResponseObject<>(((GameBot)p).getBotsHeroes());
        } else {
            return actualRepo.getPlayerHeroes(p);
        }
    }

    @Override
    public InternalResponseObject<Boolean> saveHeroForPlayer(Player p, Hero h) {
        if(p instanceof GameBot){
            return new InternalResponseObject<>(true,"saved");
        } else {
            return actualRepo.saveHeroForPlayer(p,h);
        }
    }

    @Override
    public InternalResponseObject<Boolean> createDefaultHeroesForPlayer(Player p) {
        if(p instanceof GameBot){
            return new InternalResponseObject<>(true,"created");
        } else {
            return actualRepo.createDefaultHeroesForPlayer(p);
        }
    }

    @Override
    public List<LevelReward> getLevelRewards(HeroType type, int level) {
        return actualRepo.getLevelRewards(type,level);
    }

    @Override
    public void initialize(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        actualRepo.initialize(baseData);
    }

    @Override
    public void resetToDefaultData(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        actualRepo.resetToDefaultData(baseData);
    }
}
