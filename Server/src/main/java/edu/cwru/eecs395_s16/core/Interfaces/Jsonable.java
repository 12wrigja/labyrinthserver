package edu.cwru.eecs395_s16.core.Interfaces;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

/**
 * Created by james on 1/21/16.
 */
@JsonSerialize(using = JsonableSerializer.class)
public interface Jsonable {
    Map<String, Object> getJsonableRepresentation();
}
