package edu.cwru.eecs395_s16.services.MapRepository;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 3/1/16.
 */
public class InMemoryMapRepository implements edu.cwru.eecs395_s16.interfaces.repositories.MapRepository {

    Map<String,TileType> tileTypeMap;
    Map<Integer,GameMap> mapStorage;
    Map<String, GameMap> playersMapFinder;

    public InMemoryMapRepository(){
        mapStorage = new HashMap<>();
        tileTypeMap = new HashMap<>();
        playersMapFinder = new HashMap<>();
        tileTypeMap.put("dirt",new TileType(1,"dirt",false));
        tileTypeMap.put("wall",new TileType(2,"wall",true));
        mapStorage.put(0,new AlmostBlankMap(10,10,tileTypeMap));
    }

    @Override
    public InternalResponseObject<GameMap> getMapByID(int id) {
        if(mapStorage.containsKey(id)){
            return new InternalResponseObject<>(mapStorage.get(id),"map");
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.UNKNOWN_MAP_IDENTIFIER);
        }
    }

    @Override
    public void storeNewMapInDatabase(String mapName, Player creator, GameMap map) {
        playersMapFinder.put(creator.getUsername(), map);
        int nameCode = mapName.hashCode();
        mapStorage.put(nameCode,map);
    }

    @Override
    public Map<String, TileType> getTileTypeMap() {
        return tileTypeMap;
    }
}
