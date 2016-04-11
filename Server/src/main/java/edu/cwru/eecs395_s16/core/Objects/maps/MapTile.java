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
            json.put("is_hero_spawn_tile",isHeroSpawn);
            json.put("is_architect_spawn_tile",isArchitectSpawn);
            json.put("is_objective_spawn_tile",isObjectiveSpawn);
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
