package edu.cwru.eecs395_s16.core.objects;

import edu.cwru.eecs395_s16.interfaces.objects.GameMap;

import java.util.*;

/**
 * Created by james on 2/12/16.
 */
public class RandomlyGeneratedGameMap implements GameMap {

    private int x;
    private int y;

    private MapTile[][] tiles;

    public RandomlyGeneratedGameMap(int x, int y){
        tiles = new MapTile[x][y];
        this.x=x;
        this.y=y;

        //Assign a random type to each tile from a list
        String[] tileTypes = new String[]{"wall","dirt","sand","water","rock","empty","default"};
        Random rand = new Random();
        for(int i=0; i<x; i++){
            for(int j=0; j<y; j++){
                int index = rand.nextInt(tileTypes.length);
                tiles[i][j] = new MapTile(i,j,tileTypes[index],0);
            }
        }
    }

    @Override
    public MapTile getTile(int x, int y) {
        if(x >= 0 && x < this.x && y >= 0 && y < this.y){
            return tiles[x][y];
        } else {
            return null;
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
