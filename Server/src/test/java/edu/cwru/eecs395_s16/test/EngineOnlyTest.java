package edu.cwru.eecs395_s16.test;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.services.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;

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
        engine = new GameEngine(false, new ServiceContainerBuilder().createServiceContainer());
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
