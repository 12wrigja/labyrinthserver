package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.core.objects.AttackPattern;
import edu.cwru.eecs395_s16.interfaces.Jsonable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 1/19/16.
 */
public class Weapon implements DatabaseObject, Jsonable {

    public static final String IMAGE_KEY = "image";
    public static final String RANGE_KEY = "range";
    private final int range;
    public static final String NAME_KEY = "name";
    public static final String DESCRIPTION_KEY = "description";
    public static final String DAMAGE_MOD_KEY = "damage_mod";
    private static final String ATTACK_PATTERN_KEY = "attack_pattern";
    private final int databaseID;
    private final String image;
    private final String name;
    private final String description;
    private final int damageModifier;
    private final AttackPattern attackPattern;

    public Weapon(int databaseID, String image, String name, String description, int range, int damageModifier, AttackPattern attackPattern) {
        this.databaseID = databaseID;
        this.image = image;
        this.name = name;
        this.description = description;
        this.range = range;
        this.damageModifier = damageModifier;
        this.attackPattern = attackPattern;
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

    public AttackPattern getAttackPattern() {
        return attackPattern;
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject representation = new JSONObject();
        try {
            representation.put(IMAGE_KEY, getImage());
            representation.put(DATABASE_ID_KEY, getDatabaseID());
            representation.put(NAME_KEY, getName());
            representation.put(DESCRIPTION_KEY, getDescription());
            representation.put(RANGE_KEY,getRange());
            representation.put(DAMAGE_MOD_KEY, getDamageModifier());
            representation.put(ATTACK_PATTERN_KEY, getAttackPattern().getJSONRepresentation());
        } catch (JSONException e) {
            //This should never happen - all keys are not null
        }
        return representation;
    }
}
