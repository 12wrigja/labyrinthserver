package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.interfaces.RequestData;

import java.util.List;

/**
 * Created by james on 2/18/16.
 */
public interface GameAction<T extends RequestData> {

    boolean canDoAction(GameMap map, List<GameObject> boardObjects, T data);

    void doGameAction(GameMap map, List<GameObject> boardObjects, T data);

}
