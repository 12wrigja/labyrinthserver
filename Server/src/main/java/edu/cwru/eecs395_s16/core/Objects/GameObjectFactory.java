package edu.cwru.eecs395_s16.core.objects;

import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.objectives.ObjectiveGameObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Created by james on 2/22/16.
 */
public class GameObjectFactory {

    Optional<GameObject> objectFromJson(JSONObject obj){
        GameObject.TYPE impl = GameObject.TYPE.valueOf(obj.optString("type", GameObject.TYPE.UNKNOWN.toString()).toUpperCase());
        //TODO update this when we add in concrete classes for each of the hero, monster, trap, etc game objects
        switch(impl){
            case HERO: {
                try {
                    HeroBuilder hb = new HeroBuilder(obj.getString(GameObject.OWNER_ID_KEY));
                    return Optional.ofNullable(hb.fillFromJSON(obj).createHero());
                } catch (JSONException e) {
                    return Optional.empty();
                }
            }
            case OBJECTIVE: {
                    return Optional.ofNullable(ObjectiveGameObject.fromJSON(obj));
            }
            default:
            case UNKNOWN: {
                return Optional.empty();
            }
        }
    }


}
