package edu.cwru.eecs395_s16.interfaces.objects;

import java.util.List;

/**
 * Created by james on 2/18/16.
 */
public interface GameAction {

    boolean canDoAction(GameMap map, List<GameObject> boardObjects);

    void doGameAction(GameMap map, List<GameObject> boardObjects);

}
