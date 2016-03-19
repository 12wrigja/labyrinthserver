package edu.cwru.eecs395_s16.core.objects.maps;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.MapTile;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.repositories.MapRepository;

import java.util.*;

/**
 * Created by james on 2/23/16.
 */
public class AlmostBlankMap implements GameMap {

    private int x;
    private int y;
    private MapTile[][] tiles;
    private List<Location> heroSpawnLocations;
    private List<Location> architectSpawnLocations;
    private List<Location> objectiveSpawnLocations;

    public AlmostBlankMap(int x, int y) {
        this(x,y,GameEngine.instance().services.mapRepository.getTileTypeMap());
    }

    public AlmostBlankMap(int x, int y, Map<String,MapRepository.TileType> tileMap){
        tiles = new MapTile[x][y];
        this.heroSpawnLocations = new ArrayList<>();
        this.architectSpawnLocations = new ArrayList<>();
        this.objectiveSpawnLocations = new ArrayList<>();
        MapRepository.TileType wallType = tileMap.get("wall");
        MapRepository.TileType dirtType = tileMap.get("dirt");
        this.x = x;
        this.y = y;
        Random r = new Random();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                MapTile t;
                MapRepository.TileType tType = (i % 4 == 0 && i != 0 && y >= 2 && j >= 2 && j <= y-2)?wallType:dirtType;
                boolean isHeroSpawnPoint = (i>=0 && i<=2 && j >= 0 && j <= 2) && !tType.isObstruction;
                boolean isArchitectSpawnPoint = (i>=0 && i >= x-2 && i < x && j>=0 && j>=y-2 && j < y) && !tType.isObstruction;
                boolean isObjectiveSpawnPoint = isArchitectSpawnPoint && r.nextInt(100) < 30  && !tType.isObstruction;
                t = new MapTile(i, j, tType, 0, isHeroSpawnPoint, isArchitectSpawnPoint, isObjectiveSpawnPoint);
                tiles[i][j] = t;
                if(isHeroSpawnPoint){
                    heroSpawnLocations.add(t);
                }
                if(isArchitectSpawnPoint){
                    architectSpawnLocations.add(t);
                }
                if(isObjectiveSpawnPoint){
                    objectiveSpawnLocations.add(t);
                }
            }
        }
    }

    @Override
    public Optional<MapTile> getTile(int x, int y) {
        if (x >= 0 && x < this.x && y >= 0 && y < this.y) {
            return Optional.of(tiles[x][y]);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int getSizeX() {
        return x;
    }

    @Override
    public int getSizeY() {
        return y;
    }

    @Override
    public String getCreatorUsername() {
        return "SYSTEM";
    }

    @Override
    public String getName() {
        return "Almost Blank Map";
    }

    @Override
    public int getHeroCapacity() {
        return 4;
    }

    @Override
    public List<Location> getHeroSpawnLocations() {
        return heroSpawnLocations;
    }

    @Override
    public List<Location> getArchitectCreatureSpawnLocations() {
        return architectSpawnLocations;
    }

    @Override
    public List<Location> getObjectiveSpawnLocations() {
        return objectiveSpawnLocations;
    }
}
