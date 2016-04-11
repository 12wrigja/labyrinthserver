package edu.cwru.eecs395_s16.services.monsters;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.DatabaseObject;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.networking.Jsonable;
import edu.cwru.eecs395_s16.services.containers.Repository;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;

/**
 * Created by james on 2/18/16.
 */
public interface MonsterRepository extends Repository {

    InternalResponseObject<List<MonsterDefinition>> getPlayerMonsterTypes(Player p);

    InternalResponseObject<Boolean> addMonsterForPlayer(Player p, MonsterDefinition monsterDefinition, int quantity);

    InternalResponseObject<Boolean> createDefaultMonstersForPlayer(Player p);

    InternalResponseObject<MonsterDefinition> getMonsterDefinitionForId(int id);

    class MonsterDefinition implements Jsonable {
        public static final String MONSTER_COUNT_KEY = "quantity";

        public final int id;
        public final String name;
        public final int startAttack;
        public final int startDefense;
        public final int startHealth;
        public final int startMovement;
        public final int startVision;
        public final int defaultWeaponId;
        public final int count;

        protected MonsterDefinition(int id, String name, int startAttack, int startDefense, int startHealth, int startMovement, int startVision, int defaultWeaponId, int count) {
            this.id = id;
            this.name = name;
            this.startAttack = startAttack;
            this.startDefense = startDefense;
            this.startHealth = startHealth;
            this.startMovement = startMovement;
            this.startVision = startVision;
            this.defaultWeaponId = defaultWeaponId;
            this.count = count;
        }

        public MonsterDefinition(MonsterDefinition other, int countChange, boolean increment) {
            this.id = other.id;
            this.name = other.name;
            this.startAttack = other.startAttack;
            this.startDefense = other.startDefense;
            this.startHealth = other.startHealth;
            this.startMovement = other.startMovement;
            this.startVision = other.startVision;
            this.defaultWeaponId = other.defaultWeaponId;
            if(increment) {
                this.count = other.count + countChange;
            } else {
                this.count = countChange;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MonsterDefinition that = (MonsterDefinition) o;

            return id == that.id;

        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public JSONObject getJSONRepresentation() {
            JSONObject repr = new JSONObject();
            try {
                repr.put("id",id);
                repr.put(Creature.ATTACK_KEY,startAttack);
                repr.put(Creature.DEFENSE_KEY,startDefense);
                repr.put(Creature.HEALTH_KEY, startHealth);
                repr.put(Creature.MOVEMENT_KEY, startMovement);
                repr.put(Creature.VISION_KEY, startVision);
                Optional<Weapon> w = GameEngine.instance().services.heroItemRepository.getWeaponForId(defaultWeaponId);
                if(w.isPresent()){
                    repr.put(Creature.WEAPON_KEY,w.get().getJSONRepresentation());
                }
                repr.put(Monster.NAME_KEY,name);
                repr.put(MONSTER_COUNT_KEY,count);
            } catch (JSONException e){
                //Should not occur - non-null keys;
            }
            return repr;
        }
    }

}
