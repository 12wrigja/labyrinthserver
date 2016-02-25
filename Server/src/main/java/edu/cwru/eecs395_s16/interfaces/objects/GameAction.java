package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.core.InvalidGameStateException;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.interfaces.Jsonable;

/**
 * Created by james on 2/18/16.
 */
public interface GameAction extends Jsonable {

    void checkCanDoAction(GameMap map, GameObjectCollection boardObjects, Player player) throws InvalidGameStateException;

    void doGameAction(GameMap map, GameObjectCollection boardObjects);

}
