package edu.cwru.eecs395_s16.test.internalresponses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.connections.SocketIOConnectionService;
import edu.cwru.eecs395_s16.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by james on 3/3/16.
 */
public class InternalResponseTests {

    static ObjectMapper mapper;

    @BeforeClass
    public static void setupObjectMapper(){
        mapper = new SocketIOConnectionService().getManualMapper();
    }

    // Tests for valid return paths

    @Test
    public void testBasicResponseNoObjectNoMessage(){
        InternalResponseObject<JSONObject> obj = new InternalResponseObject<>(new JSONObject());
        assertTrue(obj.isNormal());
        assertTrue(obj.isPresent());
        assertEquals(WebStatusCode.OK,obj.getStatus());
        assertEquals(InternalErrorCode.UNKNOWN,obj.getInternalErrorCode());
        assertNull(obj.getMessage());
        try {
            JSONObject json = new JSONObject(mapper.writeValueAsString(obj));
            assertEquals(1,JSONObject.getNames(json).length);
            assertTrue(json.has("status"));
            assertEquals(WebStatusCode.OK.code,json.getInt("status"));
        } catch (JSONException e) {
            fail("Unable to parse incoming json.");
        } catch (JsonProcessingException e) {
            fail("Unable to convert response to json.");
        }
    }

    @Test
    public void testBasicResponseSimpleObjectNotReturnedNoMessage(){
        //Build up object for testing
        //Note that this object will not be included in the response serialization.
        JSONObject jObj = new JSONObject();
        try{
            jObj.put("blah","blahblahblah");
        } catch (JSONException e){
            fail("Unable to build up the JSON object for testing.");
        }

        InternalResponseObject<JSONObject> obj = new InternalResponseObject<>(jObj);
        assertTrue(obj.isNormal());
        assertTrue(obj.isPresent());
        assertEquals(WebStatusCode.OK,obj.getStatus());
        assertEquals(InternalErrorCode.UNKNOWN,obj.getInternalErrorCode());
        assertNull(obj.getMessage());
        try {
            JSONObject json = new JSONObject(mapper.writeValueAsString(obj));
            assertEquals(1,JSONObject.getNames(json).length);
            assertTrue(json.has("status"));
            assertEquals(WebStatusCode.OK.code,json.getInt("status"));
        } catch (JSONException e) {
            fail("Unable to parse incoming json.");
        } catch (JsonProcessingException e) {
            fail("Unable to convert response to json.");
        }
    }

    @Test
    public void testBasicResponseSimpleObjectReturnedNoMessage(){
        //Build up object for testing
        //Note that this object will not be included in the response serialization.
        JSONObject jObj = new JSONObject();
        try{
            jObj.put("blah","blahblahblah");
        } catch (JSONException e){
            fail("Unable to build up the JSON object for testing.");
        }
        String key = "testkey";
        InternalResponseObject<JSONObject> obj = new InternalResponseObject<>(jObj,key);
        assertTrue(obj.isNormal());
        assertTrue(obj.isPresent());
        assertNull(obj.getMessage());
        assertTrue(JSONUtils.isEquivalent(jObj,obj.get()));
        try {
            JSONObject json = new JSONObject(mapper.writeValueAsString(obj));
            assertEquals(2,JSONObject.getNames(json).length);
            assertTrue(json.has("status"));
            assertEquals(WebStatusCode.OK.code,json.getInt("status"));
            assertTrue(json.has(key));
            JSONObject retrievedObj = (JSONObject) json.get(key);
            assertTrue(JSONUtils.isEquivalent(jObj,retrievedObj));
        } catch (JSONException e) {
            fail("Unable to parse incoming json.");
        } catch (JsonProcessingException e) {
            fail("Unable to convert response to json.");
        }
    }

    //Tests for invalid return paths

