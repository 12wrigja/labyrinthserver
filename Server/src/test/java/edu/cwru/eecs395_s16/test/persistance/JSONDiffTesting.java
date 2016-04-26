package edu.cwru.eecs395_s16.test.persistance;

import edu.cwru.eecs395_s16.utils.JSONDiff;
import edu.cwru.eecs395_s16.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by james on 2/22/16.
 */
public class JSONDiffTesting {

    @Test
    public void testBasicJSONDiffIntegers() throws JSONException {
        JSONObject obj1 = new JSONObject("{\"x\":5}");
        JSONObject obj2 = new JSONObject("{\"x\":7}");
        JSONDiff diff = JSONUtils.getDiff(obj1, obj2);
        assertEquals(0, diff.getAdded().length());
        assertEquals(0, diff.getRemoved().length());
        assertEquals(1, diff.getChanged().length());
        assertTrue(diff.getChanged().has("x"));
        assertEquals(7, diff.getChanged().get("x"));
    }

    @Test
    public void testBasicJSONDiffStrings() throws JSONException {
        JSONObject obj1 = new JSONObject("{\"x\":\"hello\"}");
        JSONObject obj2 = new JSONObject("{\"x\":\"world\"}");
        JSONDiff diff = JSONUtils.getDiff(obj1, obj2);
        assertEquals(0, diff.getAdded().length());
        assertEquals(0, diff.getRemoved().length());
        assertEquals(1, diff.getChanged().length());
        assertTrue(diff.getChanged().has("x"));
        assertEquals("world", diff.getChanged().get("x"));
    }

    @Test
    public void testNestedObjectDiff() throws JSONException {
        JSONObject obj1 = new JSONObject("{\"x\":{\"y\":\"z\"}}");
        JSONObject obj2 = new JSONObject("{\"x\":{\"y\":\"v\"}}");
        JSONDiff diff = JSONUtils.getDiff(obj1, obj2);
        assertEquals(0, diff.getAdded().length());
        assertEquals(0, diff.getRemoved().length());
        assertEquals(1, diff.getChanged().length());
        assertTrue(diff.getChanged().has("x"));
        assertTrue(diff.getChanged().get("x") instanceof JSONObject);
        JSONObject xObj = (JSONObject) diff.getChanged().get("x");
        assertEquals(1, xObj.length());
        assertTrue(xObj.has("y"));
        assertEquals("v", xObj.get("y"));
    }

    @Test
    public void testJSONObjectAddition() throws JSONException {
        JSONObject obj1 = new JSONObject("{\"x\": 7}");
        JSONObject obj2 = new JSONObject("{\"x\": 7,\"y\":6}");
        JSONObject expectedAddition = new JSONObject("{\"y\":6}");
        JSONDiff actualDiff = JSONUtils.getDiff(obj1, obj2);
        assertTrue(actualDiff.hasChanges());
        assertEquals(0, actualDiff.getChanged().length());
        assertEquals(0, actualDiff.getRemoved().length());
        assertEquals(1, actualDiff.getAdded().length());
        assertJSONEquals(expectedAddition, actualDiff.getAdded());
    }

    @Test
    public void testJSONObjectRemoval() throws JSONException {
        JSONObject obj1 = new JSONObject("{\"x\": 7,\"y\":6}");
        JSONObject obj2 = new JSONObject("{\"x\": 7}");
        JSONObject expectedDeletion = new JSONObject("{\"y\":REMOVED}");
        JSONDiff actualDiff = JSONUtils.getDiff(obj1, obj2);
        assertEquals(0, actualDiff.getChanged().length());
        assertEquals(0, actualDiff.getAdded().length());
        assertEquals(1, actualDiff.getRemoved().length());
        assertJSONEquals(expectedDeletion, actualDiff.getRemoved());
    }

