package edu.cwru.eecs395_s16.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2/22/16.
 */
public class JSONDiff {

    private JSONObject removed;
    private JSONObject added;
    private JSONObject changed;

    public JSONDiff(JSONObject removed, JSONObject added, JSONObject changed) {
        this.removed = removed;
        this.added = added;
        this.changed = changed;
    }

    public JSONObject getRemoved() {
        try {
            return new JSONObject(removed.toString());
        } catch (JSONException e) {
            //This should never happen - the json is being generated from the library itself.
            return new JSONObject();
        }
    }

    public JSONObject getAdded() {
        try {
            return new JSONObject(added.toString());
        } catch (JSONException e) {
            //This should never happen - the json is being generated from the library itself.
            return new JSONObject();
        }
    }

    public JSONObject getChanged() {
        try {
            return new JSONObject(changed.toString());
        } catch (JSONException e) {
            //This should never happen - the json is being generated from the library itself.
            return new JSONObject();
        }
    }

    public boolean hasChanges(){
        //In order for no changes to have occurred, then added, removed, and changed need to be empty
        return removed.length() != 0 || added.length() != 0 || changed.length() != 0;
    }

    public JSONObject asJSONObject(){
        JSONObject temp = new JSONObject();
        try {
            temp.put("added", added);
            temp.put("removed", removed);
            temp.put("changed", changed);

        }catch(JSONException e){
            //This should never be triggered as the keys are all non-null
        }
        return temp;
    }

    public String toString(){
        return asJSONObject().toString();
    }
}
