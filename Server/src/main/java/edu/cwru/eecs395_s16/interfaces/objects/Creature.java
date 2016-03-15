package edu.cwru.eecs395_s16.interfaces.objects;

import java.util.List;

/**
 * Created by james on 1/19/16.
 */
public interface Creature extends GameObject {

    String ATTACK_KEY = "attack";
    int getAttack();

    String DEFENSE_KEY = "defense";
    int getDefense();

    String HEALTH_KEY = "health";
    int getHealth();

    String MOVEMENT_KEY = "movement";
    int getMovement();

    String VISION_KEY = "vision";
    int getVision();

    String ABILITIES_KEY = "abilities";
    List<Ability> getAbilities();

    String STATUSES_KEY = "statuses";
    List<String> getStatuses();

    String ACTION_POINTS_KEY = "action_points";
    int getActionPoints();
    void drainActionPoints();
    void useActionPoint();
    void resetActionPoints();

    void triggerPassive(GameMap map, List<GameObject> boardObjects);

}
