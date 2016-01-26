package edu.cwru.eecs395_s16.core.Interfaces.Objects;

import java.util.List;

/**
 * Created by james on 1/19/16.
 */
public interface Creature extends GameObject {

    public int getStrength();

    public int getConstitution();

    public int getWisdom();

    public int getMobility();

    public int getVision();

    public int getDexterity();

    public List<Ability> getAbilities();

}
