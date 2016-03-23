package edu.cwru.eecs395_s16.services.heroitems;

import edu.cwru.eecs395_s16.core.objects.creatures.Equipment;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.services.containers.Repository;

import java.util.Optional;

/**
 * Created by james on 3/17/16.
 */
public interface HeroItemRepository extends Repository {

    Optional<Weapon> getWeaponForId(int id);

    Optional<Equipment> getEquipmentForId(int id);

}
