package edu.cwru.eecs395_s16.core;

import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Created by james on 2/28/16.
 */
public class InternalResponseObject<T> extends Response {

    private final Optional<T> object;
    private static final String DEFAULT_OBJECT_KEY = "object";
    private final String objectKey;
    private final Optional<InternalErrorCode> internalErrorCode;

    //Path 1: correct returns

    /**
     * Creates a normal response that contains a status of 200.
     */
    public InternalResponseObject() {
        this(WebStatusCode.OK);
    }

    /**
     * Creates a normal response that contains a status of 200, and the given object under the given key.
     * If the given object is null, then no object is included in the response. If the given key is null,
     * or is empty then the object will not be included in the response.
     *
     * @param object
     * @param key
     */
    public InternalResponseObject(T object, String key) {
        super(WebStatusCode.OK, null);
        this.object = Optional.ofNullable(object);
        if (key == null || key.equals("")) {
            this.objectKey = DEFAULT_OBJECT_KEY;
        } else {
            this.objectKey = key;
        }
        this.internalErrorCode = Optional.empty();
    }

    /**
     * Creates a response with status code 200 and no message. Hides the object from the response.
     *
     * @param object
     */
    public InternalResponseObject(T object) {
        this(object, null);
    }

    //Path 2: Incorrect returns

    /**
     * Creates a response with the given web status code. No object is included in the response, and the default message for the code is used.
     *
     * @param code The status code to use.
     */
    public InternalResponseObject(WebStatusCode code) {
        super(code, code.message);
        this.objectKey = DEFAULT_OBJECT_KEY;
        this.object = Optional.empty();
        this.internalErrorCode = Optional.empty();

    }


    /**
     * Creates a response with the given web status code, internal error code, and message
     *
     * @param code
     * @param errorCode
     * @param message
     */
    public InternalResponseObject(WebStatusCode code, InternalErrorCode errorCode, String message) {
        super(code, message);
        this.internalErrorCode = Optional.of(errorCode);
        this.object = Optional.empty();
        this.objectKey = DEFAULT_OBJECT_KEY;
    }


    /**
     * Creates a 500 error response with the given internal error code and message
     *
     * @param code
     * @param message
     */
    public InternalResponseObject(InternalErrorCode code, String message) {
        this(WebStatusCode.SERVER_ERROR, code, message);
    }

    /**
     * Creates a Unprocessable data error response with the given internal error code, and uses the default message from the error code as the message
     *
     * @param code
     */
    public InternalResponseObject(InternalErrorCode code) {
        this(WebStatusCode.UNPROCESSABLE_DATA, code, code.message);
    }

    /**
     * Creates a response with the given status code and uses the message from the given internal error.
     * @param code
     * @param errorCode
     */
    public InternalResponseObject(WebStatusCode code, InternalErrorCode errorCode) {
        this(code, errorCode, errorCode.message);
    }

    public static <T> InternalResponseObject<T> cloneError(InternalResponseObject<?> original,String message){
        return new InternalResponseObject<>(original.getStatus(),original.getInternalErrorCode(),message);
    }

    public static <T> InternalResponseObject<T> cloneError(InternalResponseObject<?> original){
        return new InternalResponseObject<>(original.getStatus(),original.getInternalErrorCode(),original.getMessage());
    }

    public T get() {
        return object.get();
    }

    public boolean isPresent() {
        return object.isPresent();
    }

    public InternalErrorCode getInternalErrorCode() {
        if (this.internalErrorCode.isPresent()) {
            return this.internalErrorCode.get();
        } else {
            return InternalErrorCode.UNKNOWN;
        }
    }

    public boolean isNormal() {
        return this.status.equals(WebStatusCode.OK);
    }

    public boolean hasObjectKey(){
        return this.objectKey != null;
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject repr = super.getJSONRepresentation();
        if (isNormal() && object.isPresent() && !objectKey.equals(DEFAULT_OBJECT_KEY)) {
            try {
                repr.put(objectKey, object.get());
            } catch (JSONException e) {
                //TODO use logging of sorts.
            }
        }
        return repr;
    }
}
