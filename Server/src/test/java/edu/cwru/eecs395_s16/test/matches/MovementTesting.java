package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.test.InMatchTest;
import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by james on 2/27/16.
 */
public class MovementTesting extends InMatchTest {
//
//    @Test
//    public void testMoveOneTile() throws JSONException {
//        //Get a character for the hero
//        List<GameObject> heroChars = boardObjects.getForPlayerOwner(heroBot);
//        assertTrue(heroChars.size() > 0);
//        Hero h = (Hero) heroChars.get(0);
//        UUID heroID = h.getGameObjectID();
//
//        //Figure out where the hero is and try and move it one tile away.
//        Location l  = h.getLocation();
//        Location t = gameMap.getTileNeighbours(l).get(0);
//
//        //Build path and submit move.
//        List<Location> pathToMove = new ArrayList<>();
//        pathToMove.add(t);
//        moveCharacter(heroBot,heroID,pathToMove,true);
//    }
//
//    @Test
//    public void testForceSetCharacterLocation() throws JSONException {
//        //Get character for hero
//        List<GameObject> heroChars = boardObjects.getForPlayerOwner(heroBot);
//        assertTrue(heroChars.size() > 0);
//        Hero h = (Hero) heroChars.get(0);
//        UUID heroID = h.getGameObjectID();
//
//        forceSetCharacterLocation(heroBot,heroID,new Location(0,1));
//
//    }
//
//    @Test
//    public void testMoveLongPath() throws JSONException {
//        //Get character for hero
//        List<GameObject> heroChars = boardObjects.getForPlayerOwner(heroBot);
//        assertTrue(heroChars.size() > 0);
//        Hero h = (Hero) heroChars.get(0);
//        UUID heroID = h.getGameObjectID();
//
//        forceSetCharacterLocation(heroBot,heroID,new Location(0,1));
//
//        //Get hero location and try and move them really far away
//        List<Location> pathToMove = new ArrayList<>();
//        pathToMove.add(new Location(0,2));
//        pathToMove.add(new Location(0,3));
//        pathToMove.add(new Location(0,4));
//        moveCharacter(heroBot,heroID,pathToMove,true);
//    }
//
//    @Test
//    public void testMoveInvalidTile() throws JSONException {
//        //Get character for hero
//        List<GameObject> heroChars = boardObjects.getForPlayerOwner(heroBot);
//        assertTrue(heroChars.size() > 0);
//        Hero h = (Hero) heroChars.get(0);
//        UUID heroID = h.getGameObjectID();
//
//        forceSetCharacterLocation(heroBot,heroID,new Location(0,1));
//
//        //Get hero location and try and move them really far away
//        List<Location> pathToMove = new ArrayList<>();
//        pathToMove.add(new Location(0,2));
//        pathToMove.add(new Location(1,2));
//        pathToMove.add(new Location(2,2));
//        pathToMove.add(new Location(3,2));
//        pathToMove.add(new Location(4,2));
//        pathToMove.add(new Location(5,2));
//        Location newLocation = moveCharacter(heroBot, heroID, pathToMove, false);
//        assertEquals(new Location(0,1),newLocation);
//
//    }

}
