package edu.cwru.eecs395_s16.services.maprepository;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.MapTile;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.core.objects.maps.FromDatabaseMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;

import java.util.HashMap;
import java.util.List;
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

    public void initialize(List<List<String>> maps, List<List<String>> tiles, List<List<String>> tile_map, List<List<String>> players) {
        tileTypeMap = new HashMap<>();
        tiles.forEach(lst -> {
            TileType t = new TileType(tileTypeMap.size()+1,lst.get(1),Boolean.parseBoolean(lst.get(2)));
            tileTypeMap.put(t.type,t);
        });
        maps.forEach(lst -> {
            FromDatabaseMap mp = new FromDatabaseMap(mapStorage.size()+1,lst.get(1),"",Integer.parseInt(lst.get(3)),Integer.parseInt(lst.get(4)),Integer.parseInt(lst.get(5)));
            tile_map.stream().filter(lst1 -> Integer.parseInt(lst1.get(0)) == mp.getDatabaseID()).forEach(lst1->{
                MapTile t = new MapTile(Integer.parseInt(lst1.get(2)),
                        Integer.parseInt(lst1.get(3)),
                        tileTypeMap.values().stream()
                                .filter(tile->tile.id == Integer.parseInt(lst1.get(1))).findFirst().get(),
                        Integer.parseInt(lst1.get(7)),
                        Boolean.parseBoolean(lst1.get(4)),
                        Boolean.parseBoolean(lst1.get(5)),
                        Boolean.parseBoolean(lst1.get(6)));
                mp.setTile(t.getX(),t.getY(),t);
            });
            String username = players.get(Integer.parseInt(lst.get(2))).get(2);
            playersMapFinder.put(username,mp);
            mapStorage.put(mp.getDatabaseID(),mp);
        });
    }
}
