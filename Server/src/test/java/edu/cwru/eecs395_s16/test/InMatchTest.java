package edu.cwru.eecs395_s16.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.auth.exceptions.UnauthorizedActionException;
import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.bots.PassBot;
import edu.cwru.eecs395_s16.core.InvalidGameStateException;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.core.objects.maps.FromJSONGameMap;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.networking.requests.GameActionBaseRequest;
import edu.cwru.eecs395_s16.networking.requests.NoInputRequest;
import edu.cwru.eecs395_s16.networking.requests.gameactions.MoveGameActionData;
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

//    protected GameBot heroBot;
//    protected GameBot architectBot;
//    protected JSONObject matchState;
//    protected GameObjectCollection boardObjects;
//    protected GameMap gameMap;
//    protected UUID matchID;
//    protected static ObjectMapper objectMapper;
//    protected static InMatchConfig config;
//
//    @BeforeClass
//    public static void setupObjectMapper() {
//        objectMapper = new ObjectMapper().registerModule(new JsonOrgModule());
//        config = new InMatchConfig();
//    }
//
//    @Before
//    public void setupMatch() throws Exception {
//        heroBot = new PassBot();
//        architectBot = new PassBot();
//        Optional<Match> matchOpt = Match.InitNewMatch(heroBot, architectBot, new AlmostBlankMap(10, 10));
//        if (matchOpt.isPresent()) {
//            matchID = matchOpt.get().getMatchIdentifier();
//            updateMatchState();
//        } else {
//            fail("Unable to setup a match.");
//        }
//    }
//
//    public void updateMatchState() {
//        Response r = game.matchState(new NoInputRequest(), heroBot);
//        try {
//            assertEquals(200, r.getJSONRepresentation().getInt("status"));
//        } catch (JSONException e) {
//            fail("Can't retrieve status of match state command.");
//        }
//        try {
//            matchState = new JSONObject(objectMapper.writeValueAsString(r.getJSONRepresentation().get("game_state")));
//        } catch (JSONException e) {
//            fail("Unable to retrieve game state.");
//        } catch (JsonProcessingException e) {
//            fail("Unable to serialize game state.");
//        }
//        boardObjects = new GameObjectCollection();
//        try {
//            if(!config.skipUpdatingBoardObjects) {
//                boardObjects.fillFromJSONData(matchState.getJSONObject("board_objects"));
//            }
//        } catch (JSONException e) {
//            fail("Unable to get board objects from match state.");
//        }
//        try {
//            if(gameMap == null || !config.skipUpdatingMap) {
//                gameMap = new FromJSONGameMap(matchState.getJSONObject("map"));
//            }
//        } catch (JSONException e) {
//            fail("Unable to get map from match state.");
//        }
//    }
//
//    public void forceSetCharacterLocation(Player p, UUID characterID, Location loc) {
//        Optional<Match> m = Match.fromCacheWithMatchIdentifier(matchID);
//        if (m.isPresent()) {
//            Match match = m.get();
//            try {
//                JSONObject snapshot = new JSONObject(objectMapper.writeValueAsString(match.getJSONRepresentation()));
//                match.getBoardObjects().get(characterID).setLocation(loc);
//                match.takeAndCommitSnapshot(snapshot, "Forced snapshot for movement.");
//                m = Match.fromCacheWithMatchIdentifier(matchID);
//                updateMatchState();
//                Location newLoc = match.getBoardObjects().get(characterID).getLocation();
//                assertEquals(newLoc, loc);
//            } catch (JSONException e) {
//                fail("Unable to build new JSON object from string.");
//            } catch (JsonProcessingException e) {
//                fail("Unable to stringify json object.");
//            }
//        } else {
//            fail("Can't find match to force set character location");
//        }
//    }
//
//    public Location moveCharacter(Player p, UUID characterID, List<Location> path, boolean failTestOnFailure) {
//        Location baseLocation = boardObjects.get(characterID).getLocation();
//        MoveGameActionData actionData = new MoveGameActionData();
//        actionData.setCharacter(characterID.toString());
//        actionData.setPath(path);
//        try {
//            GameActionBaseRequest req = new GameActionBaseRequest();
//            try {
//                String json = objectMapper.writeValueAsString(actionData.convertToJSON());
//                req.fillFromJSON(new JSONObject(json));
//            } catch (Exception e) {
//                fail("Unable to create input data.");
//            }
//            Response response = game.gameAction(req, p);
//            assertEquals(200, response.getJSONRepresentation().getInt("status"));
//            updateMatchState();
//            Hero newHeroState = (Hero) boardObjects.get(characterID);
//            Location newHeroLocation = newHeroState.getLocation();
//            if (failTestOnFailure) {
//                assertEquals(path.get(path.size() - 1), newHeroLocation);
//            }
//            return newHeroLocation;
//        } catch (InvalidDataException e) {
//            if (failTestOnFailure) {
//                fail("Invalid input for move character command.");
//            } else {
//                return baseLocation;
//            }
//            ;
//        } catch (UnauthorizedActionException e) {
//            if (failTestOnFailure) {
//                fail("Bot is not authorized for move command!");
//            } else {
//                return baseLocation;
//            }
//        } catch (InvalidGameStateException e) {
//            if (failTestOnFailure) {
//                fail("Something else went really wrong while doing a move command.");
//            } else {
//                return baseLocation;
//            }
//        } catch (JSONException e) {
//            if (failTestOnFailure) {
//                fail("Unable to get response from JSON.");
//            } else {
//                return baseLocation;
//            }
//        }
//        return baseLocation;
//    }
//
//    @After
//    public void teardownMatch() throws Exception {
//        heroBot.disconnect();
//        architectBot.disconnect();
//    }
//
//    static class InMatchConfig {
//        public boolean skipUpdatingMap = true;
//        public boolean skipUpdatingBoardObjects = false;
//    }

}
