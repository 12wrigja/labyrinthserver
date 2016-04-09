package edu.cwru.eecs395_s16.core.objects.objectives;

import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.networking.Jsonable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 3/26/16.
 */
public abstract class GameObjective implements Jsonable {

    public static final String OBJECTIVE_TYPE_KEY = "objective_type";

    public enum GAME_WINNER {
        NO_WINNER,
        HERO_WINNER,
        ARCHITECT_WINNER,
        TIE
    }

    public abstract GAME_WINNER checkForGameEnd(Match m);

    public static GameObjective objectiveForJSON(JSONObject json){
        try {
            String typeKey = json.getString(GameObjective.OBJECTIVE_TYPE_KEY);
            switch (typeKey) {
                case DeathmatchGameObjective.DEATHMATCH_KEY:
                    return new DeathmatchGameObjective();
                case CaptureObjectivesGameObjective.CAPTURE_OBJECTIVES_KEY:
                    int numObjectives = json.getInt(CaptureObjectivesGameObjective.NUMBER_TOTAL_OBJECTIVES_KEY);
                    return new CaptureObjectivesGameObjective(numObjectives);
                default:
                    return null;
            }
        } catch (JSONException e){
            return null;
        }
    }

    public abstract void setup(Match match);

}
