package edu.cwru.eecs395_s16.core.objects.creatures.monsters;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.creatures.CreatureBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.services.monsters.MonsterRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 4/11/16.
 */
public class MonsterBuilder extends CreatureBuilder {

    protected String name;
    protected MonsterRepository.MonsterDefinition def;

    public MonsterBuilder(UUID objectID, int databaseIdentifier, String ownerID, Optional<String> controllerID, MonsterRepository.MonsterDefinition definition) {
        super(objectID, databaseIdentifier, ownerID, controllerID);
        this.def = definition;
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

    public MonsterBuilder setName(String name){
        this.name = name;
        return this;
    }

    public Monster createMonster(){
        return new Monster(objectID,Optional.of(ownerID),controllerID,def.id, GameObject.TYPE.MONSTER,attack,defense,health,maxHealth,movement,vision,actionPoints,maxActionPoints,abilities,statuses,location,weapon,def);
    }
}
