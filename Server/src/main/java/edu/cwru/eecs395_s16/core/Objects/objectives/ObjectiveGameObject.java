package edu.cwru.eecs395_s16.core.objects.objectives;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 3/17/16.
 */
public class ObjectiveGameObject extends GameObject {

    public ObjectiveGameObject(UUID objectID, Optional<String> ownerID, Optional<String> controllerID, Location location) {
        super(objectID, ownerID, controllerID, TYPE.OBJECTIVE, location);
    }

    public ObjectiveGameObject(UUID objectID, Location location) {
        super(objectID, Optional.empty(), Optional.empty(), TYPE.OBJECTIVE, location);
    }

    public static ObjectiveGameObject fromJSON(JSONObject obj){
        try {
            String id = obj.getString(GameObject.GAMEOBJECT_ID_KEY);
            UUID uuid = UUID.fromString(id);
            int x = obj.getInt(Location.X_KEY);
            int y = obj.getInt(Location.Y_KEY);
            String ownerID = obj.optString(GameObject.OWNER_ID_KEY,null);
            String controllerID = obj.optString(GameObject.CONTROLLER_ID_KEY,null);
            return new ObjectiveGameObject(uuid, Optional.ofNullable(ownerID),Optional.ofNullable(controllerID), new Location(x,y));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
