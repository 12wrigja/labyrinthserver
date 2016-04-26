package edu.cwru.eecs395_s16.services.maps;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.maps.FromDatabaseMap;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.maps.MapTile;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by james on 3/1/16.
 */
public class InMemoryMapRepository implements MapRepository {

    Map<String, TileType> tileTypeMap;
    Map<Integer, GameMap> mapStorage;
    Map<String, GameMap> playersMapFinder;

    public InMemoryMapRepository() {
        mapStorage = new HashMap<>();
        tileTypeMap = new HashMap<>();
        playersMapFinder = new HashMap<>();
    }

    @Override
    public InternalResponseObject<GameMap> getMapByID(int id) {
        if (mapStorage.containsKey(id)) {
            return new InternalResponseObject<>(mapStorage.get(id), "map");
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode
                    .UNKNOWN_MAP_IDENTIFIER);
        }
    }

    @Override
    public InternalResponseObject<Integer> storeNewMapInDatabase(String mapName, Player creator, GameMap map) {
        playersMapFinder.put(creator.getUsername(), map);
        int id = mapStorage.size() + 1;
        mapStorage.put(id, map);
        return new InternalResponseObject<>(id, "map_id");
    }

    @Override
    public Map<String, TileType> getTileTypeMap() {
        return tileTypeMap;
    }

    @Override
    public InternalResponseObject<List<MapMetadata>> getMapData() {
        return new InternalResponseObject<>(mapStorage.values().stream().map(map -> new MapMetadata(map.getDatabaseID
                (), map.getName(), map.getCreatorUsername(), map.getSizeX(), map.getSizeY(), map.getHeroCapacity()))
                .collect(Collectors.toList()), "maps");
    }

    @Override
    public void initialize(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        List<List<String>> tiles = CoreDataUtils.splitEntries(baseData.get("tiles"));
        List<List<String>> maps = CoreDataUtils.splitEntries(baseData.get("maps"));
        List<List<String>> tile_map = CoreDataUtils.splitEntries(baseData.get("tile_map"));
        List<List<String>> players = CoreDataUtils.splitEntries(baseData.get("players"));
        tiles.forEach(lst -> {
            TileType t = new TileType(tileTypeMap.size() + 1, lst.get(1), Boolean.parseBoolean(lst.get(2)), Boolean
                    .parseBoolean(lst.get(3)));
            tileTypeMap.put(t.type, t);
        });
        maps.forEach(lst -> {
            FromDatabaseMap mp = new FromDatabaseMap(mapStorage.size() + 1, lst.get(1), lst.get(2), Integer.parseInt
                    (lst.get(3)), Integer.parseInt(lst.get(4)), Integer.parseInt(lst.get(5)));
            tile_map.stream().filter(lst1 -> Integer.parseInt(lst1.get(0)) == mp.getDatabaseID()).forEach(lst1 -> {
                MapTile t = new MapTile(Integer.parseInt(lst1.get(2)), Integer.parseInt(lst1.get(3)), tileTypeMap
                        .values().stream().filter(tile -> tile.id == Integer.parseInt(lst1.get(1))).findFirst().get()
                        , Integer.parseInt(lst1.get(7)), Boolean.parseBoolean(lst1.get(4)), Boolean.parseBoolean(lst1
                        .get(5)), Boolean.parseBoolean(lst1.get(6)));
                mp.setTile(t.getX(), t.getY(), t);
            });
            String username = players.get(Integer.parseInt(lst.get(2)) - 1).get(2);
            playersMapFinder.put(username, mp);
            mapStorage.put(mp.getDatabaseID(), mp);
        });
    }

    @Override
    public void resetToDefaultData(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        mapStorage = new HashMap<>();
        tileTypeMap = new HashMap<>();
        playersMapFinder = new HashMap<>();
        initialize(baseData);
    }
}
