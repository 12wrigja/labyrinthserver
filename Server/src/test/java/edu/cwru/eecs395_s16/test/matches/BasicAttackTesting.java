package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.UsePattern;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.test.InMatchTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by james on 3/23/16.
 */
public class BasicAttackTesting extends InMatchTest {

    @Test
    public void testBasicAttacking() {
        setupMatch();
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        List<GameObject> a = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(h.size() == 1);
        Creature creature = (Creature) a.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(0, 1));
        forceSetCharacterLocation(creature.getGameObjectID(), new Location(0, 2));
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(0,2));
        basicAttackWithCharacter(heroBot, hero, inputs, true);
    }

    @Test
    public void testAttackingOutOfRange() {
        setupMatch();
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        List<GameObject> a = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(h.size() == 1);
        Creature creature = (Creature) a.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(0, 1));
        forceSetCharacterLocation(creature.getGameObjectID(), new Location(0, 3));
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(0, 5));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero, inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NOT_IN_RANGE, resp.getInternalErrorCode());
    }


    @Test
    public void testAttackingFriendlyCharacter() {
        initialHeroes = new ArrayList<>();
        initialHeroes.add(new HeroBuilder(heroBot.getUsername()).createHero());
        initialHeroes.add(new HeroBuilder(heroBot.getUsername()).createHero());
        setupMatch();

        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() >= 2);
        Hero hero1 = (Hero) h.get(0);
        Hero hero2 = (Hero) h.get(1);

        forceSetCharacterLocation(hero1.getGameObjectID(), new Location(0, 1));
        forceSetCharacterLocation(hero2.getGameObjectID(), new Location(0, 2));
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(0,2));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero1, inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.FRIENDLY_FIRE, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWall() {
        setupMatch();
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(3, 2));
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(4, 2));
        basicAttackWithCharacter(heroBot, hero, inputs, true);
    }

    @Test
    public void testAttackEmptyTile() {
        setupMatch();
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2));
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 3));
        basicAttackWithCharacter(heroBot, hero, inputs, true);
    }

    @Test
    public void testAttackSelf() {
        setupMatch();
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2));
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1,2));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero, inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.FRIENDLY_FIRE, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackNonexistantTile() {
        setupMatch();
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(0, 1));
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(-1, 1));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero, inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.INVALID_LOCATION, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWithEnemyUnit() {
        setupMatch();
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2));
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 3));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero, inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NOT_CONTROLLER, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWithTooManyInputs() {
        setupMatch();
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2));
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 4));
        inputs.add(new Location(1,3));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero, inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.TOO_MANY_TARGETS, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackWithTooFewInputs() {
        Weapon w = GameEngine.instance().services.heroItemRepository.getWeaponForId(4).get();
        initialHeroes = new ArrayList<>();
        initialHeroes.add(new HeroBuilder(heroBot.getUsername()).setWeapon(w).createHero());
        setupMatch();
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2));
        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1, 4));
        InternalResponseObject<Boolean> resp = basicAttackWithCharacter(heroBot, hero, inputs, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.TOO_FEW_TARGETS, resp.getInternalErrorCode());
    }

    @Test
    public void testAttackAtRange() {
        Weapon w = GameEngine.instance().services.heroItemRepository.getWeaponForId(2).get();
        initialHeroes = new ArrayList<>();
        initialHeroes.add(new HeroBuilder(heroBot.getUsername()).setWeapon(w).createHero());
        setupMatch();
        List<GameObject> h = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(h.size() == 1);
        Hero hero = (Hero) h.get(0);

        List<GameObject> a = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(h.size() == 1);
        Creature creature = (Creature) a.get(0);

        forceSetCharacterLocation(hero.getGameObjectID(), new Location(1, 2));
        forceSetCharacterLocation(creature.getGameObjectID(), new Location(1,5));

        List<Location> inputs = new ArrayList<>();
        inputs.add(new Location(1,5));
        basicAttackWithCharacter(heroBot, hero, inputs, true);
    }
}
