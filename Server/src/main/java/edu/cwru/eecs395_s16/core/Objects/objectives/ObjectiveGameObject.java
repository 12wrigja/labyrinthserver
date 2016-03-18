package edu.cwru.eecs395_s16.core.objects.objectives;

import edu.cwru.eecs395_s16.interfaces.objects.GameObject;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 3/17/16.
 */
public class ObjectiveGameObject extends GameObject {

    public ObjectiveGameObject(UUID objectID) {
        super(objectID, Optional.empty(), Optional.empty(), TYPE.OBJECTIVE);
    }
}
