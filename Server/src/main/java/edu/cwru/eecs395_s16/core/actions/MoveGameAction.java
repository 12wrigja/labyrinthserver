package edu.cwru.eecs395_s16.core.actions;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.maps.MapTile;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.networking.requests.gameactions.MoveGameActionData;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/21/16.
 */
public class MoveGameAction implements GameAction {

    private MoveGameActionData data;

    public MoveGameAction(MoveGameActionData data) {
        this.data = data;
    }

    public List<Location> actualPath = new ArrayList<>();

    @Override
    public InternalResponseObject<Boolean> checkCanDoAction(Match match, GameMap map, GameObjectCollection boardObjects, Player player) {
        //Check and see if the character has enough movement to move that far.
        InternalResponseObject<Boolean> validActiveActionObject = GameAction.canDoActiveAction(data.getCharacterID(), boardObjects, player, "character_id");
        if (!validActiveActionObject.isNormal()) {
            return validActiveActionObject;
        }
        Creature creature = (Creature) boardObjects.getByID(data.getCharacterID()).get();
        if (data.getPath().size() > creature.getMovement() || data.getPath().size() <= 0) {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.PATH_TOO_LONG);
        }
        //Check path for obstacles
        List<Location> path = data.getPath();

        //Check to see if the start tile exists
        Optional<MapTile> previousTileOpt = map.getTile(creature.getLocation());
        if (!previousTileOpt.isPresent()) {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.PATH_OBSTRUCTED, "Initial tile in path is invalid.");
        }
        MapTile previousTile = previousTileOpt.get();
        this.actualPath = new ArrayList<>();
        //Loop through the tiles
        int count = 0;
        for (Location aPath : path) {
            count++;
            //Check to make sure the next tile exists on the path
            Optional<MapTile> tileOpt = map.getTile(aPath);
            if (!tileOpt.isPresent()) {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.PATH_OBSTRUCTED, "Tile at index " + count + " in the path is invalid.");
            }
            MapTile nextTile = tileOpt.get();
            List<GameObject> objsAtTile = boardObjects.getForLocation(nextTile);
            if (objsAtTile.size() > 0) {
                if (objsAtTile.size() == 1 && objsAtTile.get(0).getGameObjectType() == GameObject.TYPE.TRAP) {
                    //This is where the path ends.
                    //TODO actually trigger the trap and apply effects. This might go in doAction instead
                    this.actualPath.add(previousTile);
                    return new InternalResponseObject<>(true, "valid");
                } else {
                    String response = "Tile at index " + count + " is an obstructed tile. Obstructed by: ";
                    for (GameObject obstruction : objsAtTile) {
                        response += obstruction.getGameObjectID().toString() + " ";
                    }
                    return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.PATH_OBSTRUCTED, response);
                }
            }
            if (nextTile.isObstructionTileType()) {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.PATH_OBSTRUCTED, "The character specified cannot move across this tile type.");
            }
            if (!previousTile.isNeighbourOf(nextTile, false)) {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.PATH_SKIP, "Path jump detected! Tile at index " + count + " is not a neighbour of a previous tile.");
            }
            this.actualPath.add(nextTile);
            previousTile = nextTile;
        }

        return new InternalResponseObject<>(true, "valid");
    }

    @Override
    public void doGameAction(GameMap map, GameObjectCollection boardObjects) {
        Location last = this.actualPath.get(this.actualPath.size() - 1);
        Creature c = (Creature)boardObjects.getByID(data.getCharacterID()).get();
        //TODO check for damage, traps, etc.
        c.setLocation(last);
        c.useActionPoint();
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("type", "move");
            repr.put("character", data.getCharacterID());
            repr.put("path", JSONUtils.listify(this.actualPath));
        } catch (JSONException e) {
            //This should never be hit because the keys are not null
        }
        return repr;
    }
}