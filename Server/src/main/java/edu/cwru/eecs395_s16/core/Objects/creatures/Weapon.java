package edu.cwru.eecs395_s16.core.objects.creatures;

import edu.cwru.eecs395_s16.core.objects.DatabaseObject;
import edu.cwru.eecs395_s16.networking.Jsonable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 1/19/16.
 */
public class Weapon implements DatabaseObject, Jsonable {

    public static final String IMAGE_KEY = "image";
    public static final String RANGE_KEY = "range";
    public static final Weapon DEFAULT_NO_WEAPON = new Weapon(-1, "fists", "Fists", "Use your fists!", 1, 1, UsePattern.singleTargetPattern);
    public static final String NAME_KEY = "name";
    public static final String DESCRIPTION_KEY = "description";
    public static final String DAMAGE_MOD_KEY = "damage_mod";
    private static final String ATTACK_PATTERN_KEY = "attack_pattern";
    private final int range;
    private final int databaseID;
    private final String image;
    private final String name;
    private final String description;
    private final int damageModifier;
    private final UsePattern usePattern;

    public Weapon(int databaseID, String image, String name, String description, int range, int damageModifier, UsePattern usePattern) {
        this.databaseID = databaseID;
        this.image = image;
        this.name = name;
        this.description = description;
        this.range = range;
        this.damageModifier = damageModifier;
        this.usePattern = usePattern;
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

    public UsePattern getUsePattern() {
        return usePattern;
    }

    @Override
    public int hashCode() {
        int result = getRange();
        result = 31 * result + getDatabaseID();
        result = 31 * result + getImage().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getDamageModifier();
        result = 31 * result + getUsePattern().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Weapon weapon = (Weapon) o;

        if (getRange() != weapon.getRange()) return false;
        if (getDatabaseID() != weapon.getDatabaseID()) return false;
        if (getDamageModifier() != weapon.getDamageModifier()) return false;
        if (!getImage().equals(weapon.getImage())) return false;
        if (!getName().equals(weapon.getName())) return false;
        if (!getDescription().equals(weapon.getDescription())) return false;
        return getUsePattern().equals(weapon.getUsePattern());

    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject representation = new JSONObject();
        try {
            representation.put(IMAGE_KEY, getImage());
            representation.put(DATABASE_ID_KEY, getDatabaseID());
            representation.put(NAME_KEY, getName());
            representation.put(DESCRIPTION_KEY, getDescription());
            representation.put(RANGE_KEY, getRange());
            representation.put(DAMAGE_MOD_KEY, getDamageModifier());
            representation.put(ATTACK_PATTERN_KEY, getUsePattern().getJSONRepresentation());
        } catch (JSONException e) {
            //This should never happen - all keys are not null
        }
        return representation;
    }
}
