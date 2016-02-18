package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.interfaces.RequestData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 2/12/16.
 */
public class NewMapRequest implements RequestData {

    private int x;
    private int y;

    @Override
    public void validate() throws InvalidDataException {
        List<String> params = new ArrayList<>();
        if(x <= 0){
            params.add("x");
        }
        if(y <= 0){
            params.add("y");
        }
        if(params.size() > 0){
            throw new InvalidDataException(params);
        }
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }
}
