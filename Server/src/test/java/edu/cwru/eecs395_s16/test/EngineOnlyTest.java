package edu.cwru.eecs395_s16.test;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.services.containers.ServiceContainer;
import edu.cwru.eecs395_s16.services.matchmaking.BasicMatchmakingService;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertTrue;

/**
 * Created by james on 2/26/16.
 */
public abstract class EngineOnlyTest {

    protected GameEngine engine;

    public ServiceContainer buildContainer() {
        return ServiceContainer.buildInMemoryContainer(CoreDataUtils.defaultCoreData(), new BasicMatchmakingService());
    }

    @Before
    public void setup() throws Exception {
        if (engine == null) {
            setUpGameEngine();
        }
    }

    @After
    public void teardown() throws Exception {
        tearDownGameEngine();
    }

    private void setUpGameEngine() throws Exception {
        System.out.println("Setting up game engine.");
        engine = new GameEngine(true, buildContainer());
        engine.start();
        assertTrue(engine.isStarted());
    }

    private void tearDownGameEngine() throws Exception {
        if (engine != null) {
            engine.stop();
        }
    }
}
