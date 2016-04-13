package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterDefinition;

/**
 * Created by james on 4/13/16.
 */
class WeaponModMonsterDefinition extends MonsterDefinition {
    public WeaponModMonsterDefinition(int specialID, MonsterDefinition other, int count, int weaponID) {
        super(specialID,other.name, other.startAttack, other.startDefense, other.startHealth, other.startMovement, other.startVision, weaponID, count);

    }
}