    @Test
    public void testEquivalency() throws JSONException {
        JSONObject obj1 = new JSONObject("{ \"_id\": \"56cb4903b342343cf23169b3\", \"index\": 0, \"guid\": " +
                "\"ad534049-1741-4ac6-84ae-0d6ad0220ef0\", \"isActive\": true, \"physical\": { \"age\": 33, " +
                "\"eyeColor\": \"brown\" }, \"balance\": \"$1,386.23\", \"contact\": { \"email\": " +
                "\"bartlettbond@zillanet.com\", \"phone\": \"+1 (907) 488-3623\", \"address\": \"178 Ditmars Street, " +
                "Nescatunga, Texas, 3538\" }, \"name\": \"Hammond Palmer\", \"gender\": \"male\", \"company\": " +
                "\"VELITY\", \"about\": \"Enim ea sunt est irure laboris elit adipisicing eu fugiat elit. Ut Lorem " +
                "proident eiusmod consequat ea elit occaecat dolor sit sit ut. Labore qui nostrud nulla amet amet " +
                "aliquip labore id minim laborum occaecat. Pariatur velit aute cupidatat ullamco aliqua irure " +
                "pariatur veniam velit cupidatat esse. Veniam ullamco esse voluptate non eiusmod non. Pariatur " +
                "deserunt ea ea commodo reprehenderit ex reprehenderit in irure dolore voluptate.\\r\\n\", " +
                "\"latitude\": -47.901861, \"longitude\": -103.258658, \"greeting\": \"Hello, Hammond Palmer! You " +
                "have 9 unread messages.\", \"favoriteFruit\": \"apple\" }");
        JSONObject obj2 = new JSONObject("{ \"_id\": \"56cb4903b342343cf23169b3\", \"index\": 0, \"guid\": " +
                "\"ad534049-1741-4ac6-84ae-0d6ad0220ef0\", \"isActive\": true, \"physical\": { \"eyeColor\": " +
                "\"brown\", \"age\": 33\n" +
                "\n" +
                " }, \"balance\": \"$1,386.23\", \"contact\": { \"address\": \"178 Ditmars Street, Nescatunga, Texas," +
                " 3538\", \"email\": \"bartlettbond@zillanet.com\", \"phone\": \"+1 (907) 488-3623\" }, \"name\": " +
                "\"Hammond Palmer\", \"gender\": \"male\", \"company\": \"VELITY\", \"latitude\": -47.901861, " +
                "\"about\": \"Enim ea sunt est irure laboris elit adipisicing eu fugiat elit. Ut Lorem proident " +
                "eiusmod consequat ea elit occaecat dolor sit sit ut. Labore qui nostrud nulla amet amet aliquip " +
                "labore id minim laborum occaecat. Pariatur velit aute cupidatat ullamco aliqua irure pariatur veniam" +
                " velit cupidatat esse. Veniam ullamco esse voluptate non eiusmod non. Pariatur deserunt ea ea " +
                "commodo reprehenderit ex reprehenderit in irure dolore voluptate.\\r\\n\", \"longitude\": -103" +
                ".258658, \"greeting\": \"Hello, Hammond Palmer! You have 9 unread messages.\", \"favoriteFruit\": " +
                "\"apple\" }");
        assertJSONEquals(obj1, obj2);
    }

