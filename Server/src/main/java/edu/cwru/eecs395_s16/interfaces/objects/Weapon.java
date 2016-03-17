package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.interfaces.Jsonable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by james on 1/19/16.
 */
public class Weapon implements DatabaseObject, Jsonable {

    private final int databaseID;
    public static final String IMAGE_KEY = "image";
    private final String image;
    public static final String NAME_KEY = "name";
    private final String name;
    public static final String DESCRIPTION_KEY = "description";
    private final String description;
    public static final String DAMAGE_MOD_KEY = "damage_mod";
    private final int damageModifier;
    public static final String RANGE_KEY = "range";
    private final int range;
    public static final String DAMAGE_MAP_KEY = "damage_map";
    private final Map<Location,Float> damageDistributionMap;

    public Weapon(int databaseID, String image, String name, String description, int damageModifier, int range, Map<Location, Float> damageDistributionMap) {
        this.databaseID = databaseID;
        this.image = image;
        this.name = name;
        this.description = description;
        this.damageModifier = damageModifier;
        this.range = range;
        this.damageDistributionMap = damageDistributionMap;
    }

    @Override
    public int getDatabaseID() {
        return databaseID;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getDamageModifier() {
        return damageModifier;
    }

    public int getRange() {
        return range;
    }

    public boolean isAttackLocationValid(Hero hero, Creature target) {
        return damageDistributionMap.containsKey(target.getLocation());
    }

    public float getDamagePercentageForLocation(Location location) {
        if(damageDistributionMap.containsKey(location)){
            return damageDistributionMap.get(location);
        } else {
            return 0f;
        }
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject representation = new JSONObject();
        try{
            representation.put(IMAGE_KEY,getImage());
            representation.put(NAME_KEY,getName());
            representation.put(DESCRIPTION_KEY,getDescription());
            representation.put(DAMAGE_MOD_KEY,getDamageModifier());
            representation.put(RANGE_KEY,getRange());

        } catch (JSONException e){
            //This should never happen - all keys are not null
        }
    }
}
