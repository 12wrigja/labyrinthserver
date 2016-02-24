package edu.cwru.eecs395_s16.core.objects;

/**
 * Created by james on 2/12/16.
 */
public class Location {

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return getX() == location.getX() && getY() == location.getY();

    }

    @Override
    public int hashCode() {
        int result = getX();
        result = 31 * result + getY();
        return result;
    }

    public boolean isNeighbourOf(Location other, boolean allowDiagonalNeighbours){
        if(allowDiagonalNeighbours){
            return Math.abs(this.getX()-other.getX()) == 1 || Math.abs(this.getY()-other.getY()) == 1;
        } else {
            return Math.abs(this.getX()-other.getX()) == 1 ^ Math.abs(this.getY()-other.getY()) == 1;
        }
    }

}
