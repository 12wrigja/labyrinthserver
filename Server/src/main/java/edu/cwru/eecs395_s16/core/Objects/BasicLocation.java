package edu.cwru.eecs395_s16.core.Objects;

import edu.cwru.eecs395_s16.core.Interfaces.Objects.Location;

/**
 * Created by james on 2/12/16.
 */
public class BasicLocation implements Location {

    private int x;
    private int y;

    public BasicLocation(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }
}
