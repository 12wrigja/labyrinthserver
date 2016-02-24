package edu.cwru.eecs395_s16.interfaces.objects;

/**
 * Created by james on 1/19/16.
 */
public interface Character extends Creature {

    String WEAPON_ID_KEY = "weapon_id";

    Weapon getWeapon();

    void setWeapon();

    String LEVEL_KEY = "level";

    int getLevel();

    void grantXP(int xp);
}
