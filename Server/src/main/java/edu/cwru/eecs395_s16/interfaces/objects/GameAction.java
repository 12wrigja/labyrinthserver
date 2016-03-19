package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.InvalidGameStateException;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.MapTile;
import edu.cwru.eecs395_s16.interfaces.Jsonable;

import java.util.Optional;

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

    static boolean isLineOfSight(Location loc1, Location loc2, GameMap map, GameObjectCollection boardObjects) {
        int x0 = loc1.getX();
        int x1 = loc2.getX();
        int y0 = loc1.getY();
        int y1 = loc2.getY();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int x = x0;
        int y = y0;
        int n = 1 + dx + dy;
        int x_inc = (x1 > x0) ? 1 : -1;
        int y_inc = (y1 > y0) ? 1 : -1;
        int error = dx - dy;
        dx *= 2;
        dy *= 2;

        while(n>0)
        {
            Optional<MapTile> t = map.getTile(x,y);
            if(t.isPresent()){
                if(t.get().isObstructionTileType()){
                    return false;
                }
            } else {
                return false;
            }
            Location loc = new Location(x,y);
            if(loc != loc1 && loc != loc2 && boardObjects.getForLocation(new Location(x,y)).size() > 0){
                return false;
            }

            if (error > 0)
            {
                x += x_inc;
                error -= dy;
            }
            else
            {
                y += y_inc;
                error += dx;
            }

            n--;
        }

        return true;
    }

}
