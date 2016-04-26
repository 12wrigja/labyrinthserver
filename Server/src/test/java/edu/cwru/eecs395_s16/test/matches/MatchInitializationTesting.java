package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterDefinition;
import edu.cwru.eecs395_s16.test.InMatchTest;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by james on 4/13/16.
 */
public class MatchInitializationTesting extends InMatchTest {

    @Test
    public void testNormalMatchInitialization() {
        Set<UUID> heroUUIDs = heroBot.getBotsHeroes().stream().map(GameObject::getGameObjectID).collect(Collectors
                .toSet());
        InternalResponseObject<Match> m = setupMatch(false);
        if (!m.isNormal()) {
            fail("Something went wrong when initializing a match. ERROR: " + m.getMessage());
        }
        updateMatchState(heroBot);
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        List<Hero> heroes = heroChars.stream().filter(obj -> obj instanceof Hero).map(obj -> (Hero) obj).collect
                (Collectors.toList());
        assertEquals(heroUUIDs.size(), heroes.size());
        for (Hero hero : heroes) {
            assertTrue(heroUUIDs.contains(hero.getGameObjectID()));
        }
    }

    @Test
    public void testInitializationWhenSpecifyingHeroes() {
        useHeroIDs = heroBot.getBotsHeroes().stream().map(GameObject::getGameObjectID).collect(Collectors.toSet());
        InternalResponseObject<Match> m = setupMatch(false);
        if (!m.isNormal()) {
            fail("Something went wrong when initializing a match while specifying hero ids. ERROR: " + m.getMessage());
        }
        updateMatchState(heroBot);
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        List<Hero> heroes = heroChars.stream().filter(obj -> obj instanceof Hero).map(obj -> (Hero) obj).collect
                (Collectors.toList());
        assertEquals(useHeroIDs.size(), heroes.size());
        for (Hero hero : heroes) {
            assertTrue(useHeroIDs.contains(hero.getGameObjectID()));
        }
    }

    @Test
    public void testInvalidHeroIdentifiers() {
        useHeroIDs = new HashSet<>();
        UUID randID = UUID.randomUUID();
        List<Hero> heroes = heroBot.getBotsHeroes();
        for (Hero h : heroes) {
            assertNotEquals(h.getGameObjectID(), randID);
        }
        useHeroIDs.add(randID);
        InternalResponseObject<Match> m = setupMatch(false);
        if (m.isNormal()) {
            fail("Setting up a match worked with invalid hero id's.");
        }
        assertEquals(InternalErrorCode.INCORRECT_INITIAL_HERO_SETUP, m.getInternalErrorCode());
    }

    @Test
    public void testTooManyHeroIdentifiers() {
        int numHeroSpaces = initialGameMap.getHeroCapacity();
        useHeroIDs = new HashSet<>();
        List<Hero> heroes = heroBot.getBotsHeroes();
        for (int i = 0; i <= numHeroSpaces; i++) {
            UUID randID = UUID.randomUUID();
            assertFalse(useHeroIDs.contains(randID));
            for (Hero h : heroes) {
                assertNotEquals(h.getGameObjectID(), randID);
            }
            useHeroIDs.add(randID);
        }
        assertEquals(numHeroSpaces + 1, useHeroIDs.size());
        InternalResponseObject<Match> m = setupMatch(false);
        if (m.isNormal()) {
            fail("Setting up a match succeeded when we used too many hero ids.");
        }
        assertEquals(InternalErrorCode.INCORRECT_INITIAL_HERO_SETUP, m.getInternalErrorCode());
    }

    @Test
    public void testNoHeroIdentifiers() {
        useHeroIDs = new HashSet<>();
        InternalResponseObject<Match> m = setupMatch(false);
        if (m.isNormal()) {
            fail("Setting up a match succeeded when we used no hero ids.");
        }
        assertEquals(InternalErrorCode.INCORRECT_INITIAL_HERO_SETUP, m.getInternalErrorCode());
    }

