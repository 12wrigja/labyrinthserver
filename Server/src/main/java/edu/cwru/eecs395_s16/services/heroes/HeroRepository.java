package edu.cwru.eecs395_s16.services.heroes;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.creatures.StatChangeLevelReward;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.LevelReward;
import edu.cwru.eecs395_s16.services.containers.Repository;

import java.util.List;

/**
 * Created by james on 2/18/16.
 */
public interface HeroRepository extends Repository {

    InternalResponseObject<List<Hero>> getPlayerHeroes(Player p);

    InternalResponseObject<Boolean> saveHeroForPlayer(Player p, Hero h);

    InternalResponseObject<Boolean> createDefaultHeroesForPlayer(Player p);

    List<LevelReward> getLevelRewards(HeroType type, long previousExperience, long newExperience);

    InternalResponseObject<HeroDefinition> getHeroDefinitionForId(int id);

    InternalResponseObject<HeroDefinition> getHeroDefinitionForType(HeroType type);

    default LevelReward buildReward(HeroType type, int level, long experience, String rewardStr) {
        //Here we differentiate between reward types
        try {
            return new StatChangeLevelReward(level, experience, type, rewardStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to build a reward implementation for " + rewardStr);
            return new LevelReward(level, experience, type);
        }
    }

    class HeroDefinition {
        public final HeroType type;
        public final int startAttack;
        public final int startDefense;
        public final int startHealth;
        public final int startMovement;
        public final int startVision;
        public final int defaultWeaponId;
        protected final int id;

        protected HeroDefinition(int id, HeroType type, int startAttack, int startDefense, int startHealth, int
                startMovement, int startVision, int defaultWeaponId) {
            this.id = id;
            this.type = type;
            this.startAttack = startAttack;
            this.startDefense = startDefense;
            this.startHealth = startHealth;
            this.startMovement = startMovement;
            this.startVision = startVision;
            this.defaultWeaponId = defaultWeaponId;
        }
    }

}
