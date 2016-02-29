package edu.cwru.eecs395_s16.networking.requests.gameactions;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 2/21/16.
 */
public class MoveGameActionData implements RequestData {

    private String character_id;
    private List<Location> path;

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.character_id = RequestData.getString(obj,"character_id");
        this.path = new ArrayList<>();
        try {
            JSONArray pathArr = obj.getJSONArray("path");
            for(int i=0; i<pathArr.length(); i++){
                JSONObject location = pathArr.getJSONObject(i);
                int x = location.getInt("x");
                int y = location.getInt("y");
                Location l = new Location(x,y);
                path.add(l);
            }
        } catch (JSONException e) {
            if(GameEngine.instance().IS_DEBUG_MODE){
                e.printStackTrace();
            }
            throw new InvalidDataException("path");
        }
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("character_id", character_id);
            repr.put("path",path);
            repr.put("type","move");
        } catch (JSONException e) {
            //Should not happen b/c keys are not null.
        }
        return repr;
    }

    public String getCharacterID() {
        return character_id;
    }

    public List<Location> getPath() {
        return path;
    }

    public void setCharacter(String character_id) {
        this.character_id = character_id;
    }

    public void setPath(List<Location> path) {
        this.path = path;
    }
}
