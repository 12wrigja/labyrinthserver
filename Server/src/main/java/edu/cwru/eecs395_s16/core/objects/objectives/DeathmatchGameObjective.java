package edu.cwru.eecs395_s16.core.objects.objectives;

import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 3/26/16.
 */
public class DeathmatchGameObjective extends GameObjective {

    public static final String DEATHMATCH_KEY = "deathmatch";
    public static final String ALIVE_HERO_OBJECTS_KEY = "alive_heroes";
    public static final String ALIVE_ARCHITECT_OBJECTS_KEY = "alive_architect_objects";
    private long numAliveHeroesObjects;
    private long numAliveArchitectObjects;

    @Override
    public GAME_WINNER checkForGameEnd(Match match) {
        numAliveHeroesObjects = match.getBoardObjects().getForPlayerOwner(match.getHeroPlayer()).stream().filter(obj
                -> (obj instanceof Creature) && ((Creature) obj).getHealth() > 0).count();
        numAliveArchitectObjects = match.getBoardObjects().getForPlayerOwner(match.getArchitectPlayer()).stream()
                .filter(obj -> (obj instanceof Creature) && ((Creature) obj).getHealth() > 0).count();
        if (numAliveArchitectObjects == 0 && numAliveHeroesObjects == 0) {
            return GAME_WINNER.TIE;
        } else if (numAliveArchitectObjects > 0 && numAliveHeroesObjects > 0) {
            return GAME_WINNER.NO_WINNER;
        } else if (numAliveHeroesObjects > 0) {
            return GAME_WINNER.HERO_WINNER;
        } else {
            return GAME_WINNER.ARCHITECT_WINNER;
        }
    }

    @Override
    public void setup(Match match) {
        //Do nothing - deathmatch needs no setup.
    }


    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject representation = new JSONObject();
        try {
            representation.put(OBJECTIVE_TYPE_KEY, DEATHMATCH_KEY);
            representation.put(ALIVE_HERO_OBJECTS_KEY, numAliveHeroesObjects);
            representation.put(ALIVE_ARCHITECT_OBJECTS_KEY, numAliveArchitectObjects);
        } catch (JSONException e) {
            e.printStackTrace();
            //Should never happen as all the keys are non-null
        }
        return representation;
    }
}
