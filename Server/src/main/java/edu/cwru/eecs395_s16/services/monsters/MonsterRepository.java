package edu.cwru.eecs395_s16.services.monsters;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterDefinition;
import edu.cwru.eecs395_s16.services.containers.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/18/16.
 */
public interface MonsterRepository extends Repository {

    InternalResponseObject<List<MonsterDefinition>> getPlayerMonsterTypes(Player p);

    InternalResponseObject<Boolean> addMonsterForPlayer(Player p, MonsterDefinition monsterDefinition, int quantity);

    InternalResponseObject<Boolean> createDefaultMonstersForPlayer(Player p);

    InternalResponseObject<MonsterDefinition> getMonsterDefinitionForId(int id);

    default InternalResponseObject<Monster> buildMonsterForPlayer(UUID gameID, int monsterDBId, Player player) {
        InternalResponseObject<MonsterDefinition> def = getMonsterDefinitionForId(monsterDBId);
        if (!def.isNormal()) {
            return InternalResponseObject.cloneError(def);
        }
        MonsterBuilder mb = new MonsterBuilder(gameID, def.get(), player.getUsername(), Optional.ofNullable(player.getUsername()));
        return new InternalResponseObject<>(mb.createMonster(), "monster");
    }

}
