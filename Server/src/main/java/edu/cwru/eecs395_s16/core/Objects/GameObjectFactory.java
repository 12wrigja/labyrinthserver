package edu.cwru.eecs395_s16.core.objects;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.creatures.CreatureBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterBuilder;
import edu.cwru.eecs395_s16.core.objects.objectives.ObjectiveGameObject;
import edu.cwru.eecs395_s16.services.monsters.MonsterRepository;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/22/16.
 */
public class GameObjectFactory {

    protected UUID objectID;
    protected String ownerID;
    protected Optional<String> controllerID;
    protected GameObject.TYPE objectType;
    protected Location location;

    Optional<GameObject> fillFromJson(JSONObject obj){
        String objectID = obj.optString(GameObject.GAMEOBJECT_ID_KEY,UUID.randomUUID().toString());
        UUID goID = UUID.fromString(objectID);
        String ownerID = obj.optString(GameObject.CONTROLLER_ID_KEY, null);
        String controllerID = obj.optString(GameObject.CONTROLLER_ID_KEY,null);
        GameObject.TYPE impl = GameObject.TYPE.valueOf(obj.optString(GameObject.GAMEOBJECT_TYPE_KEY, GameObject.TYPE.UNKNOWN.toString()).toUpperCase());
        switch(impl){
            case HERO: {
                try {
                    HeroType type = HeroType.valueOf(obj.getString(Hero.HERO_TYPE_KEY).toUpperCase());
                    HeroBuilder hb = new HeroBuilder(goID,ownerID, Optional.of(controllerID), -1, type);
                    return Optional.ofNullable(hb.fillFromJSON(obj).createHero());
                } catch (JSONException e) {
                    return Optional.empty();
                }
            }
            case OBJECTIVE: {
                    return Optional.ofNullable(ObjectiveGameObject.fromJSON(obj));
            }
            case MONSTER: {
                try {
                    int monsterDBID = obj.getInt(Monster.DATABASE_ID_KEY);
                    InternalResponseObject<MonsterRepository.MonsterDefinition> defnResp = GameEngine.instance().services.monsterRepository.getMonsterDefinitionForId(monsterDBID);
                    if(defnResp.isNormal()) {
                        MonsterBuilder cb = new MonsterBuilder(goID, defnResp.get(), ownerID, Optional.of(controllerID));
                        return Optional.ofNullable(cb.fillFromJSON(obj).createCreature());
                    } else {
                        Optional.empty();
                    }
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
