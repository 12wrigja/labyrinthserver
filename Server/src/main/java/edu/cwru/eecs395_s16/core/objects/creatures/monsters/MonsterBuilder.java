package edu.cwru.eecs395_s16.core.objects.creatures.monsters;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.DatabaseObject;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.creatures.CreatureBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 4/11/16.
 */
public class MonsterBuilder extends CreatureBuilder {

    protected String name;

    public MonsterBuilder(UUID objectID, MonsterDefinition monsterDefinition, String ownerID, Optional<String>
            controllerID) {
        super(objectID, monsterDefinition.id, ownerID, controllerID);
        this.attack = monsterDefinition.startAttack;
        this.defense = monsterDefinition.startDefense;
        this.movement = monsterDefinition.startMovement;
        this.vision = monsterDefinition.startVision;
        this.health = monsterDefinition.startHealth;
        this.maxHealth = monsterDefinition.startHealth;
        this.databaseIdentifier = monsterDefinition.id;
        Optional<Weapon> wep = GameEngine.instance().services.heroItemRepository.getWeaponForId(monsterDefinition
                .defaultWeaponId);
        if (wep.isPresent()) {
            this.weapon = wep.get();
        }
        this.name = monsterDefinition.name;
    }

    private MonsterBuilder(UUID objectID, int databaseID, String ownerID, Optional<String> controllerID) {
        super(objectID, databaseID, ownerID, controllerID);
    }

    public MonsterBuilder(UUID newID, Monster template) {
        super(newID, template.getDatabaseID(), template.getOwnerID().isPresent() ? template.getOwnerID().get() :
                null, template.getControllerID());
        try {
            JSONObject templateObj = new JSONObject(template.getJSONRepresentation().toString());
            fillFromJSON(templateObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        objectID = newID;
    }

    public static InternalResponseObject<MonsterBuilder> fromJSONObject(JSONObject obj) {
        try {
            UUID gameObjectID = UUID.fromString(obj.getString(GameObject.GAMEOBJECT_ID_KEY));
            int dbID = obj.getInt(DatabaseObject.DATABASE_ID_KEY);
            String ownerID = obj.getString(GameObject.OWNER_ID_KEY);
            Optional<String> controllerID = Optional.ofNullable(obj.getString(GameObject.CONTROLLER_ID_KEY));
            MonsterBuilder mb = new MonsterBuilder(gameObjectID, dbID, ownerID, controllerID);
            mb.fillFromJSON(obj);
            return new InternalResponseObject<>(mb);
        } catch (JSONException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR);
        } catch (IllegalArgumentException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    public CreatureBuilder fillFromJSON(JSONObject obj) throws JSONException {
        super.fillFromJSON(obj);
        setName(obj.getString(Monster.NAME_KEY));
        return this;
    }

    public MonsterBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public MonsterDefinition createMonsterDefinition(int quantity) {
        return new MonsterDefinition(databaseIdentifier, name, attack, defense, health, movement, vision, weapon
                .getDatabaseID(), quantity);
    }

    public Monster createMonster() {
        return new Monster(objectID, Optional.of(ownerID), controllerID, databaseIdentifier, name, attack, defense,
                health, maxHealth, movement, vision, actionPoints, maxActionPoints, abilities, statuses, location,
                weapon);
    }
}
