package edu.cwru.eecs395_s16.interfaces.objects;

import java.util.List;

/**
 * Created by james on 1/19/16.
 */
public interface Creature extends GameObject {

    int getAttack();

    int getDefense();

    int getHealth();

    int getMovement();

    int getVision();

    List<Ability> getAbilities();

    void triggerPassive(GameMap map, List<GameObject> boardObjects);

}