    @Test
    public void testComplexRemovalsAdditionsChanged() throws JSONException {
        JSONObject originalObject = new JSONObject("{ \"_id\": \"56cb67fc7aff6dfa27e9f844\", \"index\": 0, \"guid\": " +
                "\"cd8bd9af-3769-4273-a181-9fd2cd27d500\", \"isActive\": false, \"physical\": { \"age\": 31, " +
                "\"eyeColor\": \"brown\" }, \"balance\": \"$3,295.30\", \"contact\": { \"email\": " +
                "\"valeriariley@qaboos.com\", \"phone\": \"+1 (945) 438-2381\", \"address\": \"637 Seeley Street, " +
                "Kohatk, Kentucky, 279\" }, \"name\": \"Benton Baxter\", \"gender\": \"male\", \"company\": " +
                "\"SURETECH\", \"about\": \"In dolor cupidatat laborum enim. Et ullamco laboris enim velit velit sit " +
                "qui sit ex velit. Commodo do ullamco exercitation duis ut et. Anim labore excepteur enim laboris. Id" +
                " labore consequat aute ut non id labore nisi velit enim reprehenderit quis.\\r\\n\", \"latitude\": " +
                "-35.538156, \"longitude\": 95.072343, \"greeting\": \"Hello, Benton Baxter! You have 1 unread " +
                "messages.\", \"favoriteFruit\": \"banana\" }");
        JSONObject changedObject = new JSONObject("{ \"_id\": \"56cb67fc7aff6dfa27e9f844\", \"index\": 0, \"guid\": " +
                "\"cd8bd9af-3769-4273-a181-9fd2cd27d500\", \"isActive\": false, \"physical\": { \"age\": 31, " +
                "\"height\": 50 }, \"balance\": \"$3,295.30\", \"contact\": { \"email\": \"valeriariley@qaboos.com\"," +
                " \"address\": \"637 Seeley Street, Kohatk, Kentucky, 279\", \"cell\": { \"home\": 500, \"work\": 600" +
                " } }, \"name\": \"Benton Baxter\", \"gender\": \"male\", \"company\": \"SURETECH\", \"about\": \"In " +
                "dolor cupidatat laborum enim. Et ullamco laboris enim velit velit sit qui sit ex velit. Commodo do " +
                "ullamco exercitation duis ut et. Anim labore excepteur enim laboris. Id labore consequat aute ut non" +
                " id labore nisi velit enim reprehenderit quis.\\r\\n\", \"latitude\": -35.538156, \"longitude\": " +
                "100, \"greeting\": \"Hello, Benton Baxter! You have 1 unread messages.\", \"favoriteFruit\": " +
                "\"banana\" }");
        JSONObject expectedRemovals = new JSONObject("{ \"physical\": { \"eyeColor\": \"REMOVED\" }, \"contact\": { " +
                "\"phone\": \"REMOVED\" } }");
        JSONObject expectedAdditions = new JSONObject("{ \"physical\": { \"height\": 50 }, \"contact\": { \"cell\": {" +
                " \"home\":500, \"work\":600 } } }");
        JSONObject expectedChanges = new JSONObject("{\"longitude\":100}");
        JSONDiff diff = JSONUtils.getDiff(originalObject, changedObject);
        assertJSONEquals(expectedRemovals, diff.getRemoved());
        assertJSONEquals(expectedAdditions, diff.getAdded());
        assertJSONEquals(expectedChanges, diff.getChanged());
    }

    @Test
    public void testJSONPatchBasicRemoving() throws JSONException {
        JSONObject obj1 = new JSONObject("{\"x\": 7,\"y\":6}");
        JSONObject obj2 = new JSONObject("{\"x\": 7}");
        JSONDiff diff = JSONUtils.getDiff(obj1, obj2);
        JSONObject patched = JSONUtils.patch(obj1, diff);
        assertJSONEquals(obj2, patched);
    }

    @Test
    public void testJSONPatchNestedRemoving() throws JSONException {
        //Test 1: remove all of a inner variable
        JSONObject obj1 = new JSONObject("{\"x\":{\"y\":\"z\"}}");
        JSONObject obj2 = new JSONObject("{\"x\":{}}");
        JSONDiff diff = JSONUtils.getDiff(obj1, obj2);
        JSONObject patched = JSONUtils.patch(obj1, diff);
        assertJSONEquals(obj2, patched);

        //Test 2: remove part of an inner variable
    }

    @Test
    public void testJSONPatchDoubleNestedRemoving() throws JSONException {
        JSONObject obj1 = new JSONObject("{\"x\":{\"y\":{\"z\":\"hello!\"}}}");
        JSONObject obj2 = new JSONObject("{\"x\":{\"y\":{}}}");
        JSONDiff diff = JSONUtils.getDiff(obj1, obj2);
        JSONObject patched = JSONUtils.patch(obj1, diff);
        assertJSONEquals(obj2, patched);
    }

