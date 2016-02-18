package edu.cwru.eecs395_s16.interfaces.objects;

import java.util.List;

/**
 * Created by james on 1/19/16.
 */
public interface Ability {

    public String getName();

    public String getAbility();

    public String getImage();

    public int getDamage();

    public List<Location> getTargetTiles();
}
