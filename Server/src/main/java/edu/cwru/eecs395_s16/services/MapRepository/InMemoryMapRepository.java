package edu.cwru.eecs395_s16.services.MapRepository;

import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 3/1/16.
 */
public class InMemoryMapRepository implements edu.cwru.eecs395_s16.interfaces.repositories.MapRepository {

    Map<String,TileType> tileTypeMap;

    public InMemoryMapRepository(){
        mapStorage = new HashMap<>();
        tileTypeMap = new HashMap<>();
        tileTypeMap.put("dirt",new TileType(1,"dirt",false));
        tileTypeMap.put("wall",new TileType(2,"wall",true));
        mapStorage.put(0,new AlmostBlankMap(10,10,tileTypeMap));
    }

    Map<Integer,GameMap> mapStorage;

    @Override
    public GameMap getMapByID(int id) {
        return mapStorage.get(id);
    }

    @Override
    public void storeNewMapInDatabase(String mapName, Player creator, GameMap map) {
        //Do nothing. Right now, we don't want to store maps.
        //TODO implement storing of maps in in-memory database.
    }

    @Override
    public Map<String, TileType> getTileTypeMap() {
        return tileTypeMap;
    }
}
