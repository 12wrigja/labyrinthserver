package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.GameState;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterDefinition;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.services.bots.botimpls.PassBot;
import edu.cwru.eecs395_s16.services.monsters.MonsterRepository;
import edu.cwru.eecs395_s16.test.AutoStartInMatchTest;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by james on 3/15/16.
 */
public class PassAndTurnTestingMonsters extends AutoStartInMatchTest {

    public MonsterDefinition goblinDef;

    @Override
    protected GameBot getHero() {
        PassBot b = new PassBot();
        b.setEnabled(false);
        return b;
    }

    @Override
    protected void changeArchitectList(Player architect, List<GameObject> architectObjects) {
        goblinDef = GameEngine.instance().services.monsterRepository.getMonsterDefinitionForId(1).get();
        architectObjects.add(new MonsterBuilder(UUID.randomUUID(), goblinDef,architect.getUsername(), Optional.of(architect.getUsername())).createMonster());
    }


    @Override
    protected Map<Location, Integer> placeArchitectMonsters() {
        Map<Location,Integer> ret = new HashMap<>();
        List<Location> archSpawnTiles = initialGameMap.getArchitectCreatureSpawnLocations();
        Collections.shuffle(archSpawnTiles);
        int placedMonsters = 0;
        for(GameObject obj :architectBot.getArchitectObjects()){
            if(obj instanceof Monster){
                ret.put(archSpawnTiles.get(placedMonsters),((Monster)obj).getDatabaseID());
                placedMonsters++;
            }
        }
        return ret;
    }


    @Test
    public void testPassAction(){
        //Get a character for the hero
        List<GameObject> monsters = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(monsters.size() > 0);
        Monster h = (Monster) monsters.get(0);
        ((PassBot)heroBot).passAllCharacters();
        waitForMyTurn();
        passCharacter(architectBot,h,true);
    }

    @Test
    public void testMultiplePassActions(){
        //Get a character for the hero
        List<GameObject> monsters = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(monsters.size() > 0);
        Monster h = (Monster) monsters.get(0);
        ((PassBot)heroBot).passAllCharacters();
        waitForMyTurn();
        passCharacter(architectBot,h,true);
        InternalResponseObject<Boolean> resp = passCharacter(architectBot,h,false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NO_ACTION_POINTS,resp.getInternalErrorCode());
    }

    @Test
    public void testTurnEndingDueToPassActions(){
        //Get a character for the hero
        List<GameObject> monsters = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(monsters.size() > 0);
        Monster h = (Monster) monsters.get(0);
        ((PassBot)heroBot).passAllCharacters();
        waitForMyTurn();
        int currentTurnNumber = currentMatchState.getTurnNumber();
        monsters.stream().filter(obj -> obj instanceof Creature).forEach(obj -> {
            assertTrue(passCharacter(architectBot, ((Creature) obj), false).isNormal());
        });
        InternalResponseObject<Boolean> resp = passCharacter(architectBot,h,false);
        assertFalse(resp.isNormal());
        assertEquals(InternalErrorCode.NOT_YOUR_TURN,resp.getInternalErrorCode());
        assertEquals(currentTurnNumber+1,currentMatchState.getTurnNumber());

        List<GameObject> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot);
        assertTrue(architectChars.size() > 0);
        ((PassBot)heroBot).passAllCharacters();
        waitForMyTurn();
        currentTurnNumber = currentMatchState.getTurnNumber();
        architectChars.stream().filter(obj -> obj instanceof Creature).forEach(obj -> {
            assertTrue(passCharacter(architectBot, (Creature) obj, false).isNormal());
        });
        Optional<GameObject> potentialCreature = architectChars.stream().filter(obj -> obj instanceof Creature).findFirst();
        if(potentialCreature.isPresent()){
            Creature creature = (Creature) potentialCreature.get();
            InternalResponseObject<Boolean> resp1 = passCharacter(architectBot, creature, false);
            assertFalse(resp1.isNormal());
            assertEquals(InternalErrorCode.NOT_YOUR_TURN, resp1.getInternalErrorCode());
            assertEquals(currentTurnNumber+1,currentMatchState.getTurnNumber());
        } else {
            fail("Unable to retrieve a creature to attempt passing again with.");
        }
    }

    private void waitForMyTurn(){
        do {
            updateMatchState(heroBot);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        } while (!currentMatchState.getGameState().equals(GameState.ARCHITECT_TURN));
    }

}
