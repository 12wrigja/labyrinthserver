package edu.cwru.eecs395_s16.core.objects;

import edu.cwru.eecs395_s16.interfaces.Jsonable;
import edu.cwru.eecs395_s16.interfaces.repositories.MapRepository;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2/12/16.
 */
public class MapTile extends Location implements Jsonable {

    private MapRepository.TileType tileType;
    private final int rotation;
    private final boolean isHeroSpawn;
    private final boolean isArchitectSpawn;
    private final boolean isObjectiveSpawn;

    public MapTile(int x, int y, MapRepository.TileType tileType, int rotation, boolean isHeroSpawn, boolean isArchitectSpawn, boolean isObjectiveSpawn) {
        super(x, y);
        this.tileType = tileType;
        this.rotation = rotation;
        this.isHeroSpawn = isHeroSpawn;
        this.isArchitectSpawn = isArchitectSpawn;
        this.isObjectiveSpawn = isObjectiveSpawn;
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject json = new JSONObject();
        try {
            json.put("x", getX());
            json.put("y",getY());
            json.put("terrain", tileType.type);
            json.put("rotation", rotation);
            json.put("is_obstacle",tileType.isObstruction);
        } catch (JSONException e) {
            //Never will happen - all keys are not null
        }
        return json;
    }

    public boolean isObstructionTileType() {
        return this.tileType.isObstruction;
    }

    public boolean isHeroSpawn() {
        return isHeroSpawn;
    }

    public boolean isArchitectSpawn() {
        return isArchitectSpawn;
    }

    public boolean isObjectiveSpawn() {
        return isObjectiveSpawn;
    }
}
