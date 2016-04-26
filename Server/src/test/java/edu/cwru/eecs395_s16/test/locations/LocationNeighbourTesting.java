package edu.cwru.eecs395_s16.test.locations;

import edu.cwru.eecs395_s16.core.objects.Location;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by james on 2/21/16.
 */
public class LocationNeighbourTesting {

    @Test
    public void testDefinitelyNotNeighbours() {
        Location center = new Location(1, 1);
        Location farAway = new Location(5, 5);
        assertFalse(center.isNeighbourOf(farAway, true));
        assertFalse(center.isNeighbourOf(farAway, false));
    }

    @Test
    public void testNonDiagonalNeighbors() {
        Location center = new Location(1, 1);
        Location north = new Location(1, 2);
        assertTrue(center.isNeighbourOf(north, false));
        Location south = new Location(1, 0);
        assertTrue(center.isNeighbourOf(south, false));
        Location east = new Location(2, 1);
        assertTrue(center.isNeighbourOf(east, false));
        Location west = new Location(0, 1);
        assertTrue(center.isNeighbourOf(west, false));

        Location northWest = new Location(0, 2);
        assertFalse(center.isNeighbourOf(northWest, false));
        Location northEast = new Location(2, 2);
        assertFalse(center.isNeighbourOf(northEast, false));
        Location southWest = new Location(0, 0);
        assertFalse(center.isNeighbourOf(southWest, false));
        Location southEast = new Location(2, 0);
        assertFalse(center.isNeighbourOf(southEast, false));
    }

    @Test
    public void testDiagonalNeighbours() {
        Location center = new Location(1, 1);
        Location north = new Location(1, 2);
        assertTrue(center.isNeighbourOf(north, true));
        Location south = new Location(1, 0);
        assertTrue(center.isNeighbourOf(south, true));
        Location east = new Location(2, 1);
        assertTrue(center.isNeighbourOf(east, true));
        Location west = new Location(0, 1);
        assertTrue(center.isNeighbourOf(west, true));

        Location northWest = new Location(0, 2);
        assertTrue(center.isNeighbourOf(northWest, true));
        Location northEast = new Location(2, 2);
        assertTrue(center.isNeighbourOf(northEast, true));
        Location southWest = new Location(0, 0);
        assertTrue(center.isNeighbourOf(southWest, true));
        Location southEast = new Location(2, 0);
        assertTrue(center.isNeighbourOf(southEast, true));
    }
}
