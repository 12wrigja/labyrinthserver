package edu.cwru.eecs395_s16.services.heroes;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.LevelReward;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by james on 2/18/16.
 */
public class InMemoryHeroRepository implements HeroRepository {

    final Map<String, Set<Hero>> playerHeroMap = new ConcurrentHashMap<>();
    List<List<String>> heroTemplateData;
    final List<LevelReward> levelRewards = new ArrayList<>();
    final Map<String, HeroDefinition> heroTypeMap = new HashMap<>();

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
        for(HeroDefinition def : heroTypeMap.values()){
            HeroBuilder hb = new HeroBuilder(p.getUsername(), def)
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
    public List<LevelReward> getLevelRewards(HeroType type, long previousExperience, long newExperience) {
        return levelRewards.stream().filter(lr->lr.heroType == type && lr.expThreshold <= newExperience && lr.expThreshold > previousExperience).collect(Collectors.toList());
    }

    @Override
    public InternalResponseObject<HeroDefinition> getHeroDefinitionForId(int id) {
        Optional<HeroDefinition> definition = heroTypeMap.values().stream().filter(def->def.id == id).findFirst();
        if(definition.isPresent()){
            return new InternalResponseObject<>(definition.get(),Hero.HERO_TYPE_KEY);
        } else {
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    public InternalResponseObject<HeroDefinition> getHeroDefinitionForType(HeroType type) {
        if(heroTypeMap.containsKey(type.toString().toLowerCase())){
            return new InternalResponseObject<>(heroTypeMap.get(type.toString().toLowerCase()));
        } else {
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    public void initialize(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        heroTemplateData = CoreDataUtils.splitEntries(baseData.get("heroes"));
        for(List<String> heroTemplate : heroTemplateData){
            try{
                int id = heroTemplate.get(0).equals("default")?heroTypeMap.size()+1:Integer.parseInt(heroTemplate.get(0));
                HeroType heroType = HeroType.valueOf(heroTemplate.get(1).toUpperCase());
                int startAttack = Integer.parseInt(heroTemplate.get(2));
                int startDefense = Integer.parseInt(heroTemplate.get(3));
                int startHealth = Integer.parseInt(heroTemplate.get(4));
                int startVision = Integer.parseInt(heroTemplate.get(5));
                int startMovement = Integer.parseInt(heroTemplate.get(6));
                int defaultWeaponID = Integer.parseInt(heroTemplate.get(7));
                HeroDefinition definition = new HeroDefinition(id,heroType,startAttack,startDefense,startHealth,startMovement,startVision, defaultWeaponID);
                heroTypeMap.put(heroType.toString().toLowerCase(),definition);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to create definition for line: "+Arrays.toString(heroTemplate.toArray(new String[heroTemplate.size()])));
            }
        }
        List<List<String>> levelUpData = CoreDataUtils.splitEntries(baseData.get("levels"));
        for(List<String> rewardLine : levelUpData){

            int heroTypeID = Integer.parseInt(rewardLine.get(0));
            InternalResponseObject<HeroDefinition> typeResp = getHeroDefinitionForId(heroTypeID);
            if(!typeResp.isNormal()){
                continue;
            }
            HeroType type = typeResp.get().type;
            long expThreshold = Long.parseLong(rewardLine.get(1));
            int levelAwarded = Integer.parseInt(rewardLine.get(2));
            String rewardStr = rewardLine.get(3);
            levelRewards.add(buildReward(type,levelAwarded,expThreshold,rewardStr));
        }
    }

    @Override
    public void resetToDefaultData(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        playerHeroMap.clear();
        heroTemplateData.clear();
        initialize(baseData);
    }
}
