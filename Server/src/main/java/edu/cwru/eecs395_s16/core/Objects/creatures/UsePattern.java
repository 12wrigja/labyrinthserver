package edu.cwru.eecs395_s16.core.objects.creatures;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.networking.Jsonable;
import edu.cwru.eecs395_s16.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 3/19/16.
 */
public class UsePattern implements Jsonable {

    public static final String EFFECT_MAP_KEY = "effect_map";
    public static final String EFFECT_PERCENT_KEY = "effect_percent";
    public static final String NUM_INPUT_KEY = "count";
    public static final String ROTATABLE_KEY = "rotatable";
    public static final UsePattern singleTargetPattern;
    private final int inputCount;
    private final boolean rotatable;
    private final Map<Location, Float> damageDistributionMap;

    static {
        Map<Location, Float> locs = new HashMap<>();
        locs.put(new Location(0, 0), 1.0f);
        singleTargetPattern = new UsePattern(1, false, locs);
    }

    public UsePattern(int inputCount, boolean rotatable, Map<Location, Float> damageDistributionMap) {
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

    public Map<Location, Float> getEffectDistributionMap() {
        return damageDistributionMap;
    }

    public Map<Location, Float> getEffectDistributionMap(Location centeredAround) {
        Map<Location, Float> newMap = new HashMap<>();
        for (Map.Entry<Location, Float> entry : damageDistributionMap.entrySet()) {
            newMap.put(new Location(centeredAround.getX() + entry.getKey().getX(), centeredAround.getY() + entry
                    .getKey().getY()), entry.getValue());
        }
        return newMap;
    }


    public float effectPercentForLocation(Location loc) {
        return damageDistributionMap.containsKey(loc) ? damageDistributionMap.get(loc) : 0f;
    }

    @Override
    public int hashCode() {
        int result = getInputCount();
        result = 31 * result + (isRotatable() ? 1 : 0);
        result = 31 * result + damageDistributionMap.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UsePattern that = (UsePattern) o;

        if (getInputCount() != that.getInputCount())
            return false;
        if (isRotatable() != that.isRotatable())
            return false;
        for (Map.Entry<Location, Float> entry : damageDistributionMap.entrySet()) {
            if (!that.damageDistributionMap.containsKey(entry.getKey())) {
                return false;
            }
            if (!that.damageDistributionMap.get(entry.getKey()).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject representation = new JSONObject();
        try {
            representation.put(ROTATABLE_KEY, isRotatable());
            representation.put(NUM_INPUT_KEY, getInputCount());
            representation.put(EFFECT_MAP_KEY, JSONUtils.listify(getEffectDistributionMap(), entry -> {
                JSONObject obj = entry.getKey().getJSONRepresentation();
                try {
                    obj.put(EFFECT_PERCENT_KEY, entry.getValue());
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