    @Test
    public void testJSONPatchAddition() throws JSONException {
        JSONObject obj1 = new JSONObject("{\"x\":{\"y\":{}}}");
        JSONObject obj2 = new JSONObject("{\"x\":{\"y\":{\"z\":\"hello!\"}},\"y\":\"world!\"}");
        JSONDiff diff = JSONUtils.getDiff(obj1, obj2);
        JSONObject patched = JSONUtils.patch(obj1, diff);
        assertJSONEquals(obj2, patched);
    }

    @Test
    public void testJSONPatchNestedObjectChanges() throws JSONException {
        JSONObject obj1 = new JSONObject("{\"x\":{\"y\":\"z\"},\"b\":\"c\"}");
        JSONObject obj2 = new JSONObject("{\"x\":{\"y\":\"v\"},\"b\":\"d\"}");
        JSONDiff diff = JSONUtils.getDiff(obj1, obj2);
        JSONObject patched = JSONUtils.patch(obj1, diff);
        assertJSONEquals(obj2, patched);
    }

    @Test
    public void testJSONPatchComplexExample() throws JSONException {
        JSONObject originalObject = new JSONObject("{ \"_id\": \"56cb67fc7aff6dfa27e9f844\", \"index\": 0, \"guid\": " +
                "\"cd8bd9af-3769-4273-a181-9fd2cd27d500\", \"isActive\": false, \"physical\": { \"age\": 31, " +
                "\"eyeColor\": \"brown\" }, \"balance\": \"$3,295.30\", \"contact\": { \"email\": " +
                "\"valeriariley@qaboos.com\", \"phone\": \"+1 (945) 438-2381\", \"address\": \"637 Seeley Street, " +
                "Kohatk, Kentucky, 279\" }, \"name\": \"Benton Baxter\", \"gender\": \"male\", \"company\": " +
                "\"SURETECH\", \"about\": \"In dolor cupidatat laborum enim. Et ullamco laboris enim velit velit sit " +
                "qui sit ex velit. Commodo do ullamco exercitation duis ut et. Anim labore excepteur enim laboris. Id" +
                " labore consequat aute ut non id labore nisi velit enim reprehenderit quis.\\r\\n\", \"latitude\": " +
                "-35.538156, \"longitude\": 95.072343, \"greeting\": \"Hello, Benton Baxter! You have 1 unread " +
                "messages.\", \"favoriteFruit\": \"banana\" }");
        JSONObject changedObject = new JSONObject("{ \"_id\": \"56cb67fc7aff6dfa27e9f844\", \"index\": 0, \"guid\": " +
                "\"cd8bd9af-3769-4273-a181-9fd2cd27d500\", \"isActive\": false, \"physical\": { \"age\": 31, " +
                "\"height\": 50 }, \"balance\": \"$3,295.30\", \"contact\": { \"email\": \"valeriariley@qaboos.com\"," +
                " \"address\": \"637 Seeley Street, Kohatk, Kentucky, 279\", \"cell\": { \"home\": 500, \"work\": 600" +
                " } }, \"name\": \"Benton Baxter\", \"gender\": \"male\", \"company\": \"SURETECH\", \"about\": \"In " +
                "dolor cupidatat laborum enim. Et ullamco laboris enim velit velit sit qui sit ex velit. Commodo do " +
                "ullamco exercitation duis ut et. Anim labore excepteur enim laboris. Id labore consequat aute ut non" +
                " id labore nisi velit enim reprehenderit quis.\\r\\n\", \"latitude\": -35.538156, \"longitude\": " +
                "100, \"greeting\": \"Hello, Benton Baxter! You have 1 unread messages.\", \"favoriteFruit\": " +
                "\"banana\" }");
        JSONDiff diff = JSONUtils.getDiff(originalObject, changedObject);
        JSONObject patched = JSONUtils.patch(originalObject, diff);
        assertJSONEquals(changedObject, patched);
    }

