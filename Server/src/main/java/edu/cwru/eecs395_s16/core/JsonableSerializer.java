package edu.cwru.eecs395_s16.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import edu.cwru.eecs395_s16.networking.Jsonable;

import java.io.IOException;

/**
 * Created by james on 2/12/16.
 */
public class JsonableSerializer extends JsonSerializer<Jsonable> {
    @Override
    public void serialize(Jsonable value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(value.getJSONRepresentation());
    }
}
