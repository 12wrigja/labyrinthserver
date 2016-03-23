package edu.cwru.eecs395_s16.test;

import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.services.bots.botimpls.TestBot;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;
import edu.cwru.eecs395_s16.networking.requests.GameActionBaseRequest;
import edu.cwru.eecs395_s16.networking.requests.NoInputRequest;
import edu.cwru.eecs395_s16.networking.requests.gameactions.MoveGameActionData;
import edu.cwru.eecs395_s16.networking.requests.gameactions.PassGameActionData;
import org.json.JSONObject;

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

    public List<Hero> getHeroesForHero(Player hero) {
        Hero h = new HeroBuilder(hero.getUsername()).createHero();
        List<Hero> heroes = new ArrayList<>();
        heroes.add(h);
        return heroes;
    }

    public List<GameObject> getObjectsForArchitect(Player architect) {
        Hero hero = new HeroBuilder(architect.getUsername()).createHero();
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
        if (objOpt.isPresent()) {
            currentMatchState.doAndSnapshot("Forced snapshot for movement",()->objOpt.get().setLocation(loc), true);
        } else {
            fail("Your trying to force set a character that doesn exist. STOP.");
            return;
        }
        updateMatchState();
        Location newLoc = currentMatchState.getBoardObjects().getByID(characterID).get().getLocation();
        assertEquals(loc, newLoc);
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
