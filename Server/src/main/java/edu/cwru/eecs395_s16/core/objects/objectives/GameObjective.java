package edu.cwru.eecs395_s16.core.objects.objectives;

import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.networking.Jsonable;

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

    public static GameObjective objectiveForKey(String key){
        //TODO update this to include capture game objectives
        if(key.equals(DeathmatchGameObjective.DEATHMATCH_KEY)){
            return new DeathmatchGameObjective();
        } else {
            return null;
        }
    }

}
