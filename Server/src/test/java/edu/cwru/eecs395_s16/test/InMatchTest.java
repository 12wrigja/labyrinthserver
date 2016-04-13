package edu.cwru.eecs395_s16.test;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.objects.creatures.UsePattern;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.core.objects.objectives.DeathmatchGameObjective;
import edu.cwru.eecs395_s16.core.objects.objectives.GameObjective;
import edu.cwru.eecs395_s16.networking.requests.gameactions.BasicAttackActionData;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.services.bots.botimpls.TestBot;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by james on 2/27/16.
 */
public abstract class InMatchTest extends SerializationTest {

    protected GameBot heroBot;
    protected GameBot architectBot;
    protected Match currentMatchState;
    protected List<Hero> initialHeroes;
    protected List<GameObject> initialArchitectObjects;
    protected GameMap initialGameMap;
    protected GameObjective initialObjective;
    protected Set<UUID> useHeroIDs;
    protected Map<Location, Integer> useMonsterIDs;
    protected NetworkingInterface game;

    @Override
    public void setup() throws Exception {
        super.setup();
        heroBot = getHero();
        architectBot = getArchitect();
        initialGameMap = new AlmostBlankMap(10, 10);
    }

    @Override
    public void teardown() throws Exception {
        teardownMatch();
        super.teardown();
    }

    public void teardownMatch() throws Exception {
        if (currentMatchState != null) {
            game.leaveMatch(new NoInputRequest(), heroBot);
            game.leaveMatch(new NoInputRequest(), architectBot);
        }
        heroBot.disconnect();
        architectBot.disconnect();
    }

    protected GameBot getHero() {
        return new TestBot();
    }

    protected GameBot getArchitect() {
        return new TestBot();
    }

    protected Set<UUID> useHeros() {
        return null;
    }

    protected Map<Location, Integer> placeArchitectMonsters() {
        return null;
    }

    public InternalResponseObject<Match> setupMatch(boolean shouldAutoFail) {
        if (initialHeroes != null) {
            heroBot.replaceBotHeroes(initialHeroes);
        }
        if (initialArchitectObjects != null) {
            architectBot.replaceArchitectObjects(initialArchitectObjects);
        }
        if (initialObjective == null) {
            initialObjective = new DeathmatchGameObjective();
        }
        if (useHeroIDs == null) {
            useHeroIDs = useHeros();
        }
        if (useMonsterIDs == null) {
            useMonsterIDs = placeArchitectMonsters();
        }

        InternalResponseObject<Match> matchOpt = Match.InitNewMatch(heroBot, architectBot, initialGameMap, initialObjective, useHeroIDs, useMonsterIDs);
        if (matchOpt.isNormal()) {
            game = engine.networkingInterface;
            updateMatchState(heroBot);
        } else {
            if (shouldAutoFail) {
                fail("Unable to setup a match. ERROR: " + matchOpt.getInternalErrorCode().message);
            }
        }
        return matchOpt;
    }

    public void updateMatchState(Player player) {
        InternalResponseObject<Match> r = game.matchState(new NoInputRequest(), player);
        if (!r.isNormal()) {
            fail("Unable to get current match state. ERROR: " + r.getMessage());
        } else {
            this.currentMatchState = r.get();
        }
    }

    public void forceSetCharacterLocation(UUID characterID, Location loc, Player player) {
        updateMatchState(player);
        Optional<GameObject> objOpt = currentMatchState.getBoardObjects().getByID(characterID);
        if (objOpt.isPresent()) {
            currentMatchState.doAndSnapshot("Forced snapshot for movement", () -> objOpt.get().setLocation(loc), true);
        } else {
            fail("Your trying to force set a character that doesn't exist. STOP.");
            return;
        }
        updateMatchState(player);
        Location newLoc = currentMatchState.getBoardObjects().getByID(characterID).get().getLocation();
        assertEquals(loc, newLoc);
    }

