package edu.cwru.eecs395_s16.core.actions;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.networking.requests.gameactions.PassGameActionData;
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
    public InternalResponseObject<Boolean> checkCanDoAction(Match match, GameMap map, GameObjectCollection boardObjects, Player player) {
        Optional<GameObject> boardObj = boardObjects.getByID(characterID);
        if (boardObj.isPresent()) {
            if (!(boardObj.get() instanceof Creature)) {
                return new InternalResponseObject<>(InternalErrorCode.INVALID_OBJECT);
            } else if(!GameAction.isControlledByPlayer(boardObj.get(),player)){
                    return new InternalResponseObject<>(InternalErrorCode.NOT_CONTROLLER);
            } else if (((Creature)boardObj.get()).getActionPoints() == 0){
                return new InternalResponseObject<>(InternalErrorCode.NO_ACTION_POINTS);
            }
        } else {
            return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_OBJECT);
        }
        return new InternalResponseObject<>(true, "valid");
    }

    @Override
    public void doGameAction(GameMap map, GameObjectCollection boardObjects) {
        Creature c = (Creature)boardObjects.getByID(characterID).get();
        c.drainActionPoints();
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null;
    }
}
