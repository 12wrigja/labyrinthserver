package edu.cwru.eecs395_s16.interfaces.repositories;

import edu.cwru.eecs395_s16.core.objects.Equipment;
import edu.cwru.eecs395_s16.interfaces.objects.Weapon;

import java.util.Optional;

/**
 * Created by james on 3/17/16.
 */
public interface HeroItemRepository extends Repository {

    Optional<Weapon> getWeaponForId(int id);

    Optional<Equipment> getEquipmentForId(int id);

}