    public InternalResponseObject<Boolean> moveCharacter(Player p, UUID characterID, List<Location> path, boolean failTestOnFailure) {
        MoveGameActionData actionData = new MoveGameActionData(characterID, path);
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
        updateMatchState(p);
        Creature newHeroState = (Creature) currentMatchState.getBoardObjects().getByID(characterID).get();
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
        updateMatchState(p);
        Creature myCharacter = (Creature) currentMatchState.getBoardObjects().getByID(character.getGameObjectID()).get();
        int newAP = myCharacter.getActionPoints();
        if (failTestOnFailure) {
            assertEquals(0, newAP);
        }
        return response;
    }

    public InternalResponseObject<Boolean> basicAttackWithCharacter(Player p, UUID characterID, List<Location> inputs, boolean failTestOnFailure) {
        Optional<GameObject> cObj = currentMatchState.getBoardObjects().getByID(characterID);
        if (!cObj.isPresent() || !(cObj.get() instanceof Creature)) {
            return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_OBJECT);
        }
        Creature character = (Creature) cObj.get();

        Weapon w = character.getWeapon();
        UsePattern pattern = w.getUsePattern();

        //Determine the predicted quantity of action points.
        int initialActionPoints = character.getActionPoints();
        int predictedNextActionPoints = Math.max(initialActionPoints - 1, 0);
        List<Creature> playerCreatures = currentMatchState.getBoardObjects().getForPlayerOwner(p).stream().filter(obj -> obj instanceof Creature && ((Creature) obj).getActionPoints() > 0).map(obj -> (Creature) obj).collect(Collectors.toList());
        if (playerCreatures.size() == 1 && playerCreatures.get(0).equals(character) && character.getActionPoints() == 1) {
            predictedNextActionPoints = character.getMaxActionPoints();
        }
        Map<Creature, Integer> affectedCreatureDamageMap = new HashMap<>();
        for (Location input : inputs) {
            Map<Location, Float> hitmap = pattern.getEffectDistributionMap(input);
            for (Map.Entry<Location, Float> entry : hitmap.entrySet()) {
                List<Creature> creatures = currentMatchState
                        .getBoardObjects()
                        .getForLocation(entry.getKey())
                        .stream().filter(obj -> obj instanceof Creature)
                        .map(tmp -> (Creature) tmp).collect(Collectors.toList());
                for (Creature c : creatures) {
                    int baseDamage = (int) Math.floor(((100 - c.getDefense()) / 100f) * character.getAttack());
                    affectedCreatureDamageMap.put(c, Math.max(0, c.getHealth() - (int) ((float) baseDamage * entry.getValue())));
                }
            }
        }

        BasicAttackActionData actionData = new BasicAttackActionData(character.getGameObjectID(), inputs);
        GameActionBaseRequest req = new GameActionBaseRequest();
        try {
            String json = objMapper.writeValueAsString(actionData.convertToJSON());
            req.fillFromJSON(new JSONObject(json));
        } catch (Exception e) {
            fail("Unable to create input data. Error: " + e.getMessage());
        }
        InternalResponseObject<Boolean> response = game.gameAction(req, p);
        if (failTestOnFailure && !response.isNormal()) {
            fail("Incorrect response while attacking. ERROR:" + response.getMessage());
        }
        updateMatchState(p);
        Optional<GameObject> myCreature = currentMatchState.getBoardObjects().getByID(character.getGameObjectID());
        if (myCreature.isPresent() && failTestOnFailure) {
            assertEquals(predictedNextActionPoints, ((Creature) myCreature.get()).getActionPoints());
        } else if (failTestOnFailure) {
            fail("Unable to find the original character in the board objects.");
        }
        if (failTestOnFailure) {
            for (Map.Entry<Creature, Integer> affectedCreature : affectedCreatureDamageMap.entrySet()) {
                Optional<GameObject> cResp = currentMatchState.getBoardObjects().getByID(affectedCreature.getKey().getGameObjectID());
                if (cResp.isPresent()) {
                    Creature c = (Creature) cResp.get();
                    assertEquals((int) affectedCreature.getValue(), c.getHealth());
                } else {
                    fail("Can't find one of the affected creatures.");
                }
            }
        }
        return response;
    }
}
