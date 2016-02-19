package edu.cwru.eecs395_s16.interfaces.objects;

/**
 * Created by james on 1/19/16.
 */
public interface Character extends Creature {

    Weapon getWeapon();

    void setWeapon();

    int getLevel();

    void grantXP(int xp);
}
