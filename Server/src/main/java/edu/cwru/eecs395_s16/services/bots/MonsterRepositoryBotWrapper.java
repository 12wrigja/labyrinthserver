package edu.cwru.eecs395_s16.services.bots;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.services.monsters.MonsterRepository;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by james on 4/11/16.
 */
public class MonsterRepositoryBotWrapper implements MonsterRepository {

    private final MonsterRepository actualRepo;

    public MonsterRepositoryBotWrapper(MonsterRepository actualRepo) {
        this.actualRepo = actualRepo;
    }

    @Override
    public InternalResponseObject<List<MonsterDefinition>> getPlayerMonsterTypes(Player p) {
        if(p instanceof GameBot){
            List<GameObject> architectObjects = ((GameBot)p).getArchitectObjects();
            List<Creature> allMonsters = architectObjects.stream().filter(obj->obj.getGameObjectType() == GameObject.TYPE.MONSTER).map(obj->(Creature)obj).collect(Collectors.toList());
            Map<MonsterDefinition,Integer> applicableDefinitions = new HashMap<>();
            allMonsters.forEach(c->{
                InternalResponseObject<MonsterDefinition> def = getMonsterDefinitionForId(c.getDatabaseID());
                if(def.isNormal()){
                    if(applicableDefinitions.containsKey(def.get())){
                        int oldCount = applicableDefinitions.get(def.get());
                        applicableDefinitions.put(def.get(),oldCount+1);
                    } else {
                        applicableDefinitions.put(def.get(),1);
                    }
                }
            });
            List<MonsterDefinition> actualDefinitions = applicableDefinitions.entrySet().stream().map(oldDef -> new MonsterDefinition(oldDef.getKey(), oldDef.getValue(), false)).collect(Collectors.toList());
            return new InternalResponseObject<>(actualDefinitions,"monsters");
        } else {
            return actualRepo.getPlayerMonsterTypes(p);
        }
    }

    @Override
    public InternalResponseObject<Boolean> addMonsterForPlayer(Player p, MonsterDefinition monsterDefinition, int quantity) {
        if(!(p instanceof GameBot)){
            return actualRepo.addMonsterForPlayer(p, monsterDefinition,quantity);
        } else {
            return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_USERNAME);
        }
    }

    @Override
    public InternalResponseObject<Boolean> createDefaultMonstersForPlayer(Player p) {
        if(!(p instanceof GameBot)){
            return actualRepo.createDefaultMonstersForPlayer(p);
        } else {
            return new InternalResponseObject<>(true,"created");
        }
    }

    @Override
    public InternalResponseObject<MonsterDefinition> getMonsterDefinitionForId(int id) {
        return actualRepo.getMonsterDefinitionForId(id);
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
