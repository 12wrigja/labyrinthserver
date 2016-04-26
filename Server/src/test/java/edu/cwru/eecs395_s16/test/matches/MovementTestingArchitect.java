package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.GameState;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterDefinition;
import edu.cwru.eecs395_s16.core.objects.maps.MapTile;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.services.bots.botimpls.PassBot;
import edu.cwru.eecs395_s16.test.AutoStartInMatchTest;
import org.json.JSONException;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by james on 2/27/16.
 */
public class MovementTestingArchitect extends AutoStartInMatchTest {

    public MonsterDefinition goblinDef;

    @Test
    public void testMoveOneTile() throws JSONException {
        //Get a character for the hero
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(architectChars.size() > 0);
        Monster m = (Monster) architectChars.get(0);
        UUID monsterID = m.getGameObjectID();

        //Figure out where the hero is and try and move it one tile away.
        Location l = m.getLocation();
        Optional<MapTile> t = currentMatchState.getGameMap().getTileNeighbours(l).stream().map(v -> currentMatchState
                .getGameMap().getTile(v).get()).filter(tile -> !tile.getTileType().isObstruction && tile
                .isNeighbourOf(l, false) && currentMatchState.getBoardObjects().getForLocation(tile).size() == 0)
                .findFirst();
        Location moveLoc;
        if (t.isPresent()) {
            moveLoc = t.get();
        } else {
            fail("Unable to get a valid tile to move to. Check board config.");
            return;
        }
        //Build path and submit move.
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(moveLoc);
        waitForMyTurn();
        moveCharacter(architectBot, monsterID, pathToMove, true);
    }

    @Test
    public void testForceSetCharacterLocation() throws JSONException {
        //Get character for hero
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(architectChars.size() > 0);
        Monster m = (Monster) architectChars.get(0);
        UUID monsterID = m.getGameObjectID();
        waitForMyTurn();
        forceSetCharacterLocation(monsterID, new Location(0, 1), architectBot);
    }

    @Test
    public void testMoveLongPath() throws JSONException {
        //Get character for hero
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(architectChars.size() > 0);
        Monster m = (Monster) architectChars.get(0);
        UUID monsterID = m.getGameObjectID();

        waitForMyTurn();
        forceSetCharacterLocation(monsterID, new Location(0, 1), architectBot);

        //Get hero location and try and move them their max distance (default units can move 3 units).
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(new Location(0, 2));
        pathToMove.add(new Location(0, 3));
        moveCharacter(architectBot, monsterID, pathToMove, true);
    }

    @Test
    public void testMoveInvalidTile() throws JSONException {
        //Get character for hero
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(architectChars.size() > 0);
        Monster m = (Monster) architectChars.get(0);
        UUID monsterID = m.getGameObjectID();

        waitForMyTurn();
        forceSetCharacterLocation(monsterID, new Location(3, 3), architectBot);

        //Get hero location and try and move them really far away
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(new Location(4, 3));
        pathToMove.add(new Location(5, 3));
        InternalResponseObject<Boolean> response = moveCharacter(architectBot, monsterID, pathToMove, false);
        assertFalse(response.isNormal());
        assertEquals(WebStatusCode.UNPROCESSABLE_DATA, response.getStatus());
        assertEquals(InternalErrorCode.PATH_OBSTRUCTED, response.getInternalErrorCode());
    }

    @Test
    public void testMoveTooFar() throws JSONException {
        //Get character for hero
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(architectChars.size() > 0);
        Monster m = (Monster) architectChars.get(0);
        UUID monsterID = m.getGameObjectID();

        waitForMyTurn();

        forceSetCharacterLocation(monsterID, new Location(2, 3), architectBot);

        //Get hero location and try and move them really far away
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(new Location(3, 3));
        pathToMove.add(new Location(3, 4));
        pathToMove.add(new Location(3, 5));
        pathToMove.add(new Location(3, 6));
        InternalResponseObject<Boolean> response = moveCharacter(architectBot, monsterID, pathToMove, false);
        assertFalse(response.isNormal());
        assertEquals(WebStatusCode.UNPROCESSABLE_DATA, response.getStatus());
        assertEquals(InternalErrorCode.PATH_TOO_LONG, response.getInternalErrorCode());
    }

