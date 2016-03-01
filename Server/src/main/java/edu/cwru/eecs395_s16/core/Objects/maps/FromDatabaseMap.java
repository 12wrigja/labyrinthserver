package edu.cwru.eecs395_s16.core.objects.maps;

import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.MapTile;
import edu.cwru.eecs395_s16.interfaces.objects.DatabaseObject;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;

import java.util.Optional;

/**
 * Created by james on 3/1/16.
 */
public class FromDatabaseMap implements DatabaseObject, GameMap {

    private final int databaseID;
    private final String name;
    private final String creator;
    private final int width;
    private final int height;
    private final int hero_capacity;

    private final MapTile[][] tiles;

    public FromDatabaseMap(int databaseID, String name, String creator, int width, int height, int hero_capacity) {
        this.databaseID = databaseID;
        this.name = name;
        this.creator = creator;
        this.width = width;
        this.height = height;
        this.hero_capacity = hero_capacity;
        this.tiles = new MapTile[width][height];
    }

    public void setTile(int x, int y, MapTile tile){
        if(!(x < 0 || x >= width || y < 0 || y >= height)) {
            this.tiles[x][y] = tile;
        }
    }

    @Override
    public int getDatabaseID() {
        return this.databaseID;
    }

    @Override
    public Optional<MapTile> getTile(int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= height){
            return Optional.empty();
        } else {
            return Optional.of(tiles[x][y]);
        }
    }

    @Override
    public int getSizeX() {
        return width;
    }

    @Override
    public int getSizeY() {
        return height;
    }

    @Override
    public String getCreatorUsername() {
        return creator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getHeroCapacity() {
        return hero_capacity;
    }
}