    @Test
    public void testInitializationMonstersRandom() {
        InternalResponseObject<Match> m = setupMatch(false);
        if (!m.isNormal()) {
            fail("Unable to setup a match when spawning monsters randomly.");
        }
        Map<Integer, Integer> idToCountMap = architectBot.getArchitectObjects().stream().filter(obj -> obj instanceof
                Monster).map(obj -> (Monster) obj).collect(Collectors.groupingBy(Creature::getDatabaseID)).entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

        updateMatchState(architectBot);
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        List<Monster> monsters = architectChars.stream().filter(obj -> obj instanceof Monster).map(obj -> (Monster)
                obj).collect(Collectors.toList());

        Map<Integer, Integer> actualCountMap = new HashMap<>();
        for (Monster monster : monsters) {
            actualCountMap.put(monster.getDatabaseID(), actualCountMap.containsKey(monster.getDatabaseID()) ?
                    actualCountMap.get(monster.getDatabaseID()) + 1 : 1);
        }
        for (Map.Entry<Integer, Integer> actualIdCountEntry : actualCountMap.entrySet()) {
            assertTrue(idToCountMap.containsKey(actualIdCountEntry.getKey()));
            int maxCount = idToCountMap.get(actualIdCountEntry.getKey());
            int usedCount = actualIdCountEntry.getValue();
            assertTrue(maxCount >= usedCount);
        }
    }

    @Test
    public void testInitializationWhenPlacingMonster() {
        useMonsterIDs = new HashMap<>();
        List<Monster> archMonsters = architectBot.getArchitectObjects().stream().filter(obj -> obj instanceof
                Monster).map(obj -> (Monster) obj).collect(Collectors.toList());
        assertEquals(1, archMonsters.size());
        Monster definedMonster = archMonsters.get(0);
        Location spawnLoc = initialGameMap.getArchitectCreatureSpawnLocations().get(0);
        useMonsterIDs.put(spawnLoc, definedMonster.getDatabaseID());
        InternalResponseObject<Match> m = setupMatch(false);
        if (!m.isNormal()) {
            fail("Unable to setup a match when spawning monsters randomly.");
        }

        updateMatchState(architectBot);
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        List<Monster> monsters = architectChars.stream().filter(obj -> obj instanceof Monster).map(obj -> (Monster)
                obj).collect(Collectors.toList());
        assertEquals(1, monsters.size());

        Monster actualMonster = monsters.get(0);

        assertEquals(definedMonster.getDatabaseID(), actualMonster.getDatabaseID());
        assertEquals(spawnLoc, actualMonster.getLocation());

    }

    @Test
    public void testInitializationWhenPlacingMonsters() {
        Weapon w = GameEngine.instance().services.heroItemRepository.getWeaponForId(4).get();
        initialArchitectObjects = new ArrayList<>();
        MonsterDefinition rangedGoblinDef = new WeaponModMonsterDefinition(-1, GameEngine.instance().services
                .monsterRepository.getMonsterDefinitionForId(1).get(), 1, w.getDatabaseID());
        MonsterBuilder mb = new MonsterBuilder(UUID.randomUUID(), rangedGoblinDef, architectBot.getUsername(),
                Optional.of(architectBot.getUsername()));
        mb.setWeapon(w);
        initialArchitectObjects.add(mb.createMonster());
        mb = new MonsterBuilder(UUID.randomUUID(), rangedGoblinDef, architectBot.getUsername(), Optional.of
                (architectBot.getUsername()));
        mb.setWeapon(w);
        initialArchitectObjects.add(mb.createMonster());
        useMonsterIDs = new HashMap<>();
        int spawnLocIndex = 0;
        Map<Location, Integer> locMap = new HashMap<>();
        for (GameObject m : initialArchitectObjects) {
            Monster monster = (Monster) m;
            Location spawnLoc = initialGameMap.getArchitectCreatureSpawnLocations().get(spawnLocIndex);
            useMonsterIDs.put(spawnLoc, monster.getDatabaseID());
            locMap.put(spawnLoc, monster.getDatabaseID());
            spawnLocIndex++;
        }
        InternalResponseObject<Match> match = setupMatch(false);
        if (!match.isNormal()) {
            fail("Unable to setup a match when spawning monsters randomly.");
        }

        updateMatchState(architectBot);
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        List<Monster> monsters = architectChars.stream().filter(obj -> obj instanceof Monster).map(obj -> (Monster)
                obj).collect(Collectors.toList());
        assertEquals(2, monsters.size());

        Map<Location, Integer> actualLocMap = new HashMap<>();
        for (Monster m : monsters) {
            actualLocMap.put(m.getLocation(), m.getDatabaseID());
        }
        assertEquals(locMap, actualLocMap);
    }