    @Test
    public void testBasicErrorResponseNoObject(){
        InternalResponseObject<JSONObject> obj = new InternalResponseObject<>(InternalErrorCode.INVALID_USERNAME);
        assertFalse(obj.isNormal());
        assertFalse(obj.isPresent());
        assertNotNull(obj.getMessage());
        assertEquals(InternalErrorCode.INVALID_USERNAME.message, obj.getMessage());
        try{
            JSONObject json = new JSONObject(mapper.writeValueAsString(obj));
            assertEquals(2,JSONObject.getNames(json).length);
            assertTrue(json.has("status"));
            assertEquals(422,json.getInt("status"));
            assertTrue(json.has("message"));
            assertEquals(InternalErrorCode.INVALID_USERNAME.message,json.getString("message"));
        } catch (JSONException e) {
            fail("Unable to parse incoming json.");
        } catch (JsonProcessingException e) {
            fail("Unable to convert response to json.");
        }
    }

    @Test
    public void testComplexErrorResponseNoObject(){
        InternalResponseObject<JSONObject> obj = new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA,InternalErrorCode.INVALID_USERNAME);
        assertFalse(obj.isNormal());
        assertFalse(obj.isPresent());
        assertNotNull(obj.getMessage());
        try{
            JSONObject json = new JSONObject(mapper.writeValueAsString(obj));
            assertEquals(2,JSONObject.getNames(json).length);
            assertTrue(json.has("status"));
            assertEquals(WebStatusCode.UNPROCESSABLE_DATA.code,json.getInt("status"));
            assertTrue(json.has("message"));
            assertEquals(InternalErrorCode.INVALID_USERNAME.message,json.getString("message"));
        } catch (JSONException e) {
            fail("Unable to parse incoming json.");
        } catch (JsonProcessingException e) {
            fail("Unable to convert response to json.");
        }
    }

    @Test
    public void testComplexErrorResponseNoObjectCustomMessage(){
        String message = "This is a custom messaage";
        InternalResponseObject<JSONObject> obj = new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA,InternalErrorCode.INVALID_USERNAME,message);
        assertFalse(obj.isNormal());
        assertFalse(obj.isPresent());
        assertNotNull(obj.getMessage());
        try{
            JSONObject json = new JSONObject(mapper.writeValueAsString(obj));
            assertEquals(2,JSONObject.getNames(json).length);
            assertTrue(json.has("status"));
            assertEquals(WebStatusCode.UNPROCESSABLE_DATA.code,json.getInt("status"));
            assertTrue(json.has("message"));
            assertEquals(message,json.getString("message"));
        } catch (JSONException e) {
            fail("Unable to parse incoming json.");
        } catch (JsonProcessingException e) {
            fail("Unable to convert response to json.");
        }
    }

    @Test
    public void testWebStatusOnlyResponse(){
        InternalResponseObject<JSONObject> obj = new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA);
        assertFalse(obj.isNormal());
        assertFalse(obj.isPresent());
        assertNotNull(obj.getMessage());
        try{
            JSONObject json = new JSONObject(mapper.writeValueAsString(obj));
            assertEquals(2,JSONObject.getNames(json).length);
            assertTrue(json.has("status"));
            assertEquals(WebStatusCode.UNPROCESSABLE_DATA.code,json.getInt("status"));
            assertTrue(json.has("message"));
            assertEquals(WebStatusCode.UNPROCESSABLE_DATA.message,json.getString("message"));
        } catch (JSONException e) {
            fail("Unable to parse incoming json.");
        } catch (JsonProcessingException e) {
            fail("Unable to convert response to json.");
        }
    }

    @Test
    public void testInternalErrorCodeAndCustomMessage(){
        String message = "This is a really custom message.";
        InternalResponseObject<JSONObject> obj = new InternalResponseObject<>(InternalErrorCode.INVALID_USERNAME,message);
        assertFalse(obj.isNormal());
        assertFalse(obj.isPresent());
        assertNotNull(obj.getMessage());
        try{
            JSONObject json = new JSONObject(mapper.writeValueAsString(obj));
            assertEquals(2,JSONObject.getNames(json).length);
            assertTrue(json.has("status"));
            assertEquals(WebStatusCode.SERVER_ERROR.code,json.getInt("status"));
            assertTrue(json.has("message"));
            assertEquals(message,json.getString("message"));
        } catch (JSONException e) {
            fail("Unable to parse incoming json.");
        } catch (JsonProcessingException e) {
            fail("Unable to convert response to json.");
        }
    }
}
