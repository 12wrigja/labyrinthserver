package edu.cwru.eecs395_s16.core.actions;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.interfaces.objects.Creature;
import edu.cwru.eecs395_s16.interfaces.objects.GameAction;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.networking.requests.gameactions.PassGameActionData;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 3/15/16.
 */
public class PassGameAction implements GameAction {

    UUID characterID;
    public PassGameAction(PassGameActionData data){
        this.characterID = data.getCharacterID();
    }


    @Override
    public InternalResponseObject<Boolean> checkCanDoAction(GameMap map, GameObjectCollection boardObjects, Player player) {
        Optional<GameObject> boardObj = boardObjects.getByID(characterID);
        if (boardObj.isPresent()) {
            if (!(boardObj.get() instanceof Creature)) {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.INVALID_OBJECT);
            } else {

            }
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.UNKNOWN_OBJECT);
        }
        return new InternalResponseObject<>(true, "valid");
    }

    @Override
    public void doGameAction(GameMap map, GameObjectCollection boardObjects) {
        Creature c = (Creature)boardObjects.get(characterID);
        c.drainActionPoints();
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null;
    }
}
