package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.core.InvalidGameStateException;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.interfaces.Jsonable;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by james on 2/18/16.
 */
public interface GameAction extends Jsonable {

    void checkCanDoAction(GameMap map, GameObjectCollection boardObjects) throws InvalidGameStateException;

    void doGameAction(GameMap map, GameObjectCollection boardObjects);

}
