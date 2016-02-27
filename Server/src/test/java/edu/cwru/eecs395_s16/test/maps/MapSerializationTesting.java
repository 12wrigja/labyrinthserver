package edu.cwru.eecs395_s16.test.maps;

import edu.cwru.eecs395_s16.test.NetworkedTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by james on 2/12/16.
 */
public class MapSerializationTesting extends NetworkedTest {

    final int MAP_X = 6;
    final int MAP_Y = 7;

    @Test
    public void testSizeSerialization() throws JSONException {
        JSONObject inputs = new JSONObject();
        inputs.put("x",MAP_X);
        inputs.put("y",MAP_Y);
        JSONObject obj = emitEventAndWaitForResult("map",inputs);
        JSONObject map = obj.getJSONObject("map");
        assertEquals(MAP_X,map.getJSONObject("size").getInt("x"));
        assertEquals(MAP_Y,map.getJSONObject("size").getInt("y"));
    }

    @Test
    public void testTileSpecification() throws JSONException {

        JSONObject inputs = new JSONObject();
        inputs.put("x",MAP_X);
        inputs.put("y",MAP_Y);
        JSONObject obj = emitEventAndWaitForResult("map",inputs);
        JSONObject map = obj.getJSONObject("map");

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

            //Validate rotation
            assertTrue(tile.has("rotation"));
        }
    }
}
