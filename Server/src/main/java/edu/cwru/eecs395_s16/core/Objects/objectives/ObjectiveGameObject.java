package edu.cwru.eecs395_s16.core.objects.objectives;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 3/17/16.
 */
public class ObjectiveGameObject extends GameObject {

    public ObjectiveGameObject(UUID objectID, Location location) {
        super(objectID, Optional.empty(), Optional.empty(), TYPE.OBJECTIVE, location);
    }

    public static ObjectiveGameObject fromJSON(JSONObject obj){
        try {
            String id = obj.getString(GameObject.GAMEOBJECT_ID_KEY);
            UUID uuid = UUID.fromString(id);
            int x = obj.getInt(Location.X_KEY);
            int y = obj.getInt(Location.Y_KEY);
            return new ObjectiveGameObject(uuid, new Location(x,y));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
