package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.test.AutoStartInMatchTest;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by james on 3/15/16.
 */
public class PassAndTurnTesting extends AutoStartInMatchTest {

    @Override
    public List<Hero> getHeroesForHero(Player hero) {
        List<Hero> defaultHeroes = super.getHeroesForHero(hero);
        defaultHeroes.add(new HeroBuilder(hero.getUsername(), HeroType.WARRIOR).setHeroType(HeroType.MAGE).createHero());
        return defaultHeroes;
    }

    @Test
    public void testPassAction(){
        //Get a character for the hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroChars.size() > 0);
        Hero h = (Hero) heroChars.get(0);

        passCharacter(heroBot,h,true);
    }

    @Test
    public void testMultiplePassActions(){
        //Get a character for the hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroChars.size() > 0);
        Hero h = (Hero) heroChars.get(0);

        passCharacter(heroBot,h,true);
        InternalResponseObject<Boolean> resp = passCharacter(heroBot,h,false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NO_ACTION_POINTS,resp.getInternalErrorCode());
    }

    @Test
    public void testTurnEndingDueToPassActions(){
        //Get a character for the hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroChars.size() > 0);
        Hero h = (Hero) heroChars.get(0);
        heroChars.stream().filter(obj -> obj instanceof Creature).forEach(obj -> {
            assertTrue(passCharacter(heroBot, ((Creature) obj), false).isNormal());
        });
        InternalResponseObject<Boolean> resp = passCharacter(heroBot,h,false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NOT_YOUR_TURN,resp.getInternalErrorCode());

        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(architectChars.size() > 0);
        architectChars.stream().filter(obj -> obj instanceof Creature).forEach(obj -> {
            assertTrue(passCharacter(architectBot, (Creature) obj, false).isNormal());
        });
        Optional<GameObject> potentialCreature = architectChars.stream().filter(obj -> obj instanceof Creature).findFirst();
        if(potentialCreature.isPresent()){
            Creature creature = (Creature) potentialCreature.get();
            InternalResponseObject<Boolean> resp1 = passCharacter(architectBot, creature, false);
            assertFalse(resp1.isNormal());
            assertEquals(InternalErrorCode.NOT_YOUR_TURN, resp1.getInternalErrorCode());
        } else {
            fail("Unable to retrieve a creature to attempt passing again with.");
        }
    }

}
