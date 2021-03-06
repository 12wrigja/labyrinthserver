package edu.cwru.eecs395_s16.test.maps;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.networking.requests.LoginUserRequest;
import edu.cwru.eecs395_s16.networking.requests.RegisterUserRequest;
import edu.cwru.eecs395_s16.test.SingleUserNetworkTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by james on 4/17/16.
 */
public class MapStorgeTesting extends SingleUserNetworkTest {

    String USERNAME = "testusername";
    String PASSWORD = "testpassword";

    @Before
    public void setup() {
        setupSingleClient();
        InternalResponseObject<Player> registerResp = engine.networkingInterface.register(new RegisterUserRequest
                (USERNAME, PASSWORD, PASSWORD));
        if (!registerResp.isNormal()) {
            fail("Unable to register user for tests.");
            return;
        }
        JSONObject loginResp = emitEventAndWaitForResult(socket, "login", new LoginUserRequest(USERNAME, PASSWORD)
                .convertToJSON(), 10);
        try {
            if (loginResp.getInt("status") != 200) {
                fail("Unable to log in.");
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Unable to log in due to JSON Exception.");
        }
    }

    @After
    public void deleteUser() {
        engine.services.playerRepository.deletePlayer(new Player(-1, USERNAME, PASSWORD, false));
    }

    @Test
    public void testRetrieveMapMetadata() throws JSONException {
        JSONObject mapMetadataResp = emitEventAndWaitForResult(socket, "maps", new JSONObject(), 10);
        assertEquals(200, mapMetadataResp.getInt("status"));
        assertTrue(mapMetadataResp.has("maps"));
        JSONArray mapMetadataArr = mapMetadataResp.getJSONArray("maps");
        assertTrue(mapMetadataArr.length() > 0);
        for (int i = 0; i < mapMetadataArr.length(); i++) {
            JSONObject mapMetadata = mapMetadataArr.getJSONObject(i);
            assertEquals(5, mapMetadata.length());
            assertTrue("No size key.", mapMetadata.has(GameMap.MAP_SIZE_KEY));
            assertTrue("No creator id key.", mapMetadata.has(GameMap.MAP_CREATOR_ID_KEY));
            assertTrue("No Hero Capacity Key.", mapMetadata.has(GameMap.MAP_HERO_CAPACITY_KEY));
            assertTrue("No Map Name Key.", mapMetadata.has(GameMap.MAP_NAME_KEY));
            assertTrue("No ID key", mapMetadata.has(GameMap.MAP_ID_KEY));
        }
    }

    @Test
    public void correctlyStoreMap() throws JSONException {
        InternalResponseObject<GameMap> map = engine.services.mapRepository.getMapByID(1);
        if (!map.isNormal()) {
            fail("Unable to get a map to clone.");
            return;
        }
        GameMap mp = map.get();
        JSONObject obj = mp.getJSONRepresentation();
        JSONObject newMapIDResp = emitEventAndWaitForResult(socket, "store_map", obj, 10);
        if (newMapIDResp.getInt("status") != 200) {
            fail("Unable to store new map.");
            return;
        }
        int newMapID = newMapIDResp.getInt("map_id");
        assertNotEquals(mp.getDatabaseID(), newMapID);
    }

    @Test
    public void tryStoreMapInvalidTileCount() throws JSONException {
        JSONObject obj = new JSONObject("{ \"tiles\": [{ \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": " +
                "true, \"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 1, \"is_objective_spawn_tile\": " +
                "false, \"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 8, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 8, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": true, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 8, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": true, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": true, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }], \"size\": { \"x\": 10, \"y\": 10 }, \"hero_capacity\": 4, \"name\": " +
                "\"Almost Blank Map\"}");
        JSONObject newMapIDResp = emitEventAndWaitForResult(socket, "store_map", obj, 10);
        if (newMapIDResp.getInt("status") == 200) {
            fail("Was able to store new map.");
        }
    }

    @Test
    public void tryStoreMapDoubleSpecifiedTile() throws JSONException {
        JSONObject obj = new JSONObject("{ \"tiles\": [{ \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": " +
                "true, \"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 1, \"is_objective_spawn_tile\": " +
                "false, \"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 8, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 8, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": true, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 8, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": true, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": true, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" },{ \"is_architect_spawn_tile\": true, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"water\" } ], \"size\": { \"x\": 10, \"y\": 10 }, \"hero_capacity\": 4, \"name\": " +
                "\"Almost Blank Map\", }");
        JSONObject newMapIDResp = emitEventAndWaitForResult(socket, "store_map", obj, 10);
        if (newMapIDResp.getInt("status") == 200) {
            fail("Was able to store map with a tile specified more than once.");
        }
    }

    @Test
    public void tryStoreMapInvalidTileType() throws JSONException {
        JSONObject obj = new JSONObject("{ \"tiles\": [{ \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": " +
                "true, \"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 1, \"is_objective_spawn_tile\": " +
                "false, \"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": true, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 5, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 6, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 7, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 8, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 8, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 8, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": true, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 8, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 0, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 1, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 2, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 3, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 4, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 5, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 6, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": false, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 7, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": true, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 8, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": true, \"is_hero_spawn_tile\": false, " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 9, \"is_objective_spawn_tile\": false, " +
                "\"terrain\": \"whitespace\" }], \"size\": { \"x\": 10, \"y\": 10 }, \"hero_capacity\": 4, \"name\": " +
                "\"Almost Blank Map\" }");
        JSONObject newMapIDResp = emitEventAndWaitForResult(socket, "store_map", obj, 10);
        if (newMapIDResp.getInt("status") == 200) {
            fail("Was able to store new map with invalid tile types.");
        }
    }

    @Test
    public void correctlyStoreMapUsingShorthand() throws JSONException {
        JSONObject obj = new JSONObject("{ \"tiles\": [{ \"is_hero_spawn_tile\": true, \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 0, \"y\": 0, \"terrain\": \"dirt\" }, { \"is_hero_spawn_tile\": true," +
                " \"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 1, \"terrain\": \"dirt\" }, { " +
                "\"is_hero_spawn_tile\": true, \"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 2, " +
                "\"terrain\": \"dirt\" }, { \"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 3, \"terrain\":" +
                " \"dirt\" }, { \"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 4, \"terrain\": \"dirt\" }," +
                " { \"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 5, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 6, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 7, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 8, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 0, \"y\": 9, \"terrain\": \"dirt\" }, { " +
                "\"is_hero_spawn_tile\": true, \"rotation\": 0, \"is_obstacle\": false, \"x\": 1, \"y\": 0, " +
                "\"terrain\": \"dirt\" }, { \"is_hero_spawn_tile\": true, \"rotation\": 0, \"is_obstacle\": false, " +
                "\"x\": 1, \"y\": 1, \"terrain\": \"dirt\" }, { \"is_hero_spawn_tile\": true, \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 1, \"y\": 2, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 1, \"y\": 3, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 1, \"y\": 4, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 1, \"y\": 5, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 1, \"y\": 6, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 1, \"y\": 7, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 1, \"y\": 8, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 1, \"y\": 9, \"terrain\": \"dirt\" }, { \"is_hero_spawn_tile\": true," +
                " \"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 0, \"terrain\": \"dirt\" }, { " +
                "\"is_hero_spawn_tile\": true, \"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 1, " +
                "\"terrain\": \"dirt\" }, { \"is_hero_spawn_tile\": true, \"rotation\": 0, \"is_obstacle\": false, " +
                "\"x\": 2, \"y\": 2, \"terrain\": \"dirt\" }, { \"rotation\": 0, \"is_obstacle\": false, \"x\": 2, " +
                "\"y\": 3, \"terrain\": \"dirt\" }, { \"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 4, " +
                "\"terrain\": \"dirt\" }, { \"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 5, \"terrain\":" +
                " \"dirt\" }, { \"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 6, \"terrain\": \"dirt\" }," +
                " { \"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 7, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 8, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 2, \"y\": 9, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 0, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 1, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 2, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 3, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 4, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 5, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 6, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 7, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 8, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 3, \"y\": 9, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 0, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 4, \"y\": 1, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": true, \"x\": 4, \"y\": 2, \"terrain\": \"wall\" }, { \"rotation\":" +
                " 0, \"is_obstacle\": true, \"x\": 4, \"y\": 3, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 4, \"y\": 4, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 4, \"y\": 5, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 4, \"y\": 6, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 4, \"y\": 7, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 4, \"y\": 8, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 4, \"y\": 9, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 5, \"y\": 0, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 5, \"y\": 1, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 5, \"y\": 2, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 5, \"y\": 3, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 5, \"y\": 4, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 5, \"y\": 5, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 5, \"y\": 6, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 5, \"y\": 7, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 5, \"y\": 8, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 5, \"y\": 9, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 6, \"y\": 0, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 6, \"y\": 1, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 6, \"y\": 2, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 6, \"y\": 3, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 6, \"y\": 4, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 6, \"y\": 5, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 6, \"y\": 6, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 6, \"y\": 7, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 6, \"y\": 8, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 6, \"y\": 9, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 7, \"y\": 0, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 7, \"y\": 1, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 7, \"y\": 2, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 7, \"y\": 3, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 7, \"y\": 4, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 7, \"y\": 5, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 7, \"y\": 6, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 7, \"y\": 7, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 7, \"y\": 8, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 7, \"y\": 9, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 8, \"y\": 0, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": false, \"x\": 8, \"y\": 1, \"terrain\": \"dirt\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 8, \"y\": 2, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 8, \"y\": 3, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 8, \"y\": 4, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 8, \"y\": 5, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 8, \"y\": 6, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 8, \"y\": 7, \"terrain\": \"wall\" }, { \"rotation\": 0, " +
                "\"is_obstacle\": true, \"x\": 8, \"y\": 8, \"terrain\": \"wall\" }, { \"is_architect_spawn_tile\": " +
                "true, \"rotation\": 0, \"is_obstacle\": false, \"x\": 8, \"y\": 9, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 0, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 1, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 2, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 3, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 4, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 5, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 6, \"terrain\": \"dirt\" }, { " +
                "\"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 7, \"terrain\": \"dirt\" }, { " +
                "\"is_architect_spawn_tile\": true, \"rotation\": 0, \"is_obstacle\": false, \"x\": 9, \"y\": 8, " +
                "\"terrain\": \"dirt\" }, { \"is_architect_spawn_tile\": true, \"rotation\": 0, \"is_obstacle\": " +
                "false, \"x\": 9, \"y\": 9, \"terrain\": \"dirt\" }], \"size\": { \"x\": 10, \"y\": 10 }, " +
                "\"hero_capacity\": 4, \"name\": \"Almost Blank Shorthand Map\", }");
        System.out.println(obj.toString());
        JSONObject newMapIDResp = emitEventAndWaitForResult(socket, "store_map", obj, 10);
        if (newMapIDResp.getInt("status") != 200) {
            fail("Unable to store new map.");
            return;
        }
        int newMapID = newMapIDResp.getInt("map_id");
    }

}
