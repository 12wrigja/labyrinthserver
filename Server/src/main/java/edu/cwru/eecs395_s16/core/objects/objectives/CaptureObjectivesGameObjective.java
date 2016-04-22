package edu.cwru.eecs395_s16.core.objects.objectives;

import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.objects.Location;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by james on 4/8/16.
 */
public class CaptureObjectivesGameObjective extends DeathmatchGameObjective {

    public static final String CAPTURE_OBJECTIVES_KEY = "capture_objectives";
    public static final String NUMBER_TOTAL_OBJECTIVES_KEY = "total_objectives";
    public static final String NUMBER_CAPTURED_OBJECTIVES_KEY = "captured_objectives";

//    final List<UUID> objectiveGameObjectIDs = new ArrayList<>();

    private int numObjectivesCaptured = 0;
    private int numberOfObjectives;

    public CaptureObjectivesGameObjective(int numberOfObjectives) {
        this.numberOfObjectives = numberOfObjectives;
    }

    @Override
    public GAME_WINNER checkForGameEnd(Match match) {
        numObjectivesCaptured = (int) match.getBoardObjects().stream().filter(obj -> obj instanceof ObjectiveGameObject).map(obj -> (ObjectiveGameObject) obj).filter(objective -> objective.getControllerID().isPresent() && objective.getControllerID().get().equals(match.getHeroPlayer().getUsername())).count();
        if (numberOfObjectives == numObjectivesCaptured) {
            return GAME_WINNER.HERO_WINNER;
        } else {
            return super.checkForGameEnd(match);
        }
    }

    @Override
    public void setup(Match match) {
        List<Location> objectiveSpawnLocations = match.getGameMap().getObjectiveSpawnLocations();
        Collections.shuffle(objectiveSpawnLocations);
        if (objectiveSpawnLocations.size() > 0) {
            numberOfObjectives = Math.min(objectiveSpawnLocations.size(), numberOfObjectives);
            for (int i = 0; i < numberOfObjectives; i++) {
                ObjectiveGameObject obj = new ObjectiveGameObject(UUID.randomUUID(), objectiveSpawnLocations.get(i));
                match.getBoardObjects().add(obj);
            }
        }
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject representation = super.getJSONRepresentation();
        try {
            representation.put(OBJECTIVE_TYPE_KEY, CAPTURE_OBJECTIVES_KEY);
            representation.put(NUMBER_TOTAL_OBJECTIVES_KEY, numberOfObjectives);
            representation.put(NUMBER_CAPTURED_OBJECTIVES_KEY, numObjectivesCaptured);
        } catch (JSONException e) {
            e.printStackTrace();
            //Should never happen as all the keys are non-null
        }
        return representation;
    }
}
