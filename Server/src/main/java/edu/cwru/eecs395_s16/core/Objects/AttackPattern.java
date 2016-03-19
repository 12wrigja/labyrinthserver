package edu.cwru.eecs395_s16.core.objects;

import edu.cwru.eecs395_s16.interfaces.Jsonable;
import edu.cwru.eecs395_s16.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 3/19/16.
 */
public class AttackPattern implements Jsonable {

    public static final String DAMAGE_MAP_KEY = "damage_map";
    public static final String DAMAGE_PERCENT_KEY = "damage_percent";
    public static final String NUM_INPUT_KEY = "count";
    public static final String ROTATABLE_KEY = "rotatable";
    private final int inputCount;

    private final boolean rotatable;
    private final Map<Location,Float> damageDistributionMap;

    public static final AttackPattern singleTargetPattern;

    static {
        Map<Location,Float> locs = new HashMap<>();
        locs.put(new Location(0,0),1.0f);
        singleTargetPattern = new AttackPattern(1,false,locs);
    }

    public AttackPattern(int inputCount, boolean rotatable, Map<Location, Float> damageDistributionMap) {
        this.inputCount = inputCount;
        this.rotatable = rotatable;
        this.damageDistributionMap = damageDistributionMap;
    }

    public int getInputCount() {
        return inputCount;
    }

    public boolean isRotatable() {
        return rotatable;
    }

    public Map<Location, Float> getDamageDistributionMap() {
        return damageDistributionMap;
    }

    public Map<Location,Float> getDamageDistributionMap(Location centeredAround){
        Map<Location,Float> newMap = new HashMap<>();
        for(Map.Entry<Location,Float> entry : damageDistributionMap.entrySet()){
            newMap.put(new Location(centeredAround.getX()+entry.getKey().getX(),centeredAround.getY()+entry.getKey().getY()),entry.getValue());
        }
        return newMap;
    }


    public float damageForLocation(Location loc){
        return damageDistributionMap.containsKey(loc)?damageDistributionMap.get(loc):0f;
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject representation = new JSONObject();
        try {
            representation.put(ROTATABLE_KEY, isRotatable());
            representation.put(NUM_INPUT_KEY, getInputCount());
            representation.put(DAMAGE_MAP_KEY, JSONUtils.listify(getDamageDistributionMap(), entry -> {
                JSONObject obj = entry.getKey().getJSONRepresentation();
                try {
                    obj.put(DAMAGE_PERCENT_KEY,entry.getValue());
                } catch (JSONException e) {
                    //This should never happen - keys are all non-null.
                }
                return obj;
            }));
        } catch (JSONException e) {
            //This should never happen - all keys are not null
        }
        return representation;
    }
}
