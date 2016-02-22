package edu.cwru.eecs395_s16.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by james on 2/22/16.
 */
public class JSONUtils {

    public static final String REMOVED = "REMOVED";

    public static JSONDiff getDiff(JSONObject oldObj, JSONObject newObj) {
        JSONObject added = new JSONObject();
        JSONObject changed = new JSONObject();
        JSONObject removed = new JSONObject();
        Iterator it = newObj.keys();
        while (it.hasNext()) {
            try {
                String key = (String) it.next();

                //Check if this is a newly added key
                if (!oldObj.has(key)) {
                    added.put(key, newObj.get(key));
                } else {
                    //This key has existed before. Let's diff it.
                    Object oldVal = oldObj.get(key);
                    Object newVal = newObj.get(key);

                    //Check and see if they are actually different
                    if (!oldVal.equals(newVal)) {
                        //Check and see if they are different types entirely.
                        //If so, overwrite the old one with the new one
                        if (isJSONTypeMismatch(oldVal, newVal)) {
                            changed.put(key, newVal);
                        } else {
                            //They are the same type.
                            //Check and see if we need to do further inspection
                            if (oldVal instanceof JSONObject && newVal instanceof JSONObject) {
                                //Do recursive diff comparison here
                                JSONDiff subDiff = getDiff((JSONObject) oldVal, (JSONObject) newVal);
                                if (subDiff.hasChanges()) {
                                    if (subDiff.getAdded().length() > 0) {
                                        added.put(key, subDiff.getAdded());
                                    }
                                    if (subDiff.getChanged().length() > 0) {
                                        changed.put(key, subDiff.getChanged());
                                    }
                                    if (subDiff.getRemoved().length() > 0) {
                                        removed.put(key, subDiff.getRemoved());
                                    }
                                }
                            } else if (oldVal instanceof JSONArray && newVal instanceof JSONArray) {
                                //TODO implement array comparison and diffing here
                            } else {
                                //It's a primitive here, so lets just replace the old value with the new one
                                changed.put(key, newVal);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Iterator oldKeyIterator = oldObj.keys();
        while (oldKeyIterator.hasNext()) {
            String key = (String) oldKeyIterator.next();
            if (!newObj.has(key)) {
                //Add the key to the structure of deleted keys.
                try {
                    removed.put(key, REMOVED);
                } catch (JSONException e) {
                    //Do nothing - will never occur as the key will never be null
                }
            }
        }
        return new JSONDiff(removed, added, changed);
    }

    public static JSONObject patch(JSONObject current, JSONDiff diff) throws JSONException {
        JSONObject patched = new JSONObject(current.toString());
        //First deal with removals
        JSONObject removals = diff.getRemoved();
        JSONObject additions = diff.getAdded();
        JSONObject changes = diff.getChanged();
        patched = doRemovals(patched, removals);
        patched = doAdditions(patched, additions);
        patched = doChanges(patched,changes);
        return patched;
    }

    private static JSONObject doRemovals(JSONObject current, JSONObject removals) throws JSONException {
        Iterator removalIterator = removals.keys();
        while (removalIterator.hasNext()) {
            String key = (String) removalIterator.next();
            Object value = removals.get(key);
            if (value.equals(REMOVED)) {
                current.remove(key);
            } else if (value instanceof JSONObject) {
                Object temp = current.opt(key);
                if (temp != null && temp instanceof JSONObject) {
                    JSONObject nestedSection = (JSONObject) temp;
                    JSONObject nestedRemovals = (JSONObject) value;
                    JSONObject removed = doRemovals(nestedSection, nestedRemovals);
                    try {
                        current.put(key, removed);
                    } catch (JSONException e) {
                        //Do nothing - should never be called as the key is known to be non-null
                    }
                }
            } else {
                throw new JSONException("Invalid removal key value. All removal keys should have the value of REMOVED.");
            }
        }
        return current;
    }

    //TODO add in support for list additions
    private static JSONObject doAdditions(JSONObject current, JSONObject additions) throws JSONException {
        Iterator additionIterator = additions.keys();
        while (additionIterator.hasNext()) {
            String key = (String) additionIterator.next();
            Object value = additions.get(key);
            if (value instanceof JSONObject) {
                Object innerObj = current.opt(key);
                if (innerObj == null) {
                    innerObj = new JSONObject();
                } else if (!(innerObj instanceof JSONObject)) {
                    throw new JSONException("Trying to add an sub-key to a key that is not an object!");
                }
                JSONObject added = doAdditions((JSONObject) innerObj, (JSONObject) value);
                current.put(key, added);
            } else {
                Object innerObj = current.opt(key);
                if (innerObj == null) {
                    current.put(key, value);
                } else {
                    throw new JSONException("The provided key is already assigned and cannot be changed in an addition!");
                }
            }
        }
        return current;
    }

    private static JSONObject doChanges(JSONObject current, JSONObject changes) throws JSONException {
        Iterator changesIterator = changes.keys();
        while (changesIterator.hasNext()) {
            String key = (String) changesIterator.next();
            Object value = changes.get(key);
            Object currentVal = current.opt(key);
            if(currentVal != null){
                if(value instanceof JSONObject){
                    if(!(currentVal instanceof JSONObject)){
                        throw new JSONException("Trying to change a sub-key on a key that is not an object!");
                    } else {
                        JSONObject innerObj = (JSONObject)currentVal;
                        JSONObject innerChanges = (JSONObject)value;
                        current.put(key,doChanges(innerObj,innerChanges));
                    }
                } else {
                    current.put(key,value);
                }
            } else {
                throw new JSONException("Trying to change a key that doesn't exist!");
            }
        }
        return current;
    }

    public static boolean isJSONTypeMismatch(Object obj1, Object obj2) {
        return !obj1.getClass().equals(obj2.getClass());
    }

    public static boolean isEquivalent(JSONObject obj1, JSONObject obj2) {
        JSONDiff diff = getDiff(obj1, obj2);
        return !diff.hasChanges();
    }
}