    @Test
    public void testMoveWhenExhausted() throws JSONException {
        //Get character for hero
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(architectChars.size() >= 2);
        Monster h = (Monster) architectChars.get(0);
        UUID monsterID = h.getGameObjectID();

        waitForMyTurn();

        forceSetCharacterLocation(monsterID, new Location(2, 3), architectBot);

        //Move the hero 3 times and make sure the last one fails
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(new Location(3, 3));
        moveCharacter(architectBot, monsterID, pathToMove, true);

        //Move the hero 3 times and make sure the last one fails
        pathToMove = new ArrayList<>();
        pathToMove.add(new Location(3, 4));
        moveCharacter(architectBot, monsterID, pathToMove, true);

        //Move the hero 3 times and make sure the last one fails
        pathToMove = new ArrayList<>();
        pathToMove.add(new Location(3, 5));
        InternalResponseObject<Boolean> resp = moveCharacter(architectBot, monsterID, pathToMove, false);
        assertFalse(resp.isNormal());
        assertEquals(WebStatusCode.UNPROCESSABLE_DATA, resp.getStatus());
        assertEquals(InternalErrorCode.NO_ACTION_POINTS, resp.getInternalErrorCode());
    }

    @Test
    public void testMoveOntoObject() throws JSONException {
        //Get character for hero
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(architectChars.size() > 0);
        Monster h = (Monster) architectChars.get(0);
        UUID monsterID = h.getGameObjectID();

        //Try and move to the location of an existing character.
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(new Location(0, 0));
        waitForMyTurn();
        InternalResponseObject<Boolean> resp = moveCharacter(architectBot, monsterID, pathToMove, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.PATH_OBSTRUCTED, resp.getInternalErrorCode());
    }

    @Test
    public void testMoveEnemyUnits() throws JSONException {
        //Get character for hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertTrue(heroChars.size() > 0);
        Hero h = (Hero) heroChars.get(0);
        UUID heroID = h.getGameObjectID();

        //Try and move to the location of an existing character.
        List<Location> pathToMove = new ArrayList<>();
        pathToMove.add(new Location(0, 0));
        waitForMyTurn();
        InternalResponseObject<Boolean> resp = moveCharacter(architectBot, heroID, pathToMove, false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NOT_CONTROLLER, resp.getInternalErrorCode());
    }

    @Override
    protected GameBot getHero() {
        return new PassBot();
    }

    @Override
    protected Map<Location, Integer> placeArchitectMonsters() {
        Map<Location, Integer> ret = new HashMap<>();
        List<Location> archSpawnTiles = initialGameMap.getArchitectCreatureSpawnLocations();
        Collections.shuffle(archSpawnTiles);
        int placedMonsters = 0;
        for (GameObject obj : architectBot.getArchitectObjects()) {
            if (obj instanceof Monster) {
                ret.put(archSpawnTiles.get(placedMonsters), ((Monster) obj).getDatabaseID());
                placedMonsters++;
            }
        }
        return ret;
    }

    @Override
    protected void changeArchitectList(Player architect, List<GameObject> architectObjects) {
        goblinDef = GameEngine.instance().services.monsterRepository.getMonsterDefinitionForId(1).get();
        architectObjects.add(new MonsterBuilder(UUID.randomUUID(), goblinDef, architect.getUsername(), Optional.of
                (architect.getUsername())).createMonster());
    }

    //TODO write tests for triggering traps
    //Maybe those belong in a different test? (Probably)

    private void waitForMyTurn() {
        do {
            updateMatchState(heroBot);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        } while (!currentMatchState.getGameState().equals(GameState.ARCHITECT_TURN));
    }

}
