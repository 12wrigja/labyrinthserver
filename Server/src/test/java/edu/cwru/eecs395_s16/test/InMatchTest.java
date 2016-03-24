package edu.cwru.eecs395_s16.test;

import edu.cwru.eecs395_s16.core.objects.creatures.UsePattern;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.networking.requests.gameactions.BasicAttackActionData;
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
    protected List<GameObject> initialArchitectObjs;
    private NetworkingInterface game;

    @Override
    public void setup() throws Exception {
        super.setup();
        heroBot = new TestBot();
        architectBot = new TestBot();
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

    public void setupMatch() {
        if(initialHeroes == null) {
            heroBot.replaceBotHeroes(getHeroesForHero(heroBot));
        } else {
            heroBot.replaceBotHeroes(initialHeroes);
        }
        if(initialArchitectObjs == null) {
            architectBot.replaceArchitectObjects(getObjectsForArchitect(architectBot));
        } else {
            architectBot.replaceArchitectObjects(initialArchitectObjs);
        }
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
        Creature myCharacter = (Creature) currentMatchState.getBoardObjects().getByID(character.getGameObjectID()).get();
        int newAP = myCharacter.getActionPoints();
        if (failTestOnFailure) {
            assertEquals(0, newAP);
        }
        return response;
    }

    public InternalResponseObject<Boolean> basicAttackWithCharacter(Player p, Creature character, List<Location> inputs, boolean failTestOnFailure){
        Weapon w = character.getWeapon();
        UsePattern pattern = w.getUsePattern();
        Integer baseDamage = character.getAttack();
        int initialActionPoints = character.getActionPoints();
        Map<Creature,Integer> affectedCreatureDamageMap = new HashMap<>();
        for(Location input : inputs){
            Map<Location,Float> hitmap = pattern.getEffectDistributionMap(input);
            for(Map.Entry<Location,Float> entry : hitmap.entrySet()){
                List<Creature> creatures = currentMatchState
                        .getBoardObjects()
                        .getForLocation(entry.getKey())
                        .stream().filter(obj->obj instanceof Creature)
                        .map(tmp->(Creature)tmp).collect(Collectors.toList());
                for(Creature c : creatures){
                    affectedCreatureDamageMap.put(c,Math.max(0,c.getHealth()-(int)((float)baseDamage*entry.getValue())));
                }
            }
        }

        BasicAttackActionData actionData = new BasicAttackActionData(character.getGameObjectID(),inputs);
        GameActionBaseRequest req = new GameActionBaseRequest();
        try {
            String json = objMapper.writeValueAsString(actionData.convertToJSON());
            req.fillFromJSON(new JSONObject(json));
        } catch (Exception e){
            fail("Unable to create input data. Error: " + e.getMessage());
        }
        InternalResponseObject<Boolean> response = game.gameAction(req,p);
        if(failTestOnFailure && !response.isNormal()){
            fail("Incorrect response while attacking. ERROR:" + response.getMessage());
        }
        updateMatchState();
        Optional<GameObject> myCreature = currentMatchState.getBoardObjects().getByID(character.getGameObjectID());
        if(myCreature.isPresent() && failTestOnFailure){
            assertEquals(initialActionPoints -1, ((Creature)myCreature.get()).getActionPoints());
        } else if (failTestOnFailure){
            fail("Unable to find the original character in the board objects.");
        }
        if(failTestOnFailure) {
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
