package edu.cwru.eecs395_s16.core.objects.creatures;

import edu.cwru.eecs395_s16.core.objects.Location;

import java.util.List;

/**
 * Created by james on 1/19/16.
 */
public interface Ability {

    String getName();

    String getAbility();

    String getImage();

    int getDamage();

    List<Location> getTargetTiles();
}
