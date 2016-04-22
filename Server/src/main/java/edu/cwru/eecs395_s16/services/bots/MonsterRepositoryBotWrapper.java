package edu.cwru.eecs395_s16.services.bots;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterDefinition;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.services.monsters.MonsterRepository;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.util.*;
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
        if (p instanceof GameBot) {
            List<GameObject> architectObjects = ((GameBot) p).getArchitectObjects();
            List<Monster> allMonsters = architectObjects.stream().filter(obj -> obj.getGameObjectType() == GameObject.TYPE.MONSTER).map(obj -> (Monster) obj).collect(Collectors.toList());
            Map<MonsterDefinition, Integer> applicableDefinitions = new HashMap<>();
            for (Monster m : allMonsters) {
                MonsterDefinition def;
                if (m.getDatabaseID() >= 0) {
                    InternalResponseObject<MonsterDefinition> defResp = getMonsterDefinitionForId(m.getDatabaseID());
                    if (defResp.isNormal()) {
                        def = defResp.get();
                    } else {
                        return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR, "Unable to find monster definition for id: " + m.getDatabaseID());
                    }
                } else {
                    def = new MonsterBuilder(UUID.randomUUID(), m).createMonsterDefinition(1);
                }
                if (applicableDefinitions.containsKey(def)) {
                    int oldCount = applicableDefinitions.get(def);
                    applicableDefinitions.put(def, oldCount + 1);
                } else {
                    applicableDefinitions.put(def, 1);
                }
            }
            List<MonsterDefinition> actualDefinitions = applicableDefinitions.entrySet().stream().map(oldDef -> new MonsterDefinition(oldDef.getKey(), oldDef.getValue(), false)).collect(Collectors.toList());
            return new InternalResponseObject<>(actualDefinitions, "monsters");
        } else {
            return actualRepo.getPlayerMonsterTypes(p);
        }
    }

    @Override
    public InternalResponseObject<Boolean> addMonsterForPlayer(Player p, MonsterDefinition monsterDefinition, int quantity) {
        if (!(p instanceof GameBot)) {
            return actualRepo.addMonsterForPlayer(p, monsterDefinition, quantity);
        } else {
            return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_USERNAME);
        }
    }

    @Override
    public InternalResponseObject<Boolean> createDefaultMonstersForPlayer(Player p) {
        if (!(p instanceof GameBot)) {
            return actualRepo.createDefaultMonstersForPlayer(p);
        } else {
            return new InternalResponseObject<>(true, "created");
        }
    }

    @Override
    public InternalResponseObject<MonsterDefinition> getMonsterDefinitionForId(int id) {
        return actualRepo.getMonsterDefinitionForId(id);
    }


    @Override
    public InternalResponseObject<Monster> buildMonsterForPlayer(UUID gameID, int monsterDBId, Player p) {
        if (p instanceof GameBot) {
            Optional<Monster> monster = ((GameBot) p).getArchitectObjects().stream().filter(obj -> obj instanceof Monster).map(obj -> (Monster) obj).filter(m -> m.getDatabaseID() == monsterDBId).findFirst();
            if (monster.isPresent()) {
                MonsterBuilder mb = new MonsterBuilder(gameID, monster.get());
                return new InternalResponseObject<>(mb.createMonster(), "monster");
            } else {
                return actualRepo.buildMonsterForPlayer(gameID, monsterDBId, p);
            }
        } else {
            return actualRepo.buildMonsterForPlayer(gameID, monsterDBId, p);
        }
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
