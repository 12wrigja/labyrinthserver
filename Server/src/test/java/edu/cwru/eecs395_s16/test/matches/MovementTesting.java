package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.test.InMatchTest;
import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by james on 2/27/16.
 */
public class MovementTesting extends InMatchTest {

    @Test
    public void testMoveOneTile() throws JSONException {
        //Get a character for the hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroChars.size() > 0);
        Hero h = (Hero) heroChars.get(0);
        UUID heroID = h.getGameObjectID();

        //Figure out where the hero is and try and move it one tile away.
        Location l  = h.getLocation();
        Location t = currentMatchState.getGameMap().getTileNeighbours(l).get(0);

        //Build path and submit move.
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(t);
        moveCharacter(heroBot,heroID,pathToMove,true);
    }

    @Test
    public void testForceSetCharacterLocation() throws JSONException {
        //Get character for hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroChars.size() > 0);
        Hero h = (Hero) heroChars.get(0);
        UUID heroID = h.getGameObjectID();

        forceSetCharacterLocation(heroID,new Location(0,1));

    }

    @Test
    public void testMoveLongPath() throws JSONException {
        //Get character for hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroChars.size() > 0);
        Hero h = (Hero) heroChars.get(0);
        UUID heroID = h.getGameObjectID();

        forceSetCharacterLocation(heroID,new Location(0,1));

        //Get hero location and try and move them their max distance (default units can move 3 units).
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(new Location(0,2));
        pathToMove.add(new Location(0,3));
        pathToMove.add(new Location(0,4));
        moveCharacter(heroBot,heroID,pathToMove,true);
    }

    @Test
    public void testMoveInvalidTile() throws JSONException {
        //Get character for hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroChars.size() > 0);
        Hero h = (Hero) heroChars.get(0);
        UUID heroID = h.getGameObjectID();

        forceSetCharacterLocation(heroID,new Location(2,3));

        //Get hero location and try and move them really far away
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(new Location(3,3));
        pathToMove.add(new Location(4,3));
        pathToMove.add(new Location(5,3));
        InternalResponseObject<Boolean> response = moveCharacter(heroBot, heroID, pathToMove, false);
        assertFalse(response.isNormal());
        assertEquals(WebStatusCode.UNPROCESSABLE_DATA,response.getStatus());
        assertEquals(InternalErrorCode.INVALID_GAME_ACTION, response.getInternalErrorCode());
    }

    @Test
    public void testMoveTooFar() throws JSONException {
        //Get character for hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroChars.size() > 0);
        Hero h = (Hero) heroChars.get(0);
        UUID heroID = h.getGameObjectID();

        forceSetCharacterLocation(heroID,new Location(2,3));

        //Get hero location and try and move them really far away
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(new Location(3,3));
        pathToMove.add(new Location(3,4));
        pathToMove.add(new Location(3,5));
        pathToMove.add(new Location(3,6));
        InternalResponseObject<Boolean> response = moveCharacter(heroBot, heroID, pathToMove, false);
        assertFalse(response.isNormal());
        assertEquals(WebStatusCode.UNPROCESSABLE_DATA,response.getStatus());
        assertEquals(InternalErrorCode.INVALID_GAME_ACTION, response.getInternalErrorCode());
    }

    @Test
    public void testMoveWhenExhausted() throws JSONException {
        //Get character for hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroChars.size() > 0);
        Hero h = (Hero) heroChars.get(0);
        UUID heroID = h.getGameObjectID();

        forceSetCharacterLocation(heroID,new Location(2,3));

        //Move the hero 3 times and make sure the last one fails
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(new Location(3,3));
        moveCharacter(heroBot, heroID, pathToMove, true);

        //Move the hero 3 times and make sure the last one fails
        pathToMove = new ArrayList<>();
        pathToMove.add(new Location(3,4));
        moveCharacter(heroBot, heroID, pathToMove, true);

        //Move the hero 3 times and make sure the last one fails
        pathToMove = new ArrayList<>();
        pathToMove.add(new Location(3,5));
        InternalResponseObject<Boolean> resp = moveCharacter(heroBot, heroID, pathToMove, false);
        assertFalse(resp.isNormal());
        assertEquals(WebStatusCode.UNPROCESSABLE_DATA,resp.getStatus());
        assertEquals(InternalErrorCode.INVALID_GAME_ACTION, resp.getInternalErrorCode());
    }

    //TODO write tests for triggering traps
    //Maybe those belong in a different test? (Probably)

}
