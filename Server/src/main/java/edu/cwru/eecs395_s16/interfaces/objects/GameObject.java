package edu.cwru.eecs395_s16.interfaces.objects;

import edu.cwru.eecs395_s16.interfaces.Jsonable;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/19/16.
 */
public interface GameObject extends Jsonable {

    Location getLocation();

    Optional<String> getOwnerID();

    UUID getGameObjectID();

}
