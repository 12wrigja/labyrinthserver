package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.interfaces.Jsonable;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/19/16.
 */
public interface GameObject extends Jsonable {

    enum TYPE {
        HERO,
        MONSTER,
        TRAP
    }

    String GAMEOBJECT_TYPE_KEY = "type";
    TYPE getGameObjectType();

    String LOCATION_KEY = "location";
    Location getLocation();

    void setLocation(Location loc);

    String OWNER_ID_KEY = "owner_id";
    Optional<String> getOwnerID();

    String GAMEOBJECT_ID_KEY = "id";
    UUID getGameObjectID();

    String CONTROLLER_ID_KEY = "controller_id";
    Optional<String> getControllerID();

}
