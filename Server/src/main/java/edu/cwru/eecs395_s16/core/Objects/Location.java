package edu.cwru.eecs395_s16.core.objects;

import edu.cwru.eecs395_s16.networking.Jsonable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2/12/16.
 */
public class Location implements Jsonable {

    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";

    private int x;
    private int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public int hashCode() {
        int result = getX();
        result = 31 * result + getY();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;

        Location location = (Location) o;

        if (getX() != location.getX()) return false;
        return getY() == location.getY();

    }

    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public boolean isNeighbourOf(Location other, boolean allowDiagonalNeighbours) {
        if (allowDiagonalNeighbours) {
            return Math.abs(this.getX() - other.getX()) == 1 || Math.abs(this.getY() - other.getY()) == 1;
        } else {
            return Math.abs(this.getX() - other.getX()) == 1 ^ Math.abs(this.getY() - other.getY()) == 1;
        }
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("x", getX());
            repr.put("y", getY());
        } catch (JSONException e) {
            //This should never be called because the keys arent null.
        }
        return repr;
    }

    public Location locationRelativeTo(Location otherLocation) {
        return new Location(otherLocation.x - this.x, otherLocation.y - this.y);
    }

}
