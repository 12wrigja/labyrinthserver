package edu.cwru.eecs395_s16.core;

import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;

import java.util.Optional;

/**
 * Created by james on 2/28/16.
 */
public class InternalResponseObject<T> extends Response {

    private Optional<T> object;
    private InternalErrorCode internalErrorCode;

    public InternalResponseObject(WebStatusCode code, String message, T object) {
        super(code, message);
        if(object != null) {
            this.object = Optional.of(object);
        } else {
            this.object = Optional.empty();
        }
    }

    public InternalResponseObject(WebStatusCode code){
        this(code, code.message, null);
    }

    public InternalResponseObject(WebStatusCode code, InternalErrorCode errorCode, String message, T object){
        this(code,message,object);
        this.internalErrorCode = errorCode;
    }

    public InternalResponseObject(InternalErrorCode code, String message){
        this(WebStatusCode.UNPROCESSABLE_DATA,message, null);
        this.internalErrorCode = code;
    }

    public InternalResponseObject(InternalErrorCode code){
        this(WebStatusCode.UNPROCESSABLE_DATA,code.message,null);
        this.internalErrorCode = code;
    }

    public InternalResponseObject(WebStatusCode code, T object) {
        this(code, code.message, object);
    }

    public InternalResponseObject(T object) {
        this(WebStatusCode.OK, WebStatusCode.OK.message,object);
    }

    public T get() {
        return object.get();
    }

    public boolean isPresent() {
        return object.isPresent();
    }

    public InternalErrorCode getInternalErrorCode() {
        return internalErrorCode;
    }

    public boolean isNormal(){
        return this.status.equals(WebStatusCode.OK);
    }
}
