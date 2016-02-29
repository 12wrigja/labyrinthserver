package edu.cwru.eecs395_s16.interfaces;

import edu.cwru.eecs395_s16.core.JsonableException;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 1/21/16.
 */
public class Response implements Jsonable {

    protected WebStatusCode status;
    protected String message;

    public Response() {
        this.status = WebStatusCode.OK;
        this.message = WebStatusCode.OK.message;
    }

    public Response(JsonableException e) {
        this(e.getErrorCode(),e.getMessage());
    }

    public Response(WebStatusCode code) {
        this(code,code.message);
    }

    public Response(WebStatusCode code, String message){
        this.status = code;
        this.message = message;
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject repr = new JSONObject();
        try{
            repr.put("status",this.status.code);
            if(this.message != null) {
                repr.put("message", this.message);
            }
        } catch (JSONException e) {
            //Should not happen - keys are not null
        }
        return repr;
    }

    public WebStatusCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
