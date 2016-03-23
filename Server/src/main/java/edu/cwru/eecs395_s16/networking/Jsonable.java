package edu.cwru.eecs395_s16.networking;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.cwru.eecs395_s16.core.JsonableSerializer;
import org.json.JSONObject;

/**
 * Created by james on 1/21/16.
 */
@JsonSerialize(using = JsonableSerializer.class)
public interface Jsonable {
    JSONObject getJSONRepresentation();
}
