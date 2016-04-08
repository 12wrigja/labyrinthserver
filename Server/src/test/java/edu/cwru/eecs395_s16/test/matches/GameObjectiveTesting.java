package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.core.GameState;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.maps.MapTile;
import edu.cwru.eecs395_s16.core.objects.objectives.DeathmatchGameObjective;
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by james on 3/26/16.
 */
public class GameObjectiveTesting extends InMatchTest {

    @Override
    protected GameBot getArchitect() {
        return new PassBot();
    }

    @Test
    public void testDeathmatchGameObjective(){
        initialObjective = new DeathmatchGameObjective();
        setupMatch();

        //Get a character for the hero
        List<GameObject> heroChars = currentMatchState.getBoardObjects().getForPlayerOwner(heroBot);
        assertEquals(1,heroChars.size());
        Hero h = (Hero) heroChars.get(0);
        UUID heroID = h.getGameObjectID();

        List<Creature> architectChars = currentMatchState.getBoardObjects().getForPlayerOwner(architectBot).stream().filter(obj ->obj instanceof Creature).map(obj->(Creature)obj).collect(Collectors.toList());
        assertEquals(1,architectChars.size());
        Creature c = architectChars.get(0);
        //Figure out where the hero is and try and move it one tile away.
        Location l  = c.getLocation();
        List<MapTile> maptiles = currentMatchState.getGameMap().getTileNeighbours(l).stream().filter(loc->currentMatchState.getGameMap().getTile(loc).isPresent()).map(loc->currentMatchState.getGameMap().getTile(loc).get()).filter(tile->!tile.isObstructionTileType()).collect(Collectors.toList());
        Location moveToLocation = maptiles.get(0);

        forceSetCharacterLocation(h.getGameObjectID(),moveToLocation);

        List<Location> attackLocations = new ArrayList<>();
        attackLocations.add(c.getLocation());
        UUID creatureID = c.getGameObjectID();
        int attackCount = 0;
        while(c.getHealth() > 0){
            attackCount++;
            System.out.println(attackCount);
            basicAttackWithCharacter(heroBot,heroID,attackLocations,false);
            Optional<GameObject> opt = currentMatchState.getBoardObjects().getByID(creatureID);
            if(opt.isPresent()) {
                c = (Creature)opt.get();
//                System.out.println(c.getHealth());
            } else {
                fail("Unable to re-retrieve the character we are attacking.");
            }
            while(!(currentMatchState.getGameState()== GameState.HERO_TURN || currentMatchState.getGameState()==GameState.GAME_END)){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateMatchState();
            }
        }
        updateMatchState();
        assertEquals(GameState.GAME_END,currentMatchState.getGameState());

    }

}
