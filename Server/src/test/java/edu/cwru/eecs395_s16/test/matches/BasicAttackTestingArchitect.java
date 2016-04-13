package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterDefinition;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.services.bots.botimpls.PassBot;
import edu.cwru.eecs395_s16.test.InMatchTest;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by james on 3/23/16.
 */
public class BasicAttackTestingArchitect extends InMatchTest {

    @Override
    protected Map<Location, Integer> placeArchitectMonsters() {
        Map<Location,Integer> ret = new HashMap<>();
        List<Location> archSpawnTiles = initialGameMap.getArchitectCreatureSpawnLocations();
        Collections.shuffle(archSpawnTiles);
        int placedMonsters = 0;
        for(GameObject obj :architectBot.getArchitectObjects()){
            if(obj instanceof Monster){
                ret.put(archSpawnTiles.get(placedMonsters),((Monster)obj).getDatabaseID());
                placedMonsters++;
            }
        }
        return ret;
    }

    @Override
    protected GameBot getHero() {
        PassBot b = new PassBot();
        b.setEnabled(false);
        return b;
    }

    @Test
    public void testBasicAttacking() {
        initialHeroes = new ArrayList<>();
        initialHeroes.add(new HeroBuilder(heroBot.getUsername(), HeroType.MAGE).createHero());
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        List<GameObject> a = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(h.size() == 1);
        Creature creature = (Creature) a.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(0, 1), architectBot);
        forceSetCharacterLocation(creature.getGameObjectID(), new Location(0, 2), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(0,1));
        ((PassBot)heroBot).passAllCharacters();
        basicAttackWithCharacter(architectBot, creature.getGameObjectID(), inputs, true);
    }

    @Test
    public void testAttackingOutOfRange() {
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        List<GameObject> a = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(h.size() == 1);
        Creature creature = (Creature) a.get(0);

        ((PassBot)heroBot).passAllCharacters();

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(0, 1), architectBot);
        forceSetCharacterLocation(creature.getGameObjectID(), new Location(0, 3), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(0, 5));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(architectBot, creature.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NOT_IN_RANGE, resp.getInternalErrorCode());
    }


    @Test
    public void testAttackingFriendlyCharacter() {
        initialArchitectObjects = new ArrayList<>();
        MonsterDefinition goblinDef = GameEngine.instance().services.monsterRepository.getMonsterDefinitionForId(1).get();
        initialArchitectObjects.add(new MonsterBuilder(UUID.randomUUID(),goblinDef,architectBot.getUsername(), Optional.of(architectBot.getUsername())).createMonster());
        initialArchitectObjects.add(new MonsterBuilder(UUID.randomUUID(),goblinDef,architectBot.getUsername(), Optional.of(architectBot.getUsername())).createMonster());
        setupMatch(true);

        List<GameObject> monsters = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(monsters.size() >= 2);
        Monster monster1 = (Monster) monsters.get(0);
        Monster monster2 = (Monster) monsters.get(1);

        ((PassBot)heroBot).passAllCharacters();

        forceSetCharacterLocation(monster1.getGameObjectID(), new Location(0, 1), architectBot);
        forceSetCharacterLocation(monster2.getGameObjectID(), new Location(0, 2), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(0,2));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(architectBot, monster1.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.FRIENDLY_FIRE, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWall() {
        setupMatch(true);
        List<GameObject> monsters = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(monsters.size() >= 1);
        Monster monster = (Monster) monsters.get(0);

        ((PassBot)heroBot).passAllCharacters();

        forceSetCharacterLocation(monster.getGameObjectID(), new Location(3, 2), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(4, 2));
        basicAttackWithCharacter(architectBot, monster.getGameObjectID(), inputs, true);
    }

    @Test
    public void testAttackEmptyTile() {
        setupMatch(true);
        List<GameObject> monsters = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(monsters.size() >= 1);
        Monster monster = (Monster) monsters.get(0);

        ((PassBot)heroBot).passAllCharacters();

        forceSetCharacterLocation(monster.getGameObjectID(), new Location(1, 2), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 3));
        basicAttackWithCharacter(architectBot, monster.getGameObjectID(), inputs, true);
    }

    @Test
    public void testAttackSelf() {
        setupMatch(true);
        List<GameObject> monsters = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(monsters.size() == 1);
        Monster monster = (Monster) monsters.get(0);

        ((PassBot)heroBot).passAllCharacters();

        forceSetCharacterLocation(monster.getGameObjectID(), new Location(1, 2), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1,2));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(architectBot, monster.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.FRIENDLY_FIRE, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackNonexistantTile() {
        setupMatch(true);
        List<GameObject> monsters = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(monsters.size() >= 1);
        Monster monster = (Monster) monsters.get(0);

        ((PassBot)heroBot).passAllCharacters();

        forceSetCharacterLocation(monster.getGameObjectID(), new Location(0, 1), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(-1, 1));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(architectBot, monster.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.INVALID_LOCATION, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWithEnemyUnit() {
        setupMatch(true);
        List<GameObject> heroes = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroes.size() >= 1);
        Hero hero = (Hero) heroes.get(0);

        ((PassBot)heroBot).passAllCharacters();

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2), heroBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 3));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(architectBot, hero.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NOT_CONTROLLER, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWithTooManyInputs() {
        setupMatch(true);
        List<GameObject> monsters = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(monsters.size() >= 1);
        Monster monster = (Monster) monsters.get(0);

        ((PassBot)heroBot).passAllCharacters();

        forceSetCharacterLocation(monster.getGameObjectID(), new Location(1, 2), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 4));
        inputs.add(new Location(1,3));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(architectBot, monster.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.TOO_MANY_TARGETS, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWithTooFewInputs() {
        Weapon w = GameEngine.instance().services.heroItemRepository.getWeaponForId(6).get();
        initialArchitectObjects = new ArrayList<>();
        MonsterDefinition dualShotGoblin = new WeaponModMonsterDefinition(-1,GameEngine.instance().services.monsterRepository.getMonsterDefinitionForId(1).get(),1,w.getDatabaseID());
        MonsterBuilder mb = new MonsterBuilder(UUID.randomUUID(), dualShotGoblin, architectBot.getUsername(), Optional.of(architectBot.getUsername()));
        mb.setWeapon(w);
        initialArchitectObjects.add(mb.createMonster());
        setupMatch(true);
        List<GameObject> monsters = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(monsters.size() == 1);
        Monster monster = (Monster) monsters.get(0);

        ((PassBot)heroBot).passAllCharacters();

        forceSetCharacterLocation(monster.getGameObjectID(), new Location(1, 2), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 4));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(architectBot, monster.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.TOO_FEW_TARGETS, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackAtRange() {
        Weapon w = GameEngine.instance().services.heroItemRepository.getWeaponForId(4).get();
        initialArchitectObjects = new ArrayList<>();
        MonsterDefinition rangedGoblinDef = new WeaponModMonsterDefinition(-1,GameEngine.instance().services.monsterRepository.getMonsterDefinitionForId(1).get(),1, w.getDatabaseID());
        MonsterBuilder mb = new MonsterBuilder(UUID.randomUUID(), rangedGoblinDef, architectBot.getUsername(), Optional.of(architectBot.getUsername()));
        mb.setWeapon(w);
        initialArchitectObjects.add(mb.createMonster());
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        List<GameObject> a = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(a.size() == 1);
        Creature creature = (Creature) a.get(0);

        ((PassBot)heroBot).passAllCharacters();

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2), heroBot);
        forceSetCharacterLocation(creature.getGameObjectID(), new Location(1,5), architectBot);

        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1,2));
        basicAttackWithCharacter(architectBot, creature.getGameObjectID(), inputs, true);
    }

}
