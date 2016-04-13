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
import edu.cwru.eecs395_s16.test.InMatchTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by james on 3/23/16.
 */
public class BasicAttackTestingHeroes extends InMatchTest {

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

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(0, 1), heroBot);
        forceSetCharacterLocation(creature.getGameObjectID(), new Location(0, 2), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(0,2));
        basicAttackWithCharacter(heroBot, hero.getGameObjectID(), inputs, true);
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

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(0, 1),heroBot );
        forceSetCharacterLocation(creature.getGameObjectID(), new Location(0, 3), architectBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(0, 5));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NOT_IN_RANGE, resp.getInternalErrorCode());
    }


    @Test
    public void testAttackingFriendlyCharacter() {
        initialHeroes = new ArrayList<>();
        initialHeroes.add(new HeroBuilder(heroBot.getUsername(), HeroType.WARRIOR).createHero());
        initialHeroes.add(new HeroBuilder(heroBot.getUsername(), HeroType.WARRIOR).createHero());
        setupMatch(true);

        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() >= 2);
        Hero hero1 = (Hero) h.get(0);
        Hero hero2 = (Hero) h.get(1);

        forceSetCharacterLocation(hero1.getGameObjectID(), new Location(0, 1), heroBot);
        forceSetCharacterLocation(hero2.getGameObjectID(), new Location(0, 2), heroBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(0,2));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero1.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.FRIENDLY_FIRE, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWall() {
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(3, 2), heroBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(4, 2));
        basicAttackWithCharacter(heroBot, hero.getGameObjectID(), inputs, true);
    }

    @Test
    public void testAttackEmptyTile() {
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2), heroBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 3));
        basicAttackWithCharacter(heroBot, hero.getGameObjectID(), inputs, true);
    }

    @Test
    public void testAttackSelf() {
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2), heroBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1,2));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.FRIENDLY_FIRE, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackNonexistantTile() {
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(0, 1), heroBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(-1, 1));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.INVALID_LOCATION, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWithEnemyUnit() {
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(h.size() == 1);
        Creature hero = (Creature) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2), heroBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 3));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NOT_CONTROLLER, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWithTooManyInputs() {
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2), heroBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 4));
        inputs.add(new Location(1,3));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.TOO_MANY_TARGETS, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWithTooFewInputs() {
        Weapon w = GameEngine.instance().services.heroItemRepository.getWeaponForId(6).get();
        initialHeroes = new ArrayList<>();
        initialHeroes.add(new HeroBuilder(heroBot.getUsername(), HeroType.WARRIOR).setWeapon(w).createHero());
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2), heroBot);
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 4));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero.getGameObjectID(), inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.TOO_FEW_TARGETS, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackAtRange() {
        Weapon w = GameEngine.instance().services.heroItemRepository.getWeaponForId(4).get();
        initialHeroes = new ArrayList<>();
        initialHeroes.add(new HeroBuilder(heroBot.getUsername(), HeroType.WARRIOR).setWeapon(w).createHero());
        setupMatch(true);
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        List<GameObject> a = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(a.size() == 1);
        Creature creature = (Creature) a.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2), heroBot);
        forceSetCharacterLocation(creature.getGameObjectID(), new Location(1,5), architectBot);

        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1,5));
        basicAttackWithCharacter(heroBot, hero.getGameObjectID(), inputs, true);
    }
}
