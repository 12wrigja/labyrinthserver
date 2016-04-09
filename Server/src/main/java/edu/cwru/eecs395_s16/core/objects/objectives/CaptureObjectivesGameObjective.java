package edu.cwru.eecs395_s16.core.objects.objectives;

import edu.cwru.eecs395_s16.core.Match;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.jar.Pack200;

/**
 * Created by james on 4/8/16.
 */
public class CaptureObjectivesGameObjective extends DeathmatchGameObjective {

    public static final String CAPTURE_OBJECTIVES_KEY = "capture_objectives";
    public static final String NUMBER_TOTAL_OBJECTIVES_KEY = "total_objectives";
    public static final String NUMBER_CAPTURED_OBJECTIVES_KEY = "captured_objectives";

    List<UUID> objectiveGameObjectIDs = new ArrayList<>();

    private int numObjectivesCaptured = 0;

    public CaptureObjectivesGameObjective(ObjectiveGameObject... objectivesToCapture) {
        for(ObjectiveGameObject obj : objectivesToCapture){
            objectiveGameObjectIDs.add(obj.getGameObjectID());
        }
    }

    @Override
    public GAME_WINNER checkForGameEnd(Match match) {
        numObjectivesCaptured = (int)objectiveGameObjectIDs.stream().map(id->(ObjectiveGameObject)match.getBoardObjects().getByID(id).get()).filter(objective->objective.getControllerID().equals(match.getHeroPlayer().getUsername())).count();
        if(objectiveGameObjectIDs.size() == numObjectivesCaptured){
            return GAME_WINNER.HERO_WINNER;
        } else {
            return super.checkForGameEnd(match);
        }
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject representation = super.getJSONRepresentation();
        try {
            representation.put(OBJECTIVE_TYPE_KEY,CAPTURE_OBJECTIVES_KEY);
            representation.put(NUMBER_TOTAL_OBJECTIVES_KEY,objectiveGameObjectIDs.size());
            representation.put(NUMBER_CAPTURED_OBJECTIVES_KEY,numObjectivesCaptured);
        } catch (JSONException e) {
            e.printStackTrace();
            //Should never happen as all the keys are non-null
        }
        return representation;
    }
}