    @Test
    public void testNoMonstersSpecified() {
        useMonsterIDs = new HashMap<>();
        InternalResponseObject<Match> match = setupMatch(false);
        if (match.isNormal()) {
            fail("Should not be able to start a match when there are no monsters specified.");
        }
        assertEquals(InternalErrorCode.INCORRECT_INITIAL_ARCHITECT_SETUP, match.getInternalErrorCode());
    }

    @Test
    public void testTooManyOfAMonsterSpecified() {
        InternalResponseObject<List<MonsterDefinition>> archDefs = GameEngine.instance().services.monsterRepository
                .getPlayerMonsterTypes(architectBot);
        if (!archDefs.isNormal()) {
            fail("Unable to retrieve monster definitions for architect.");
        }
        assertEquals(1, archDefs.get().size());

        int definitionID = archDefs.get().get(0).id;
        int numSpawnLocations = initialGameMap.getArchitectCreatureSpawnLocations().size();
        assertTrue(archDefs.get().get(0).count < numSpawnLocations);
        useMonsterIDs = new HashMap<>();
        for (Location l : initialGameMap.getArchitectCreatureSpawnLocations()) {
            useMonsterIDs.put(l, definitionID);
        }
        InternalResponseObject<Match> match = setupMatch(false);
        if (match.isNormal()) {
            fail("Should not be able to start a match when you don't have enough of that creature.");
        }
        assertEquals(InternalErrorCode.INCORRECT_INITIAL_ARCHITECT_SETUP, match.getInternalErrorCode());
    }

    @Test
    public void testNotOwnedMonsterSpecified() {
        InternalResponseObject<List<MonsterDefinition>> archDefs = GameEngine.instance().services.monsterRepository
                .getPlayerMonsterTypes(architectBot);
        if (!archDefs.isNormal()) {
            fail("Unable to retrieve monster definitions for architect.");
        }
        assertEquals(1, archDefs.get().size());
        int notOwnedDefinitionID = 0;
        Optional<MonsterDefinition> monsterDef = archDefs.get().stream().filter(def -> def.id ==
                notOwnedDefinitionID).findFirst();
        if (monsterDef.isPresent() && monsterDef.get().count > 0) {
            fail("The architect owns monsters with id " + notOwnedDefinitionID);
        }
        useMonsterIDs = new HashMap<>();
        useMonsterIDs.put(initialGameMap.getArchitectCreatureSpawnLocations().get(0), notOwnedDefinitionID);
        InternalResponseObject<Match> match = setupMatch(false);
        if (match.isNormal()) {
            fail("Should not be able to start a match when you own any of that creature.");
        }
        assertEquals(InternalErrorCode.INCORRECT_INITIAL_ARCHITECT_SETUP, match.getInternalErrorCode());
    }

    @Test
    public void testInvalidLocationSpecified() {
        int mapX = initialGameMap.getSizeX();
        int mapY = initialGameMap.getSizeY();
        Random r = new Random();
        Location invalidLoc;
        do {
            invalidLoc = new Location(r.nextInt(mapX), r.nextInt(mapY));
        } while (initialGameMap.getArchitectCreatureSpawnLocations().contains(invalidLoc));

        useMonsterIDs = new HashMap<>();

        Optional<Monster> archMonster = architectBot.getArchitectObjects().stream().filter(obj -> obj instanceof
                Monster).map(obj -> (Monster) obj).findAny();
        if (!archMonster.isPresent()) {
            fail("The architect bot doesn't have any monsters.");
        }
        useMonsterIDs.put(invalidLoc, archMonster.get().getDatabaseID());
        InternalResponseObject<Match> match = setupMatch(false);
        if (match.isNormal()) {
            fail("Should not be able to start a match when you are trying to position a monster in an invalid spawn location.");
        }
        assertEquals(InternalErrorCode.INCORRECT_INITIAL_ARCHITECT_SETUP, match.getInternalErrorCode());
    }


}
