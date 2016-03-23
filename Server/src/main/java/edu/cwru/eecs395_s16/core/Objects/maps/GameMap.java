package edu.cwru.eecs395_s16.core.objects.maps;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.networking.Jsonable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by james on 2/12/16.
 */
public interface GameMap extends Jsonable {

    Optional<MapTile> getTile(int x, int y);

    default Optional<MapTile> getTile(Location loc){
        return getTile(loc.getX(),loc.getY());
    };

    int getSizeX();

    int getSizeY();

    default List<Location> getTileNeighbours(Location tile){
        List<Location> neighbours = new ArrayList<>();
        int x = tile.getX();
        int y = tile.getY();

        int mapX = getSizeX();
        int mapY = getSizeY();

        if( x-1 >= 0){
            neighbours.add(new Location(x-1,y));
            if(y-1 >= 0){
                neighbours.add(new Location(x-1,y-1));
            }
            if(y+1 < mapY){
                neighbours.add(new Location(x-1,y+1));
            }
        }
        if(x+1 < mapX){
            neighbours.add(new Location(x+1,y));
            if(y-1 >= 0){
                neighbours.add(new Location(x+1,y-1));
            }
            if(y+1 < mapY){
                neighbours.add(new Location(x+1,y+1));
            }
        }
        if(y-1 >= 0){
            neighbours.add(new Location(x,y-1));
        }
        if(y+1 < mapY){
            neighbours.add(new Location(x,y+1));
        }
        return neighbours;
    }

    @Override
    default JSONObject getJSONRepresentation(){
        JSONObject mapObj = new JSONObject();
        JSONObject sizeObj = new JSONObject();
        try {
            sizeObj.put("x", getSizeX());
            sizeObj.put("y", getSizeY());
            mapObj.put("size", sizeObj);
            JSONArray tileArray = new JSONArray();
            for (int i = 0; i < getSizeX(); i++) {
                for (int j = 0; j < getSizeY(); j++) {
                    //JSONRepresentation Change
                    tileArray.put(getTile(i,j).get().getJSONRepresentation());
                }
            }
            mapObj.put("tiles", tileArray);
        }catch(JSONException e){
            //Never will occur - all keys are not null
        }
        return mapObj;
    }

    String getCreatorUsername();

    String getName();

    int getHeroCapacity();

    List<Location> getHeroSpawnLocations();

    List<Location> getArchitectCreatureSpawnLocations();

    List<Location> getObjectiveSpawnLocations();

}
