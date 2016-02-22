package edu.cwru.eecs395_s16.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.cwru.eecs395_s16.core.JsonableSerializer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by james on 1/21/16.
 */
@JsonSerialize(using = JsonableSerializer.class)
public interface Jsonable {
    JSONObject getJsonableRepresentation();
}
