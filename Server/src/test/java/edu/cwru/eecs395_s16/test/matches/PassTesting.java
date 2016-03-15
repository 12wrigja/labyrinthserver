package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.test.InMatchTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by james on 3/15/16.
 */
public class PassTesting extends InMatchTest {

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
        passCharacter(heroBot,h,true);
    }
}
