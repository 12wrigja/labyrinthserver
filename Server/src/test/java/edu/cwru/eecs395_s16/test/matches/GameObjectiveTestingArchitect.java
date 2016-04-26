package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.core.GameState;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.core.objects.maps.MapTile;
import edu.cwru.eecs395_s16.core.objects.objectives.CaptureObjectivesGameObjective;
import edu.cwru.eecs395_s16.core.objects.objectives.DeathmatchGameObjective;
import edu.cwru.eecs395_s16.core.objects.objectives.ObjectiveGameObject;
import edu.cwru.eecs395_s16.networking.requests.GameActionBaseRequest;
import edu.cwru.eecs395_s16.networking.requests.gameactions.CaptureObjectiveActionData;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.services.bots.botimpls.PassBot;
import edu.cwru.eecs395_s16.test.InMatchTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by james on 3/26/16.
 */
public class GameObjectiveTestingArchitect extends InMatchTest {

    @Test
    public void testDeathmatchGameObjective() {
        initialObjective = new DeathmatchGameObjective();
        setupMatch(true);

        //Get a character for the hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertEquals(1, heroChars.size());
        Hero h = (Hero) heroChars.get(0);
        UUID heroID = h.getGameObjectID();

        List<Creature> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot).stream()
                .filter(obj -> obj instanceof Creature).map(obj -> (Creature) obj).collect(Collectors.toList());
        assertEquals(1, architectChars.size());
        Creature c = architectChars.get(0);
        //Figure out where the hero is and try and move it one tile away.
        Location l = c.getLocation();
        List<MapTile> maptiles = currentMatchState.getGameMap().getTileNeighbours(l).stream().filter(loc ->
                currentMatchState.getGameMap().getTile(loc).isPresent()).map(loc -> currentMatchState.getGameMap()
                .getTile(loc).get()).filter(tile -> !tile.isObstructionTileType()).collect(Collectors.toList());
        Location moveToLocation = maptiles.get(0);

        waitForMyTurn();

        forceSetCharacterLocation(heroID, moveToLocation, heroBot);

        List<Location> attackLocations = new ArrayList<>();
        attackLocations.add(moveToLocation);
        UUID creatureID = c.getGameObjectID();
        int attackCount = 0;
        while (h.getHealth() > 0) {
            attackCount++;
            System.out.println(attackCount);
            basicAttackWithCharacter(architectBot, creatureID, attackLocations, false);
            Optional<GameObject> opt = currentMatchState.getBoardObjects().getByID(heroID);
            if (opt.isPresent()) {
                h = (Hero) opt.get();
            } else {
                fail("Unable to re-retrieve the character we are attacking.");
            }
            while (!(currentMatchState.getGameState() == GameState.ARCHITECT_TURN || currentMatchState.getGameState()
                    == GameState.GAME_END)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateMatchState(architectBot);
            }
        }
        System.out.println("Well, we made it this far...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("WTF EARLY TERMINATION?");
        }
        System.out.println("And we also made it this far?");
        updateMatchState(architectBot);
        assertEquals(GameState.GAME_END, currentMatchState.getGameState());

    }

    @Test
    public void testCannotCaptureAsArchitect() {
        initialObjective = new CaptureObjectivesGameObjective(1);
        setupMatch(true);

        //Get a character for the hero
        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertEquals(1, architectChars.size());
        Monster monster = (Monster) architectChars.get(0);
        UUID monsterID = monster.getGameObjectID();

        List<ObjectiveGameObject> objectiveObjects = currentMatchState.getBoardObjects().stream().filter(obj -> obj
                instanceof ObjectiveGameObject).map(obj -> (ObjectiveGameObject) obj).collect(Collectors.toList());
        assertEquals(1, objectiveObjects.size());
        ObjectiveGameObject objective = objectiveObjects.get(0);
        //Figure out where the hero is and try and move it one tile away.
        Location l = objective.getLocation();
        List<MapTile> maptiles = currentMatchState.getGameMap().getTileNeighbours(l).stream().filter(loc ->
                currentMatchState.getGameMap().getTile(loc).isPresent()).map(loc -> currentMatchState.getGameMap()
                .getTile(loc).get()).filter(tile -> !tile.isObstructionTileType()).collect(Collectors.toList());
        Location moveToLocation = maptiles.get(0);

        waitForMyTurn();

        forceSetCharacterLocation(monster.getGameObjectID(), moveToLocation, architectBot);

        UUID objectiveID = objective.getGameObjectID();

        CaptureObjectiveActionData captureData = new CaptureObjectiveActionData(monsterID, objectiveID);
        GameActionBaseRequest gameActionRequest = new GameActionBaseRequest();
        try {
            gameActionRequest.fillFromJSON(captureData.convertToJSON());
        } catch (InvalidDataException e) {
            fail("Unable to convert game data.");
        }
        InternalResponseObject<Boolean> resp = game.gameAction(gameActionRequest, architectBot);
        if (resp.isNormal()) {
            fail("Something is wrong with the architect capturing mechanics.");
        }
        assertEquals(InternalErrorCode.INVALID_GAME_ACTION, resp.getInternalErrorCode());
    }

    @Override
    protected GameBot getHero() {
        return new PassBot();
    }

    private void waitForMyTurn() {
        do {
            updateMatchState(architectBot);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        } while (!currentMatchState.getGameState().equals(GameState.ARCHITECT_TURN));
    }

}
