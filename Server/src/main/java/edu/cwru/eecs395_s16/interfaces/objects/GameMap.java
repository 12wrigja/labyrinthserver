package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.interfaces.Jsonable;
import edu.cwru.eecs395_s16.core.objects.BasicLocation;
import edu.cwru.eecs395_s16.core.objects.MapTile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 2/12/16.
 */
public interface GameMap extends Jsonable {

    MapTile getTile(int x, int y);

    int getSizeX();

    int getSizeY();

    default List<Location> getTileNeighbours(Location tile){
        List<Location> neighbours = new ArrayList<>();
        int x = tile.getX();
        int y = tile.getY();

        int mapX = getSizeX();
        int mapY = getSizeY();

        if( x-1 >= 0){
            neighbours.add(new BasicLocation(x-1,y));
            if(y-1 >= 0){
                neighbours.add(new BasicLocation(x-1,y-1));
            }
            if(y+1 < mapY){
                neighbours.add(new BasicLocation(x-1,y+1));
            }
        }
        if(x+1 < mapX){
            neighbours.add(new BasicLocation(x+1,y));
            if(y-1 >= 0){
                neighbours.add(new BasicLocation(x+1,y-1));
            }
            if(y+1 < mapY){
                neighbours.add(new BasicLocation(x+1,y+1));
            }
        }
        if(y-1 >= 0){
            neighbours.add(new BasicLocation(x,y-1));
        }
        if(y+1 < mapY){
            neighbours.add(new BasicLocation(x,y+1));
        }
        return neighbours;
    }

    @Override
    default Map<String, Object> getJsonableRepresentation(){
        Map<String,Object> mapObj = new HashMap<>();
        Map<String,Integer> sizeMap = new HashMap<>();
        sizeMap.put("x",getSizeX());
        sizeMap.put("y",getSizeY());
        mapObj.put("size",sizeMap);
        Object[] allTiles = new Object[getSizeY()*getSizeX()];
        for(int i=0; i<getSizeX(); i++){
            for(int j=0; j<getSizeY(); j++){
                allTiles[i*getSizeY()+j] = getTile(i,j);
            }
        }
        mapObj.put("tiles",allTiles);
        return mapObj;
    }
}
