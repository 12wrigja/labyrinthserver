package edu.cwru.eecs395_s16.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.bots.PassBot;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.repositories.HeroRepository;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;
import edu.cwru.eecs395_s16.networking.requests.GameActionBaseRequest;
import edu.cwru.eecs395_s16.networking.requests.NoInputRequest;
import edu.cwru.eecs395_s16.networking.requests.gameactions.MoveGameActionData;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.connections.SocketIOConnectionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by james on 2/27/16.
 */
public abstract class InMatchTest extends EngineOnlyTest {

    protected GameBot heroBot;
    protected GameBot architectBot;
    protected Match currentMatchState;
    protected NetworkingInterface game;
    protected static ObjectMapper objectMapper;

    @BeforeClass
    public static void setupObjectMapper() {
        objectMapper = new SocketIOConnectionService().getManualMapper();
    }

    @Before
    public void setupMatch() throws Exception {
        heroBot = new PassBot();
        architectBot = new PassBot();
        GameMap gameMap = new AlmostBlankMap(10, 10);
        Hero h = new HeroBuilder().setOwnerID(Optional.of(heroBot.getUsername())).createHero();
        Hero h1 = new HeroBuilder().setOwnerID(Optional.of(heroBot.getUsername())).createHero();
        HeroRepository heroRepo = engine.getHeroRepository();
        heroRepo.saveHeroForPlayer(heroBot, h);
        heroRepo.saveHeroForPlayer(architectBot, h1);

        InternalResponseObject<Match> matchOpt = Match.InitNewMatch(heroBot, architectBot, gameMap);
        if (matchOpt.isNormal()) {
            game = engine.getNetworkingInterface();
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
        try {
            JSONObject snapshot = new JSONObject(objectMapper.writeValueAsString(currentMatchState.getJSONRepresentation()));
            currentMatchState.getBoardObjects().get(characterID).setLocation(loc);
            currentMatchState.takeAndCommitSnapshot(snapshot, "Forced snapshot for movement.");
            updateMatchState();
            Location newLoc = currentMatchState.getBoardObjects().get(characterID).getLocation();
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
            String json = objectMapper.writeValueAsString(actionData.convertToJSON());
            req.fillFromJSON(new JSONObject(json));
        } catch (Exception e) {
            fail("Unable to create input data. Error: " + e.getMessage());
        }
        InternalResponseObject<Boolean> response = game.gameAction(req, p);
        if (failTestOnFailure && !response.isNormal()) {
            fail("Incorrect response while moving. ERROR:" + response.getMessage());
        }
        updateMatchState();
        Hero newHeroState = (Hero) currentMatchState.getBoardObjects().get(characterID);
        Location newHeroLocation = newHeroState.getLocation();
        if (failTestOnFailure) {
            assertEquals(path.get(path.size() - 1), newHeroLocation);
        }
        return response;
    }

    @After
    public void teardownMatch() throws Exception {
        game.leaveMatch(new NoInputRequest(), heroBot);
        heroBot.disconnect();
        architectBot.disconnect();
    }

}