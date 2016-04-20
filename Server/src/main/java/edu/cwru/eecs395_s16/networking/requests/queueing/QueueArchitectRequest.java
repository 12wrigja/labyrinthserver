package edu.cwru.eecs395_s16.networking.requests.queueing;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.core.objects.Location;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by james on 4/10/16.
 */
public class QueueArchitectRequest extends QueueRequest {

    Map<Location,Integer> monsterLocationMap;

    public QueueArchitectRequest(){
        monsterLocationMap = null;
    }

    public QueueArchitectRequest(boolean queueWithPassBot, int mapX, int mapY, Map<Location,Integer> monsterLocationMap) {
        super(queueWithPassBot, mapX, mapY, -1, "dm");
        this.monsterLocationMap = monsterLocationMap;
    }

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        super.fillFromJSON(obj);
        if(obj.has("monsters")){
            monsterLocationMap = new HashMap<>();
            try{
                JSONArray heroesArr = obj.getJSONArray("monsters");
                for(int i=0; i<heroesArr.length(); i++) {
                    int x = heroesArr.getJSONObject(i).getInt("x");
                    int y = heroesArr.getJSONObject(i).getInt("y");
                    int monsterDBID = heroesArr.getJSONObject(i).getInt("id");
                    monsterLocationMap.put(new Location(x,y),monsterDBID);
                }
            } catch (JSONException e){
                throw new InvalidDataException("monsters");
            } catch (IllegalArgumentException e){
                throw new InvalidDataException("monsters");
            }
        }
    }

    public Map<Location, Integer> getMonsterLocationMap() {
        return monsterLocationMap;
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject repr = super.convertToJSON();
        if(monsterLocationMap != null) {
            try {
                JSONArray monsterLocationArray = new JSONArray();
                for (Map.Entry<Location, Integer> entry : monsterLocationMap.entrySet()) {
                    JSONObject obj = new JSONObject();
                    obj.put("x",entry.getKey().getX());
                    obj.put("y",entry.getKey().getY());
                    obj.put("id",entry.getValue());
                    monsterLocationArray.put(obj);
                }
            } catch (JSONException e) {
                //should never happen - no-null keys
            }
        }
        return repr;
    }
}
