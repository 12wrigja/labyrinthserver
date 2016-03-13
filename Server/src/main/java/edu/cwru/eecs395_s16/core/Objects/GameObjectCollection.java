package edu.cwru.eecs395_s16.core.objects;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.interfaces.Jsonable;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by james on 2/21/16.
 */
public class GameObjectCollection implements Jsonable {

    Map<UUID,GameObject> allObjects = new ConcurrentHashMap<>();

    public Optional<GameObject> getByID(UUID gameObjectID) {
        GameObject matchingUUID = allObjects.get(gameObjectID);
        if (matchingUUID != null) {
            return Optional.of(matchingUUID);
        }
        return Optional.empty();
    }

    public int size() {
        return allObjects.size();
    }

    public boolean isEmpty() {
        return allObjects.isEmpty();
    }

    public boolean containsGameObject(UUID gameObjectID) {
        return allObjects.containsKey(gameObjectID);
    }

    public GameObject get(UUID key) {
        return allObjects.get(key);
    }

    public GameObject add(GameObject value) {
        return allObjects.put(value.getGameObjectID(), value);
    }

    public GameObject remove(UUID key) {
        return allObjects.remove(key);
    }

    public void putAll(Map<? extends UUID, ? extends GameObject> m) {
        allObjects.putAll(m);
    }

    public List<GameObject> getForPlayerOwner(Player p) {
        return allObjects.values().stream().filter(gameObject -> {
            Optional<String> playerID = gameObject.getOwnerID();
            return playerID.isPresent() && playerID.get().equals(p.getUsername());
        }).collect(Collectors.toList());
    }

    public List<GameObject> getForLocation(Location loc) {
        return allObjects.values().stream().filter(gameObject -> gameObject.getLocation().equals(loc)).collect(Collectors.toList());
    }

    public void addAll(Collection<? extends GameObject> gameObjects){
        for(GameObject gObj : gameObjects){
            this.add(gObj);
        }
    }

    public void fillFromJSONData(JSONObject data){
        GameObjectFactory gFact = new GameObjectFactory();
        Iterator uuidIterator = data.keys();
        while(uuidIterator.hasNext()){
            String key = (String)uuidIterator.next();
            try {
                Optional<GameObject> gOpt = gFact.objectFromJson(data.getJSONObject(key));
                if(gOpt.isPresent()) {
                    GameObject gObj = gOpt.get();
                    this.add(gObj);
                }
            } catch (JSONException e) {
                if(GameEngine.instance().IS_DEBUG_MODE) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject repr = new JSONObject();
        for(UUID key : allObjects.keySet()){
            try {
                //JSONRepresentation Change
                repr.put(key.toString(),allObjects.get(key).getJSONRepresentation());
            } catch (JSONException e) {
                //This should never be called - the key is always non-null
            }
        }
        return repr;
    }
}
