package edu.cwru.eecs395_s16.utils;

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
        return removed;
    }

    public JSONObject getAdded() {
        return added;
    }

    public JSONObject getChanged() {
        return changed;
    }

    public boolean hasChanges(){
        //In order for no changes to have occurred, then added, removed, and changed need to be empty
        return removed.length() != 0 || added.length() != 0 || changed.length() != 0;
    }
}
