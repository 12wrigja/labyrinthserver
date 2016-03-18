package edu.cwru.eecs395_s16.services.weaponrepository;

import edu.cwru.eecs395_s16.interfaces.objects.Weapon;
import edu.cwru.eecs395_s16.interfaces.repositories.WeaponRepository;

import java.sql.Connection;
import java.util.Optional;

/**
 * Created by james on 3/17/16.
 */
public class PostgresWeaponRepository implements WeaponRepository {

    Connection conn;

    public PostgresWeaponRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Optional<Weapon> getWeaponForId(int id) {
        return Optional.empty();
    }
}
