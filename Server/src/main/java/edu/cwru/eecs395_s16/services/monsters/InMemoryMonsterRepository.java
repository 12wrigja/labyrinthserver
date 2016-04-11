package edu.cwru.eecs395_s16.services.monsters;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by james on 2/18/16.
 */
public class InMemoryMonsterRepository implements MonsterRepository {

    Map<String,Map<Integer,MonsterDefinition>> playerMonsterDefMap = new ConcurrentHashMap<>();
    Map<Integer, MonsterDefinition> monsterDefinitionMap = new ConcurrentHashMap<>();

    @Override
    public InternalResponseObject<List<MonsterDefinition>> getPlayerMonsterTypes(Player p) {
        if(playerMonsterDefMap.containsKey(p.getUsername())){
            Map<Integer,MonsterDefinition> playerMonsters = playerMonsterDefMap.get(p.getUsername());
            return new InternalResponseObject<>(new ArrayList<>(playerMonsters.values()),"monsters");
        } else {
            return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_USERNAME);
        }
    }

    @Override
    public InternalResponseObject<Boolean> addMonsterForPlayer(Player p, MonsterDefinition monsterDefinition, int quantity) {
        Map<Integer,MonsterDefinition> playerMonsters;
        if(!playerMonsterDefMap.containsKey(p.getUsername())){
            playerMonsters = new HashMap<>();
            playerMonsterDefMap.put(p.getUsername(),playerMonsters);
        } else {
            playerMonsters = playerMonsterDefMap.get(p.getUsername());
        }
        if(playerMonsters.containsKey(monsterDefinition.id)){
            MonsterDefinition existingDef = playerMonsters.get(monsterDefinition.id);
            playerMonsters.put(monsterDefinition.id,new MonsterDefinition(existingDef,quantity, true));
        } else {
            playerMonsters.put(monsterDefinition.id,new MonsterDefinition(monsterDefinition,quantity, false));
        }
        return new InternalResponseObject<>(true,"added");
    }

    @Override
    public InternalResponseObject<Boolean> createDefaultMonstersForPlayer(Player p) {
        //Give the player 10 goblins to start with.
        MonsterDefinition goblin = monsterDefinitionMap.get(1);
        return addMonsterForPlayer(p,goblin,10);
    }

    @Override
    public InternalResponseObject<MonsterDefinition> getMonsterDefinitionForId(int id) {
        if(monsterDefinitionMap.containsKey(id)){
            return new InternalResponseObject<>(monsterDefinitionMap.get(id),"monster");
        } else {
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    public void initialize(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        List<List<String>> monsterTemplateData = CoreDataUtils.splitEntries(baseData.get("monsters"));
        for(List<String> monsterTemplate : monsterTemplateData){
            try{
                int id = monsterTemplate.get(0).equals("default")?monsterDefinitionMap.size()+1:Integer.parseInt(monsterTemplate.get(0));
                String name = monsterTemplate.get(1);
                String classStr = monsterTemplate.get(2);
                int startAttack = Integer.parseInt(monsterTemplate.get(3));
                int startDefense = Integer.parseInt(monsterTemplate.get(4));
                int startHealth = Integer.parseInt(monsterTemplate.get(5));
                int startVision = Integer.parseInt(monsterTemplate.get(6));
                int startMovement = Integer.parseInt(monsterTemplate.get(7));
                int defaultWeaponID = Integer.parseInt(monsterTemplate.get(8));
                MonsterDefinition definition = new MonsterDefinition(id, name, startAttack, startDefense, startHealth, startMovement, startVision, defaultWeaponID, 0);
                monsterDefinitionMap.put(id, definition);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to create definition for line: "+Arrays.toString(monsterTemplate.toArray(new String[monsterTemplate.size()])));
            }
        }
    }

    @Override
    public void resetToDefaultData(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        playerMonsterDefMap.clear();
        monsterDefinitionMap.clear();
        initialize(baseData);
    }

}
