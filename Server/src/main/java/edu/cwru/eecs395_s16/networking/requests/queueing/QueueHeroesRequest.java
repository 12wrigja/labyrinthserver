package edu.cwru.eecs395_s16.networking.requests.queueing;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james on 4/10/16.
 */
public class QueueHeroesRequest extends QueueRequest {

    Set<UUID> selectedHeroesIds;

    public QueueHeroesRequest(){
        selectedHeroesIds = null;
    }

    public QueueHeroesRequest(boolean queueWithPassBot, int mapX, int mapY, Set<UUID> selectedHeroesIds) {
        super(queueWithPassBot, mapX, mapY, -1, "dm");
        this.selectedHeroesIds = selectedHeroesIds;
    }

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        super.fillFromJSON(obj);
        if(obj.has("heroes")){
            selectedHeroesIds = new HashSet<>();
            try{
                JSONArray heroesArr = obj.getJSONArray("heroes");
                for(int i=0; i<heroesArr.length(); i++) {
                    String id = heroesArr.getString(i);
                    UUID uuid = UUID.fromString(id);
                    selectedHeroesIds.add(uuid);
                }
            } catch (IllegalArgumentException e1){
                throw new InvalidDataException("heroes");
            } catch (JSONException e){
                throw new InvalidDataException("heroes");
            }
        }
    }

    public Set<UUID> getSelectedHeroesIds() {
        return selectedHeroesIds;
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject repr = super.convertToJSON();
        try {
            JSONArray heroIDArray = new JSONArray();
            for(UUID id : selectedHeroesIds){
                heroIDArray.put(id.toString());
            }
            repr.put("heroes", heroIDArray);
        } catch (JSONException e){
            //should never happen - no-null keys
        }
        return repr;
    }
}
