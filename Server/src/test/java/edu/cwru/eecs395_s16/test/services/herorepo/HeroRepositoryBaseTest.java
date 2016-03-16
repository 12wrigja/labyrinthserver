package edu.cwru.eecs395_s16.test.services.herorepo;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.networking.requests.RegisterUserRequest;
import edu.cwru.eecs395_s16.test.EngineOnlyTest;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by james on 3/16/16.
 */
public abstract class HeroRepositoryBaseTest extends EngineOnlyTest {

    private final String TEST_USERNAME = "USERNAMETEST";
    private final String TEST_PASSWORD = "PASSWORDTEST";

    private Player createdPlayer = null;

    @Test
    public void testDoesCreateInitialHeroesOnRegistration(){
        InternalResponseObject<Player> registrationResponse = engine.networkingInterface.register(new RegisterUserRequest(TEST_USERNAME,TEST_PASSWORD,TEST_PASSWORD));
        if(!registrationResponse.isNormal()){
            fail("Error registering player. ERROR: "+registrationResponse.getMessage());
        }
        createdPlayer = registrationResponse.get();
        InternalResponseObject<List<Hero>> allPlayerHeroes = engine.services.heroRepository.getPlayerHeroes(createdPlayer);
        if(!allPlayerHeroes.isNormal()){
            fail("Error creating heroes for registered player. ERROR: "+allPlayerHeroes.getMessage());
        }
        assertTrue(allPlayerHeroes.isNormal());
        assertTrue(allPlayerHeroes.get().size() > 0);
    }

    @Test
    public void testCanSaveHero(){

    }

    @After
    public void cleanup(){
        if(createdPlayer != null) {
            boolean cleaned = GameEngine.instance().services.playerRepository.deletePlayer(createdPlayer);
            assertTrue(cleaned);
        }

    }
}
