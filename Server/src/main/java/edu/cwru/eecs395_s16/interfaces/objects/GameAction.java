package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.InvalidGameStateException;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.interfaces.Jsonable;

/**
 * Created by james on 2/18/16.
 */
public interface GameAction extends Jsonable {

    InternalResponseObject<Boolean> checkCanDoAction(GameMap map, GameObjectCollection boardObjects, Player player);

    void doGameAction(GameMap map, GameObjectCollection boardObjects);

    static boolean isControlledByPlayer(GameObject object, Player p){
        return object.getControllerID().isPresent() && object.getControllerID().get().equals(p.getUsername());
    }

    static boolean isControlledByOpponent(GameObject object, Player p){
        return object.getControllerID().isPresent() && !object.getControllerID().get().equals(p.getUsername());
    }

}
