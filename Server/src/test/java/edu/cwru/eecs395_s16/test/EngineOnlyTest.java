package edu.cwru.eecs395_s16.test;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.networking.matchmaking.BasicMatchmakingService;
import edu.cwru.eecs395_s16.services.InMemoryCacheService;
import edu.cwru.eecs395_s16.services.InMemoryHeroRepository;
import edu.cwru.eecs395_s16.services.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.services.InMemorySessionRepository;
import edu.cwru.eecs395_s16.services.MapRepository.InMemoryMapRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.BindException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by james on 2/26/16.
 */
public abstract class EngineOnlyTest {

    protected static GameEngine engine;

    @BeforeClass
    public static void setUpGameEngine() throws Exception {
        System.out.println("Setting up game engine.");
        engine = new GameEngine(false, new InMemoryPlayerRepository(), new InMemorySessionRepository(), new InMemoryHeroRepository(), new BasicMatchmakingService(), new InMemoryCacheService(), new InMemoryMapRepository());
        engine.start();
        assertTrue(engine.isStarted());
    }

    @AfterClass
    public static void tearDownGameEngine() throws Exception {
        if (engine != null) {
            engine.stop();
        }
    }

}
