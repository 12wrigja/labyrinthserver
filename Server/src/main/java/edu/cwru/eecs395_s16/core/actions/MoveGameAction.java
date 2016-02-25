package edu.cwru.eecs395_s16.core.actions;

import edu.cwru.eecs395_s16.core.InvalidGameStateException;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.MapTile;
import edu.cwru.eecs395_s16.interfaces.objects.Creature;
import edu.cwru.eecs395_s16.interfaces.objects.GameAction;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.networking.requests.gameactions.MoveGameActionData;
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
    public void checkCanDoAction(GameMap map, GameObjectCollection boardObjects, Player player) throws InvalidGameStateException {
        //Check and see if the character has enough movement to move that far.
        Optional<GameObject> boardObj = boardObjects.getByID(UUID.fromString(data.getCharacterID()));
        if (boardObj.isPresent()) {
            if (!(boardObj.get() instanceof Creature)) {
                throw new InvalidGameStateException("Referenced Game Object ID is not a movable object.");
            } else {
                Creature creature = (Creature) boardObj.get();
                if(creature.getControllerID().isPresent() && !creature.getControllerID().get().equals(player.getUsername())){
                    throw new InvalidGameStateException("You do not have permission to control that character.");
                }
                //Check action points. Should be 0, 1, or 2
                if(creature.getActionPoints() <= 0){
                    throw new InvalidGameStateException("That creature does not have any action points remaining.");
                }
                if (data.getPath().size() > creature.getMovement() || data.getPath().size() <= 0) {
                    throw new InvalidGameStateException("The character cannot move this far.");
                } else {
                    //Check path for obstacles
                    List<Location> path = data.getPath();

                    //Check to see if the start tile exists
                    Optional<MapTile> previousTileOpt = map.getTile(creature.getLocation());
                    if (previousTileOpt.isPresent()) {
                        MapTile previousTile = previousTileOpt.get();
                        this.actualPath = new ArrayList<>();
                        //Loop through the tiles
                        int count = 0;
                        for (Location aPath : path) {
                            count ++;
                            //Check to make sure the next tile exists on the path
                            Optional<MapTile> tileOpt = map.getTile(aPath);
                            if (tileOpt.isPresent()) {
                                MapTile nextTile = tileOpt.get();
                                List<GameObject> objsAtTile = boardObjects.getForLocation(nextTile);
                                if (objsAtTile.size() > 0) {
                                    if(objsAtTile.size() == 1 && objsAtTile.get(0).getGameObjectType() == GameObject.TYPE.TRAP){
                                        //This is where the path ends.
                                        //TODO actually trigger the trap and apply effects. This might go in doAction instead
                                        this.actualPath.add(previousTile);
                                        return;
                                    } else {
                                        String response = "Tile at index " + count + " is an obstructed tile. Obstructed by: ";
                                        for (GameObject obstruction : objsAtTile) {
                                            response += obstruction.getGameObjectID().toString() + " ";
                                        }
                                        throw new InvalidGameStateException(response);
                                    }
                                }
                                if(nextTile.isObstructionTileType()){
                                    throw new InvalidGameStateException("The character specified cannot move across this tile type.");
                                }
                                if (!previousTile.isNeighbourOf(nextTile, false)) {
                                    throw new InvalidGameStateException("Path jump detected! Tile at index "+count+" is not a neighbour of a previous tile.");
                                }
                                this.actualPath.add(previousTile);
                                previousTile = nextTile;
                            } else {
                                throw new InvalidGameStateException("Tile at index "+ count+" in the path is invalid.");
                            }
                        }
                    } else {
                        throw new InvalidGameStateException("Initial tile in path is invalid.");
                    }
                }
            }
        } else {
            throw new InvalidGameStateException("Unable to retrieve object with given ID.");
        }
    }

    @Override
    public void doGameAction(GameMap map, GameObjectCollection boardObjects) {
        Location last = this.actualPath.get(this.actualPath.size() - 1);
        Optional<GameObject> boardObj = boardObjects.getByID(UUID.fromString(data.getCharacterID()));
        Creature c = (Creature)boardObj.get();
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
            repr.put("path", this.actualPath);
        } catch (JSONException e) {
            //This should never be hit because the keys are not null
        }
        return repr;
    }
}