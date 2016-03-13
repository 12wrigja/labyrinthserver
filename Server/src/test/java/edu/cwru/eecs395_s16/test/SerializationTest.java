package edu.cwru.eecs395_s16.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cwru.eecs395_s16.services.connections.SocketIOConnectionService;
import org.junit.BeforeClass;

/**
 * Created by james on 3/12/16.
 */
public abstract class SerializationTest extends EngineOnlyTest {

    protected static ObjectMapper objMapper;

    @BeforeClass
    public static void setupObjectSerializer(){
        objMapper = new SocketIOConnectionService().getManualMapper();
    }

}
