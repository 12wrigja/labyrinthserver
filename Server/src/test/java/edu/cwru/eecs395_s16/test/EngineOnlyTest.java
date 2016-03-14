package edu.cwru.eecs395_s16.test;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.services.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.xml.ws.Service;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by james on 2/26/16.
 */
public abstract class EngineOnlyTest {

    protected static GameEngine engine;

    protected ServiceContainer setupServiceContainer(){
        return new ServiceContainerBuilder().createServiceContainer();
    }

    @Before
    public void setUpGameEngine() throws Exception {
        if(engine == null) {
            System.out.println("Setting up game engine.");
            engine = new GameEngine(false, setupServiceContainer());
            engine.start();
            assertTrue(engine.isStarted());
        }
    }

    @AfterClass
    public static void tearDownGameEngine() throws Exception {
        if (engine != null) {
            engine.stop();
        }
    }

}
