package edu.cwru.eecs395_s16.core.objects.maps;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.networking.Jsonable;
import edu.cwru.eecs395_s16.services.maps.MapRepository;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2/12/16.
 */
public class MapTile extends Location implements Jsonable {

    public static final String TERRAIN_KEY = "terrain";
    public static final String ROTATION_KEY = "rotation";
    public static final String OBSTACLE_KEY = "is_obstacle";
    public static final String HERO_SPAWN_KEY = "is_hero_spawn_tile";
    public static final String ARCHITECT_SPAWN_KEY = "is_architect_spawn_tile";
    public static final String OBJECTIVE_SPAWN_KEY = "is_objective_spawn_tile";
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
            json.put(X_KEY, getX());
            json.put(Y_KEY,getY());
            json.put(TERRAIN_KEY, tileType.type);
            json.put(ROTATION_KEY, rotation);
            json.put(OBSTACLE_KEY,tileType.isObstruction);
            json.put(HERO_SPAWN_KEY,isHeroSpawn);
            json.put(ARCHITECT_SPAWN_KEY,isArchitectSpawn);
            json.put(OBJECTIVE_SPAWN_KEY,isObjectiveSpawn);
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

    public MapRepository.TileType getTileType() {
        return tileType;
    }

    public int getRotation() {
        return rotation;
    }
}
