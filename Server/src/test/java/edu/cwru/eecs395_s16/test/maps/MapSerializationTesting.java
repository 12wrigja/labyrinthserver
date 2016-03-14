package edu.cwru.eecs395_s16.test.maps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.repositories.MapRepository;
import edu.cwru.eecs395_s16.networking.requests.NewMapRequest;
import edu.cwru.eecs395_s16.networking.responses.NewMapResponse;
import edu.cwru.eecs395_s16.services.connections.SocketIOConnectionService;
import edu.cwru.eecs395_s16.test.EngineOnlyTest;
import edu.cwru.eecs395_s16.test.NetworkedTest;
import edu.cwru.eecs395_s16.test.SerializationTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by james on 2/12/16.
 */
public class MapSerializationTesting extends SerializationTest {

    final int MAP_X = 6;
    final int MAP_Y = 7;

    @Test
    public void testSizeSerialization() throws JSONException, JsonProcessingException {
        NewMapRequest request = new NewMapRequest(MAP_X,MAP_Y);
        //Start request
        InternalResponseObject<GameMap> obj = engine.networkingInterface.map(request);
        //Validate the response's values
        assertTrue(obj.isNormal());
        assertTrue(obj.isPresent());
        int generatedMapX = obj.get().getSizeX();
        int generatedMapY = obj.get().getSizeY();
        assertEquals(MAP_X,generatedMapX);
        assertEquals(MAP_Y,generatedMapY);

        //Validate the serialization values
        JSONObject serializedResponse = new JSONObject(objMapper.writeValueAsString(obj));
        assertTrue(serializedResponse.has("map"));
        JSONObject map = serializedResponse.getJSONObject("map");
        assertEquals(MAP_X,map.getJSONObject("size").getInt("x"));
        assertEquals(MAP_Y,map.getJSONObject("size").getInt("y"));
    }

    @Test
    public void testTileSpecification() throws JSONException, JsonProcessingException {
        NewMapRequest request = new NewMapRequest(MAP_X, MAP_Y);
        InternalResponseObject<GameMap> response = engine.networkingInterface.map(request);
        JSONObject serialized = new JSONObject(objMapper.writeValueAsString(response));
        assertTrue(serialized.has("map"));
        JSONObject map = serialized.getJSONObject("map");
        //Validate the tile spec
        JSONArray tiles = map.getJSONArray("tiles");
        assertEquals(MAP_X*MAP_Y,tiles.length());
        for(int i=0; i<MAP_X*MAP_Y; i++){
            JSONObject tile = tiles.getJSONObject(i);

            //Validate the position
            int x = tile.getInt("x");
            int y = tile.getInt("y");
            assertTrue(x >= 0);
            assertTrue(x < MAP_X);
            assertTrue(y >= 0);
            assertTrue(y < MAP_Y);

            //Validate the tile type
            assertTrue(tile.has("terrain"));
            String terrain = tile.getString("terrain");
            assertNotNull(terrain);

            //Validate obstacle type or not
            assertTrue(tile.has("is_obstacle"));

            //Validate rotation
            assertTrue(tile.has("rotation"));
        }
    }
}
