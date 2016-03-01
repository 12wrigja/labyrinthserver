package edu.cwru.eecs395_s16.core.objects.maps;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.MapTile;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.repositories.MapRepository;

import java.util.Optional;

/**
 * Created by james on 2/23/16.
 */
public class AlmostBlankMap implements GameMap {

    private int x;
    private int y;
    private MapTile[][] tiles;

    public AlmostBlankMap(int x, int y) {
        tiles = new MapTile[x][y];
        MapRepository.TileType wallType = GameEngine.instance().getMapRepository().getTileTypeMap().get("wall");
        MapRepository.TileType dirtType = GameEngine.instance().getMapRepository().getTileTypeMap().get("wall");
        this.x = x;
        this.y = y;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                MapTile t;
                boolean isHeroSpawnPoint = (i>=0 && i<=2 && j >= 0 && j <= 2);
                if (i % 4 == 0 && i != 0 && y >= 2 && j >= 2 && j <= y-2) {
                    t = new MapTile(i, j, wallType, 0, isHeroSpawnPoint);
                } else {
                    t = new MapTile(i, j, dirtType, 0, isHeroSpawnPoint);
                }
                tiles[i][j] = t;
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
}
