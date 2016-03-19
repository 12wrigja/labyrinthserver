package edu.cwru.eecs395_s16.services.weaponrepository;

import edu.cwru.eecs395_s16.core.objects.AttackPattern;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.objects.Weapon;
import edu.cwru.eecs395_s16.interfaces.repositories.WeaponRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by james on 3/17/16.
 */
public class InMemoryWeaponRepository implements WeaponRepository {

    Map<Integer,Weapon> weaponMap;

    public InMemoryWeaponRepository() {
        //Add in default weapons
        weaponMap = new HashMap<>();
        Weapon basicMeleeWeapon = new Weapon(0,"sword","Basic Sword","A basic sword. Does no damage scaling.", 1, 1, AttackPattern.singleTargetPattern);
        weaponMap.put(basicMeleeWeapon.getDatabaseID(),basicMeleeWeapon);
        Weapon basicRangedWeapon = new Weapon(1,"bow","Basic Bow","A basic bow. Does no damage scaling.", 3, 1, AttackPattern.singleTargetPattern);
        weaponMap.put(basicRangedWeapon.getDatabaseID(),basicRangedWeapon);
    }

    @Override
    public Optional<Weapon> getWeaponForId(int id) {
        if(weaponMap.containsKey(id)){
            return Optional.of(weaponMap.get(id));
        } else {
            return Optional.empty();
        }

    }
}
