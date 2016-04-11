package edu.cwru.eecs395_s16.core.objects.creatures.monsters;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.creatures.CreatureBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.services.monsters.MonsterRepository;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 4/11/16.
 */
public class MonsterBuilder extends CreatureBuilder {

    protected String name;
    protected MonsterRepository.MonsterDefinition def;

    public MonsterBuilder(UUID objectID, MonsterRepository.MonsterDefinition monsterDefinition, String ownerID, Optional<String> controllerID) {
        super(objectID, monsterDefinition.id, ownerID, controllerID);
        this.def = monsterDefinition;
        this.attack = def.startAttack;
        this.defense = def.startDefense;
        this.movement = def.startMovement;
        this.vision = def.startVision;
        this.health = def.startHealth;
        this.maxHealth = def.startHealth;
        this.databaseIdentifier = def.id;
        Optional<Weapon> wep = GameEngine.instance().services.heroItemRepository.getWeaponForId(def.defaultWeaponId);
        if(wep.isPresent()){
            this.weapon = wep.get();
        }
    }

    @Override
    public CreatureBuilder fillFromJSON(JSONObject obj) throws JSONException {
        super.fillFromJSON(obj);
        setName(obj.getString(Monster.NAME_KEY));
        return this;
    }

    public MonsterBuilder setName(String name){
        this.name = name;
        return this;
    }

    public Monster createMonster(){
        return new Monster(objectID,Optional.of(ownerID),controllerID,def.id, GameObject.TYPE.MONSTER,attack,defense,health,maxHealth,movement,vision,actionPoints,maxActionPoints,abilities,statuses,location,weapon,def);
    }
}
