package edu.cwru.eecs395_s16.core.objects.maps;

import edu.cwru.eecs395_s16.core.objects.MapTile;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;

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
        this.x = x;
        this.y = y;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                MapTile t;
                if (i % 4 == 0 && i != 0 && y >= 2 && j >= 2 && j <= y-2) {
                    t = new MapTile(i, j, "wall", 0);
                } else {
                    t = new MapTile(i, j, "dirt", 0);
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
}
