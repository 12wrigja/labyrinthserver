package edu.cwru.eecs395_s16.test.maps;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.networking.requests.GetMapRequest;
import edu.cwru.eecs395_s16.services.bots.botimpls.TestBot;
import edu.cwru.eecs395_s16.test.SerializationTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by james on 2/12/16.
 */
public class MapSerializationTesting extends SerializationTest {

    public static final int MAP_X = 10;
    public static final int MAP_Y = 10;

    @Test
    public void testSizeSerialization() throws JSONException, JsonProcessingException {
        GetMapRequest request = new GetMapRequest(1);
        //Start request
        InternalResponseObject<GameMap> obj = engine.networkingInterface.map(request, new TestBot());
        //Validate the response's values
        assertTrue(obj.isNormal());
        assertTrue(obj.isPresent());
        int generatedMapX = obj.get().getSizeX();
        int generatedMapY = obj.get().getSizeY();
        assertEquals(MAP_X, generatedMapX);
        assertEquals(MAP_Y, generatedMapY);

        //Validate the serialization values
        JSONObject serializedResponse = new JSONObject(objMapper.writeValueAsString(obj));
        assertTrue(serializedResponse.has("map"));
        JSONObject map = serializedResponse.getJSONObject("map");
        assertEquals(MAP_X, map.getJSONObject("size").getInt("x"));
        assertEquals(MAP_Y, map.getJSONObject("size").getInt("y"));
    }

    @Test
    public void testTileSpecification() throws JSONException, JsonProcessingException {
        GetMapRequest request = new GetMapRequest(1);
        InternalResponseObject<GameMap> response = engine.networkingInterface.map(request, new TestBot());
        JSONObject serialized = new JSONObject(objMapper.writeValueAsString(response));
        assertTrue(serialized.has("map"));
        JSONObject map = serialized.getJSONObject("map");
        //Validate the tile spec
        JSONArray tiles = map.getJSONArray("tiles");
        assertEquals(MAP_X * MAP_Y, tiles.length());
        for (int i = 0; i < MAP_X * MAP_Y; i++) {
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