    @Test
    public void testJSONPrimitiveTypeMismatching() {
        //Declare all types
        String val1 = "val";
        Integer val2 = 5;
        Float val3 = 5.4f;
        Double val4 = 5.4;
        JSONObject val5 = null;
        try {
            val5 = new JSONObject("{\"x\":5}");
        } catch (JSONException e) {
            fail("Incorrectly specified json object.");
        }
        JSONArray val6 = null;
        try {
            val6 = new JSONArray("[{\"x\":5}]");
        } catch (JSONException e) {
            fail("Incorrectly specified json array");
        }

        //Test for type equality

        //String - String
        assertFalse(JSONUtils.isJSONTypeMismatch(val1, val1));
        assertFalse(JSONUtils.isJSONTypeMismatch(val2, val2));
        assertFalse(JSONUtils.isJSONTypeMismatch(val3, val3));
        assertFalse(JSONUtils.isJSONTypeMismatch(val4, val4));
        assertFalse(JSONUtils.isJSONTypeMismatch(val5, val5));
        assertFalse(JSONUtils.isJSONTypeMismatch(val6, val6));

        //Compare against string
        assertTrue(JSONUtils.isJSONTypeMismatch(val1, val2));
        assertTrue(JSONUtils.isJSONTypeMismatch(val1, val3));
        assertTrue(JSONUtils.isJSONTypeMismatch(val1, val4));
        assertTrue(JSONUtils.isJSONTypeMismatch(val1, val5));
        assertTrue(JSONUtils.isJSONTypeMismatch(val1, val6));

        //Compare against Integer
        assertTrue(JSONUtils.isJSONTypeMismatch(val2, val1));
        assertTrue(JSONUtils.isJSONTypeMismatch(val2, val3));
        assertTrue(JSONUtils.isJSONTypeMismatch(val2, val4));
        assertTrue(JSONUtils.isJSONTypeMismatch(val2, val5));
        assertTrue(JSONUtils.isJSONTypeMismatch(val2, val6));

        //Compare against Float
        assertTrue(JSONUtils.isJSONTypeMismatch(val3, val1));
        assertTrue(JSONUtils.isJSONTypeMismatch(val3, val2));
        assertTrue(JSONUtils.isJSONTypeMismatch(val3, val4));
        assertTrue(JSONUtils.isJSONTypeMismatch(val3, val5));
        assertTrue(JSONUtils.isJSONTypeMismatch(val3, val6));

        //Compare against Double
        assertTrue(JSONUtils.isJSONTypeMismatch(val4, val1));
        assertTrue(JSONUtils.isJSONTypeMismatch(val4, val1));
        assertTrue(JSONUtils.isJSONTypeMismatch(val4, val1));
        assertTrue(JSONUtils.isJSONTypeMismatch(val4, val1));
        assertTrue(JSONUtils.isJSONTypeMismatch(val4, val1));

        //Compare against JSONObject
        assertTrue(JSONUtils.isJSONTypeMismatch(val5, val1));
        assertTrue(JSONUtils.isJSONTypeMismatch(val5, val2));
        assertTrue(JSONUtils.isJSONTypeMismatch(val5, val3));
        assertTrue(JSONUtils.isJSONTypeMismatch(val5, val4));
        assertTrue(JSONUtils.isJSONTypeMismatch(val5, val6));

        //Compare against JSONArray
        assertTrue(JSONUtils.isJSONTypeMismatch(val6, val1));
        assertTrue(JSONUtils.isJSONTypeMismatch(val6, val2));
        assertTrue(JSONUtils.isJSONTypeMismatch(val6, val3));
        assertTrue(JSONUtils.isJSONTypeMismatch(val6, val4));
        assertTrue(JSONUtils.isJSONTypeMismatch(val6, val5));
    }

    private void assertJSONEquals(JSONObject obj1, JSONObject obj2) {
        assertTrue(String.format("%s is not equivalent to %s", obj1.toString(), obj2.toString()), JSONUtils
                .isEquivalent(obj1, obj2));
    }

}
