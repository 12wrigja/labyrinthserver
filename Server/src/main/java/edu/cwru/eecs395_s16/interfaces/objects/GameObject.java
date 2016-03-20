package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.Jsonable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/19/16.
 */
public abstract class GameObject implements Jsonable {

    public static final String GAMEOBJECT_TYPE_KEY = "type";
    public static final String OWNER_ID_KEY = "owner_id";
    public static final String GAMEOBJECT_ID_KEY = "id";
    public static final String CONTROLLER_ID_KEY = "controller_id";

    private final TYPE objectType;
    private final Optional<String> ownerID;
    private final UUID objectID;
    private final Optional<String> controllerID;
    private Location location = new Location(0, 0);

    protected GameObject(UUID objectID, Optional<String> ownerID, Optional<String> controllerID, TYPE objectType, Location location) {
        this.ownerID = ownerID;
        this.objectID = objectID;
        this.controllerID = controllerID;
        this.objectType = objectType;
        this.location = location;
    }

    public TYPE getGameObjectType() {
        return objectType;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location loc) {
        this.location = loc;
    }

    public Optional<String> getOwnerID() {
        return ownerID;
    }

    public UUID getGameObjectID() {
        return objectID;
    }

    public Optional<String> getControllerID() {
        return controllerID;
    }

    @Override
    public JSONObject getJSONRepresentation() {
        //Setup json representation
        JSONObject representation = new JSONObject();
        try {
            representation.put(GAMEOBJECT_ID_KEY, getGameObjectID());
            representation.put(Location.X_KEY, location.getX());
            representation.put(Location.Y_KEY, location.getY());
            representation.put(GAMEOBJECT_TYPE_KEY, getGameObjectType().toString().toLowerCase());
            representation.put(OWNER_ID_KEY, getOwnerID().isPresent() ? getOwnerID().get() : null);
            representation.put(CONTROLLER_ID_KEY, getOwnerID().isPresent() ? getOwnerID().get() : null);
        } catch (JSONException e) {
            //Never will occur - all keys are non-null
        }
        return representation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameObject)) return false;

        GameObject object = (GameObject) o;

        return objectID.equals(object.objectID);

    }

    @Override
    public int hashCode() {
        return objectID.hashCode();
    }

    public enum TYPE {
        HERO,
        MONSTER,
        TRAP,
        OBJECTIVE,
        UNKNOWN
    }
}
