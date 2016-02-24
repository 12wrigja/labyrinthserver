package edu.cwru.eecs395_s16.core.objects;

import edu.cwru.eecs395_s16.core.objects.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.cert.PKIXRevocationChecker;
import java.util.Optional;

/**
 * Created by james on 2/22/16.
 */
public class GameObjectFactory {

    enum GAMEOBJECTIMPL {
        UNKNOWN,
        HERO
    }

    Optional<GameObject> objectFromJson(JSONObject obj){
        GAMEOBJECTIMPL impl = GAMEOBJECTIMPL.valueOf(obj.optString("type",GAMEOBJECTIMPL.UNKNOWN.toString()).toUpperCase());
        //TODO update this when we add in concrete classes for each of the hero, monster, trap, etc game objects
        switch(impl){
            case HERO: {
                HeroBuilder hb = new HeroBuilder();
                try {
                    return Optional.ofNullable(hb.fillFromJSON(obj).createHero());
                } catch (JSONException e) {
                    return Optional.empty();
                }
            }
            default:
            case UNKNOWN: {
                return Optional.empty();
            }
        }
    }


}
