package edu.cwru.eecs395_s16.core;

import edu.cwru.eecs395_s16.networking.Jsonable;
import edu.cwru.eecs395_s16.networking.Response;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;

/**
 * While developing this project I needed a way to return failure in nested function calls back to the client. I
 * initially would simply throw a specific kind of exception, but then learned that that is very computationally
 * expensive (especially when unwrapping the stack to log the issue), and set out to find a different solution. Thus
 * was born the InternalResponseObject.
 *
 * This object is a container object, much like an Optional, except there are two primary ways to create one. One way
 * is when a function call succeeds, and this contains the object as well as a WebStatusCode (in case the object
 * needs to be sent to a client). The other way is on "failure" - when something didn't work as expected. This could
 * be something as simple as not being able to log in a client to something as complicated as a deserialization error
 * when working with Matches. If the object is constructed this way, the object is then considered to be "not normal"
 * and signifies that there was an error in a function call. These errors can easily be transmitted back up the call
 * stack using normal returns (so no unwrapping the stack for a stack trace) while retaining a reason for failure -
 * the InternalErrorCode. This allows for error reporting that doesn't bring the program to a halt when something
 * goes wrong.
 *
 * These "not normal" returns can be customized with a message at any time - if something fails for a reason x in a
 * specific layer that could have a different meaning in the code that called it. Changing that is really easy using
 * the cloneError method that is able to clone the InternalErrorCode while allowing you to change the message if
 * necessary to provide a better reason for failure.
 *
 * Take a quick look here and get familiar - these objects are used EVERYWHERE - every service uses these as their
 * return type. The NetworkingInterface uses these as their return type, which is awesome because they are easy to
 * use in tests, and they can automatically convert themselves into JSON to send to clients.
 *
 * The next place to go to is Match.java - there I can start to cover the game aspect of this server.
 */

/**
 * Created by james on 2/28/16.
 */
public class InternalResponseObject<T> extends Response {

    private static final String DEFAULT_OBJECT_KEY = "object";
    private final Optional<T> object;
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
     * Creates a response with the given web status code. No object is included in the response, and the default
     * message for the code is used.
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
        this(WebStatusCode.UNPROCESSABLE_DATA, code, message);
    }

    /**
     * Creates a Unprocessable data error response with the given internal error code, and uses the default message
     * from the error code as the message
     *
     * @param code
     */
    public InternalResponseObject(InternalErrorCode code) {
        this(WebStatusCode.UNPROCESSABLE_DATA, code, code.message);
    }

    /**
     * Creates a response with the given status code and uses the message from the given internal error.
     *
     * @param code
     * @param errorCode
     */
    public InternalResponseObject(WebStatusCode code, InternalErrorCode errorCode) {
        this(code, errorCode, errorCode.message);
    }

    public static <T> InternalResponseObject<T> cloneError(InternalResponseObject<?> original, String message) {
        return new InternalResponseObject<>(original.getStatus(), original.getInternalErrorCode(), message);
    }

    public static <T> InternalResponseObject<T> cloneError(InternalResponseObject<?> original) {
        return new InternalResponseObject<>(original.getStatus(), original.getInternalErrorCode(), original
                .getMessage());
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

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject repr = super.getJSONRepresentation();
        if (isNormal() && object.isPresent() && !objectKey.equals(DEFAULT_OBJECT_KEY)) {
            try {
                T jObj = object.get();
                if (jObj instanceof List<?>) {
                    JSONArray arr = new JSONArray();
                    for (Object obj : (List<?>) jObj) {
                        if (obj instanceof Jsonable) {
                            arr.put(((Jsonable) obj).getJSONRepresentation());
                        } else {
                            arr.put(obj.toString());
                        }
                    }
                    repr.put(objectKey, arr);
                } else {
                    repr.put(objectKey, object.get());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return repr;
    }
}
