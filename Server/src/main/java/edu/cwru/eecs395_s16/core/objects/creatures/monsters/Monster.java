package edu.cwru.eecs395_s16.core.objects.creatures.monsters;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.Ability;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.CreatureStatus;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 4/11/16.
 */
public class Monster extends Creature {

    public static final String NAME_KEY = "name";

    protected String name;

    public Monster(UUID objectID, Optional<String> ownerID, Optional<String> controllerID, int databaseId, String name, int attack, int defense, int currentHealth, int maxHealth, int movement, int vision, int currentActionPoints, int maxActionPoints, List<Ability> abilities, List<CreatureStatus> statuses, Location location, Weapon weapon) {
        super(objectID, ownerID, controllerID, databaseId, TYPE.MONSTER, attack, defense, currentHealth, maxHealth, movement, vision, currentActionPoints, maxActionPoints, abilities, statuses, location, weapon);
        this.name = name;
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject repr = super.getJSONRepresentation();
        try {
            repr.put(NAME_KEY, name);
        } catch (JSONException e) {
            //DO nothing should never happen
        }
        return repr;
    }
}
