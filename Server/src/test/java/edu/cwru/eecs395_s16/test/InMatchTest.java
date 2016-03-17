package edu.cwru.eecs395_s16.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.bots.PassBot;
import edu.cwru.eecs395_s16.bots.TestBot;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.interfaces.objects.Creature;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.interfaces.repositories.HeroRepository;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;
import edu.cwru.eecs395_s16.networking.requests.GameActionBaseRequest;
import edu.cwru.eecs395_s16.networking.requests.NoInputRequest;
import edu.cwru.eecs395_s16.networking.requests.gameactions.MoveGameActionData;
import edu.cwru.eecs395_s16.networking.requests.gameactions.PassGameActionData;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by james on 2/27/16.
 */
public abstract class InMatchTest extends SerializationTest {

    protected GameBot heroBot;
    protected GameBot architectBot;
    protected Match currentMatchState;
    private NetworkingInterface game;

    @Override
    public void setup() throws Exception {
        super.setup();
        setupMatch();
    }

    @Override
    public void teardown() throws Exception {
        teardownMatch();
        super.teardown();
    }

    public void teardownMatch() throws Exception {
        game.leaveMatch(new NoInputRequest(), heroBot);

        heroBot.disconnect();
        architectBot.disconnect();
    }

    public List<Hero> getHeroesForHero(Player hero){
        Hero h = new HeroBuilder().setOwnerID(Optional.of(hero.getUsername())).createHero();
        List<Hero> heroes = new ArrayList<>();
        heroes.add(h);
        return heroes;
    }

    public List<GameObject> getObjectsForArchitect(Player architect){
        Hero hero = new HeroBuilder().setOwnerID(Optional.of(architect.getUsername())).createHero();
        List<GameObject> gameObjects = new ArrayList<>();
        gameObjects.add(hero);
        return gameObjects;
    }

    public void setupMatch() throws Exception {
        heroBot = new TestBot();
        heroBot.replaceBotHeroes(getHeroesForHero(heroBot));
        architectBot = new TestBot();
        architectBot.replaceArchitectObjects(getObjectsForArchitect(architectBot));
        GameMap gameMap = new AlmostBlankMap(10, 10);

        InternalResponseObject<Match> matchOpt = Match.InitNewMatch(heroBot, architectBot, gameMap);
        if (matchOpt.isNormal()) {
            game = engine.networkingInterface;
            updateMatchState();
        } else {
            fail("Unable to setup a match. ERROR: " + matchOpt.getInternalErrorCode().message);
        }
    }

    public void updateMatchState() {
        InternalResponseObject<Match> r = game.matchState(new NoInputRequest(), heroBot);
        if (!r.isNormal()) {
            fail("Unable to get current match state. ERROR: " + r.getMessage());
        } else {
            this.currentMatchState = r.get();
        }
    }

    public void forceSetCharacterLocation(UUID characterID, Location loc) {
        updateMatchState();
        Optional<GameObject> objOpt = currentMatchState.getBoardObjects().getByID(characterID);
        if(objOpt.isPresent()){
            objOpt.get().setLocation(loc);
        } else {
            //The object is not on the board. Return
            return;
        }
        try {
            JSONObject snapshot = new JSONObject(objMapper.writeValueAsString(currentMatchState.getJSONRepresentation()));
            currentMatchState.takeAndCommitSnapshot(snapshot, "Forced snapshot for movement.");
            updateMatchState();
            Location newLoc = currentMatchState.getBoardObjects().getByID(characterID).get().getLocation();
            assertEquals(newLoc, loc);
        } catch (JSONException e) {
            fail("Unable to build new JSON object from string.");
        } catch (JsonProcessingException e) {
            fail("Unable to stringify json object.");
        }
    }

    public InternalResponseObject<Boolean> moveCharacter(Player p, UUID characterID, List<Location> path, boolean failTestOnFailure) {
        MoveGameActionData actionData = new MoveGameActionData(characterID.toString(), path);
        GameActionBaseRequest req = new GameActionBaseRequest();
        try {
            String json = objMapper.writeValueAsString(actionData.convertToJSON());
            req.fillFromJSON(new JSONObject(json));
        } catch (Exception e) {
            fail("Unable to create input data. Error: " + e.getMessage());
        }
        InternalResponseObject<Boolean> response = game.gameAction(req, p);
        if (failTestOnFailure && !response.isNormal()) {
            fail("Incorrect response while moving. ERROR:" + response.getMessage());
        }
        updateMatchState();
        Hero newHeroState = (Hero) currentMatchState.getBoardObjects().getByID(characterID).get();
        Location newHeroLocation = newHeroState.getLocation();
        if (failTestOnFailure) {
            assertEquals(path.get(path.size() - 1), newHeroLocation);
        }
        return response;
    }

    public InternalResponseObject<Boolean> passCharacter(Player p, Creature character, boolean failTestOnFailure) {
        PassGameActionData actionData = new PassGameActionData(character.getGameObjectID());
        GameActionBaseRequest req = new GameActionBaseRequest();
        try {
            String json = objMapper.writeValueAsString(actionData.convertToJSON());
            req.fillFromJSON(new JSONObject(json));
        } catch (Exception e) {
            fail("Unable to create input data. Error: " + e.getMessage());
        }
        InternalResponseObject<Boolean> response = game.gameAction(req, p);
        if (failTestOnFailure && !response.isNormal()) {
            fail("Incorrect response while moving. ERROR:" + response.getMessage());
        }
        updateMatchState();
        Hero myHero = (Hero) currentMatchState.getBoardObjects().getByID(character.getGameObjectID()).get();
        int newAP = myHero.getActionPoints();
        if (failTestOnFailure) {
            assertEquals(0, newAP);
        }
        return response;
    }
}
