package edu.cwru.eecs395_s16.test.maps;

import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.interfaces.objects.GameAction;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.services.maprepository.InMemoryMapRepository;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by james on 3/19/16.
 */
public class LineOfSightTests {
    public static GameMap map = new InMemoryMapRepository().getMapByID(0).get();
    public static GameObjectCollection objects = new GameObjectCollection();

    @Test
    public void testBasicLineOfSight() {
        Hero h1 = new HeroBuilder().setLocation(new Location(5, 9)).createHero();
        Hero h2 = new HeroBuilder().setLocation(new Location(8, 9)).createHero();
        objects.add(h1);
        objects.add(h2);
        assertTrue(GameAction.isLineOfSight(h1.getLocation(), h2.getLocation(), map, objects));
    }

    @Test
    public void testPureDiagonalLineOfSight(){
        Hero h1 = new HeroBuilder().setLocation(new Location(5, 6)).createHero();
        Hero h2 = new HeroBuilder().setLocation(new Location(8, 9)).createHero();
        objects.add(h1);
        objects.add(h2);
        assertTrue(GameAction.isLineOfSight(h1.getLocation(), h2.getLocation(), map, objects));
    }

    @Test
    public void testOffDiagonalLineOfSight(){
        Hero h1 = new HeroBuilder().setLocation(new Location(5, 8)).createHero();
        Hero h2 = new HeroBuilder().setLocation(new Location(8, 9)).createHero();
        objects.add(h1);
        objects.add(h2);
        assertTrue(GameAction.isLineOfSight(h1.getLocation(), h2.getLocation(), map, objects));
    }

    @Test
    public void testNotLineOfSight(){
        Hero h1 = new HeroBuilder().setLocation(new Location(7, 7)).createHero();
        Hero h2 = new HeroBuilder().setLocation(new Location(8, 9)).createHero();
        objects.add(h1);
        objects.add(h2);
        assertFalse(GameAction.isLineOfSight(h1.getLocation(), h2.getLocation(), map, objects));
    }

    @Test
    public void testStartNotLineOfSight(){
        Hero h1 = new HeroBuilder().setLocation(new Location(0, 0)).createHero();
        Hero h2 = new HeroBuilder().setLocation(new Location(8, 9)).createHero();
        objects.add(h1);
        objects.add(h2);
        assertFalse(GameAction.isLineOfSight(h1.getLocation(), h2.getLocation(), map, objects));
    }

    @Test
    public void testReversedLineOfSight(){
        Hero h2 = new HeroBuilder().setLocation(new Location(0, 0)).createHero();
        Hero h1 = new HeroBuilder().setLocation(new Location(8, 9)).createHero();
        objects.add(h1);
        objects.add(h2);
        assertFalse(GameAction.isLineOfSight(h1.getLocation(), h2.getLocation(), map, objects));
    }

    @Test
    public void testBlockedByCharacter(){
        Hero h2 = new HeroBuilder().setLocation(new Location(0, 0)).createHero();
        Hero h1 = new HeroBuilder().setLocation(new Location(0, 2)).createHero();
        Hero h3 = new HeroBuilder().setLocation(new Location(0, 1)).createHero();
        objects.add(h1);
        objects.add(h2);
        objects.add(h3);
        assertFalse(GameAction.isLineOfSight(h1.getLocation(), h2.getLocation(), map, objects));
    }


    @Test
    public void testFloodFill(){
        Set<Location> locs = GameAction.floodFill(map,new Location(0,0),3,true);
        assertEquals(16,locs.size());
        assertTrue(locs.contains(new Location(0,0)));
        assertTrue(locs.contains(new Location(1,1)));
        assertTrue(locs.contains(new Location(2,2)));
        assertTrue(locs.contains(new Location(3,3)));
        assertTrue(locs.contains(new Location(0,1)));
        assertTrue(locs.contains(new Location(0,2)));
        assertTrue(locs.contains(new Location(0,3)));
        assertTrue(locs.contains(new Location(1,0)));
        assertTrue(locs.contains(new Location(1,2)));
        assertTrue(locs.contains(new Location(1,3)));
        assertTrue(locs.contains(new Location(2,0)));
        assertTrue(locs.contains(new Location(2,1)));
        assertTrue(locs.contains(new Location(2,3)));
        assertTrue(locs.contains(new Location(3,0)));
        assertTrue(locs.contains(new Location(3,1)));
        assertTrue(locs.contains(new Location(3,2)));
    }

    @Test
    public void testFloodFillNoDiagonals(){
        Set<Location> locs = GameAction.floodFill(map,new Location(0,0),3,false);
        assertEquals(10,locs.size());
        assertTrue(locs.contains(new Location(0,0)));
        assertTrue(locs.contains(new Location(1,1)));
        assertTrue(locs.contains(new Location(0,1)));
        assertTrue(locs.contains(new Location(0,2)));
        assertTrue(locs.contains(new Location(0,3)));
        assertTrue(locs.contains(new Location(1,0)));
        assertTrue(locs.contains(new Location(1,2)));
        assertTrue(locs.contains(new Location(2,0)));
        assertTrue(locs.contains(new Location(2,1)));
        assertTrue(locs.contains(new Location(3,0)));
    }

    @Test
    public void testFloodFillObeysObstructionTiles(){
        Set<Location> locs = GameAction.floodFill(map,new Location(3,2),3,false);
        assertEquals(18,locs.size());
        assertTrue(locs.contains(new Location(3,0)));
        assertTrue(locs.contains(new Location(3,1)));
        assertTrue(locs.contains(new Location(3,2)));
        assertTrue(locs.contains(new Location(3,3)));
        assertTrue(locs.contains(new Location(3,4)));
        assertTrue(locs.contains(new Location(3,5)));
        assertTrue(locs.contains(new Location(2,0)));
        assertTrue(locs.contains(new Location(2,1)));
        assertTrue(locs.contains(new Location(2,2)));
        assertTrue(locs.contains(new Location(2,3)));
        assertTrue(locs.contains(new Location(2,4)));
        assertTrue(locs.contains(new Location(1,1)));
        assertTrue(locs.contains(new Location(1,2)));
        assertTrue(locs.contains(new Location(1,3)));
        assertTrue(locs.contains(new Location(0,2)));
        assertTrue(locs.contains(new Location(4,0)));
        assertTrue(locs.contains(new Location(4,1)));
        assertTrue(locs.contains(new Location(5,1)));
    }

}
